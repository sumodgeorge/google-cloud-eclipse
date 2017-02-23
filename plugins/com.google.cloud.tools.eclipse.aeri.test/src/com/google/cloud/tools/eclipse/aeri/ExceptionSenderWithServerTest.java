/*
 * Copyright 2017 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.cloud.tools.eclipse.aeri;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.google.cloud.tools.eclipse.test.util.http.TestHttpServer;
import com.google.cloud.tools.eclipse.util.CloudToolsInfo;
import java.io.IOException;
import org.eclipse.epp.logging.aeri.core.IThrowable;
import org.junit.Rule;
import org.junit.Test;

public class ExceptionSenderWithServerTest {

  @Rule public TestHttpServer server = new TestHttpServer("", "");

  private static final String EXPECTED_POST_BODY =
      "-----------------------------45224ee4-f3c1-4b23-8df1-4012f722218c" + "\r\n" +
      "Accept-Encoding: gzip" + "\r\n" +
      "Content-Length: 4" + "\r\n" +
      "content-disposition: form-data; name=\"product\"" + "\r\n" +
      "content-transfer-encoding: binary" + "\r\n" +
      "\r\n" +
      "CT4E" + "\r\n" +
      "-----------------------------45224ee4-f3c1-4b23-8df1-4012f722218c" + "\r\n" +
      "Accept-Encoding: gzip" + "\r\n" +
      "Content-Length: 255" + "\r\n" +
      "content-disposition: form-data; name=\"exception_info\"" + "\r\n" +
      "content-transfer-encoding: binary" + "\r\n" +
      "\r\n" +
      "java.nio.charset.IllegalCharsetNameException: no CT4E packages in stack trace\n" +
      "\tat com.example.Charlie.boom(Charlie.java:34)\n" +
      "\tat com.example.Bob.callCharlie(Bob.java:30)\n" +
      "\tat com.example.Alice.callBob(Alice.java:26)\n" +
      "\tat java.lang.Thread.run(Thread.java:745)" + "\r\n" +
      "-----------------------------45224ee4-f3c1-4b23-8df1-4012f722218c" + "\r\n" +
      "Accept-Encoding: gzip" + "\r\n" +
      "Content-Length: 162" + "\r\n" +
      "content-disposition: form-data; name=\"comments\"" + "\r\n" +
      "content-transfer-encoding: binary" + "\r\n" +
      "\r\n" +
      "eclipseBuildId: eclipseBuildId Alpha\n" +
      "javaVersion: javaVersion Bravo\n" +
      "os: os Charie osVersion Delta\n" +
      "userSeverity: userSeverity Echo\n" +
      "userComment: userComment Foxtrot" + "\r\n" +
      "-----------------------------45224ee4-f3c1-4b23-8df1-4012f722218c" + "\r\n" +
      "Accept-Encoding: gzip" + "\r\n" +
      "Content-Length: 15" + "\r\n" +
      "content-disposition: form-data; name=\"version\"" + "\r\n" +
      "content-transfer-encoding: binary" + "\r\n" +
      "\r\n" +
      "0.1.0.qualifier" + "\r\n" +
      "-----------------------------45224ee4-f3c1-4b23-8df1-4012f722218c--" + "\r\n";

  @Test
  public void testSendException() throws IOException {
    IThrowable aeriThrowable = AeriTestUtil.getMockIThrowable(AeriTestUtil.sampleThrowable);

    new ExceptionSender(server.getAddress()).sendException(aeriThrowable, "eclipseBuildId Alpha",
        "javaVersion Bravo", "os Charie", "osVersion Delta", "userSeverity Echo", "userComment Foxtrot");
    assertEquals("POST", server.getRequestMethod());
    assertTrue(server.requestParametersContain("product", "CT4E"));
    assertTrue(server.requestParametersContain("version", CloudToolsInfo.getToolsVersion()));
    assertTrue(server.getRequestHeaders()
        .get("Content-Type").startsWith("multipart/form-data; boundary="));
    assertEquals(EXPECTED_POST_BODY, server.getRequestBody());
  }
}
