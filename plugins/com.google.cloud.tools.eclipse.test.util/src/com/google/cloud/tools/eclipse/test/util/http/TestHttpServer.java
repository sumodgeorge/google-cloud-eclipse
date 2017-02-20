package com.google.cloud.tools.eclipse.test.util.http;

import static org.junit.Assert.assertTrue;

import com.google.common.base.Preconditions;
import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.server.handler.HandlerWrapper;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.junit.rules.ExternalResource;
import org.junit.rules.TemporaryFolder;

/**
 * Simple HTTP server (wrapping an embedded Jetty server) to serve a file on a random available port.
 * <p>
 * Use {@link #getAddress()} to obtain the server's address after it has been started via the {@link #before()} method.
 */
public class TestHttpServer extends ExternalResource {

  private static final Logger logger = Logger.getLogger(TestHttpServer.class.getName());

  private Server server;

  private boolean requestHandled = false;
  private String requestMethod;
  private Map<String, String[]> requestParameters;

  private TemporaryFolder temporaryFolder;
  private String fileName;
  private String fileContent;

  public TestHttpServer() {}

  /** Sets up a file server. */
  public TestHttpServer(TemporaryFolder temporaryFolder, String fileName, String fileContent) {
    this.temporaryFolder = temporaryFolder;
    this.fileName = fileName;
    this.fileContent = fileContent;
  }

  @Override
  protected void before() throws Exception {
    if (fileContent == null) {
      runServer(new OkHandler());
    } else {
      runFileServer();
    }
  }

  @Override
  protected void after() {
    stopServer();
    assertTrue(requestHandled);
  }

  private void runServer(Handler handler) throws Exception {
    server = new Server(new InetSocketAddress("127.0.0.1", 0));
    server.setHandler(new RequestLogger(handler));
    server.start();
  }

  private void runFileServer() throws Exception {
    File resourceBase = temporaryFolder.newFolder();
    java.nio.file.Path fileToServe = Files.createFile(resourceBase.toPath().resolve(fileName));
    Files.write(fileToServe, fileContent.getBytes(StandardCharsets.UTF_8));

    ResourceHandler handler = new ResourceHandler();
    handler.setResourceBase(resourceBase.getAbsolutePath());
    runServer(handler);
  }

  private void stopServer() {
    try {
      server.stop();
      server.join();
    } catch (Exception ex) {
      // probably should not fail the test, but if it happens it should be visible in the logs
      logger.log(Level.WARNING, "Error while shutting down Jetty server", ex);
    }
  }

  /**
   * Returns the address that can be used to get resources from the server.
   * <p>
   * Initialized only after the server has started.
   *
   * @return server address in the form of http://127.0.0.1:&lt;port&gt;/
   */
  public String getAddress() {
    Preconditions.checkNotNull(server, "server isn't started yet");
    // assume a single server connector
    return "http://127.0.0.1:" + ((ServerConnector) server.getConnectors()[0]).getLocalPort() + "/";
  }

  public String getRequestMethod() {
    Preconditions.checkState(requestHandled);
    return requestMethod;
  }

  public Map<String, String[]> getRequestParameters() {
    Preconditions.checkState(requestHandled);
    return requestParameters;
  }

  private class RequestLogger extends HandlerWrapper {

    private RequestLogger(Handler handler) {
      setHandler(handler);
    }

    @Override
    public void handle(String target, Request baseRequest, HttpServletRequest request,
        HttpServletResponse response) throws IOException, ServletException {
      Preconditions.checkState(!requestHandled);
      requestHandled = true;
      requestMethod = request.getMethod();
      requestParameters = request.getParameterMap();

      super.handle(target, baseRequest, request, response);
    }
  };

  private static class OkHandler extends AbstractHandler {
    @Override
    public void handle(String target, Request baseRequest, HttpServletRequest request,
        HttpServletResponse response) throws IOException, ServletException {
      baseRequest.setHandled(true);
      response.setStatus(HttpServletResponse.SC_OK);
    }
  }
}
