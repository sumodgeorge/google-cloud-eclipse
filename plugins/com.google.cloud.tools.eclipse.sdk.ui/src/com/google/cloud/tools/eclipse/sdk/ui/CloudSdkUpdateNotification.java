/*
 * Copyright 2018 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.cloud.tools.eclipse.sdk.ui;

import com.google.cloud.tools.appengine.cloudsdk.serialization.CloudSdkVersion;
import com.google.cloud.tools.eclipse.ui.util.WorkbenchUtil;
import com.google.cloud.tools.eclipse.ui.util.images.SharedImages;
import com.google.common.annotations.VisibleForTesting;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.mylyn.commons.ui.dialogs.AbstractNotificationPopup;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;

/**
 * Notifies the user that a new version of the Cloud SDK is available. The update should be
 * triggered unless the user explicitly cancels the update.
 */
public class CloudSdkUpdateNotification extends AbstractNotificationPopup {
  private static final Logger logger = Logger.getLogger(CloudSdkUpdateNotification.class.getName());

  /*
   * Unfortunately {@link AbstractNotificationPopup} makes it
   * impossible to determine whether the popup faded away, which we take as implied consent, or if the
   * user clicked on the notification-close button (the {@code X} in the upper right) — equivalent to
   * keying {@code ESC} or closing the window which is CANCEL. Fading away triggers {@link
   * Shell#close()}, the same code path as keying {@code ESC}. Clicking the notification-close button
   * first calls {@link #close()} and <em>then</em>calls {@code setReturnCode(CANCEL)}.
   *
   * <p>So we ignore the {@link #getReturnCode()} and instead maintain a separate tri-state for:
   * INSTALL, SKIP, and UNKNOWN. If UNKNOWN, then we use the @link Shell#getAlpha()} to determine if
   * the shell faded away (INSTALL) or otherwise the user clicked the X (SKIP).
   */
  private enum UpdateDirective {
    INSTALL,
    SKIP,
    UNKNOWN
  };

  private UpdateDirective updateDirective = UpdateDirective.UNKNOWN;

  /**
   * Asynchronously shows a notification that an update is available.
   *
   * @param updateTrigger the action to take when selected; assumed to be short-lived
   */
  public static void showNotification(
      IWorkbench workbench, CloudSdkVersion currentVersion, Runnable updateTrigger) {
    workbench
        .getDisplay()
        .asyncExec(
            () -> {
              CloudSdkUpdateNotification popup =
                  new CloudSdkUpdateNotification(workbench, currentVersion, updateTrigger);
              popup.open(); // won't wait
            });
  }

  private IWorkbench workbench;
  private CloudSdkVersion sdkVersion;
  private Runnable updateRunnable;

  @VisibleForTesting
  CloudSdkUpdateNotification(
      IWorkbench wb, CloudSdkVersion currentVersion, Runnable triggerUpdate) {
    super(wb.getDisplay());
    setBlockOnOpen(false);
    workbench = wb;
    sdkVersion = currentVersion;
    updateRunnable = triggerUpdate;
  }

  @Override
  protected String getPopupShellTitle() {
    return Messages.getString("CloudSdkUpdateNotificationTitle");
  }

  @Override
  protected Image getPopupShellImage(int maximumHeight) {
    return resources.createImage(SharedImages.CLOUDSDK_IMAGE_DESCRIPTOR);
  }

  @Override
  protected void createContentArea(Composite parent) {
    Link message = new Link(parent, SWT.WRAP);
    message.setText(Messages.getString("CloudSdkUpdateNotificationMessage", sdkVersion));
    message.addSelectionListener(
        new SelectionAdapter() {
          @Override
          public void widgetSelected(SelectionEvent event) {
            linkSelected(event.text);
          }
        });
  }

  /** React to the user selecting a link within the notification. */
  @VisibleForTesting
  void linkSelected(String linkText) {
    if ("install".equals(linkText)) {
      updateDirective = UpdateDirective.INSTALL;
      close();
    } else if ("skip".equals(linkText)) {
      updateDirective = UpdateDirective.SKIP;
      close();
    } else if (linkText != null && linkText.startsWith("http")) {
      IStatus status = WorkbenchUtil.openInBrowser(workbench, linkText);
      if (!status.isOK()) {
        logger.log(Level.SEVERE, status.getMessage(), status.getException());
      }
    } else {
      logger.warning("Unknown selection event: " + linkText);
    }
  }

  @Override
  public boolean close() {
    if (shouldInstallUpdate(updateDirective, getShell())) {
      updateRunnable.run();
    }
    return super.close();
  }

  @VisibleForTesting
  static boolean shouldInstallUpdate(UpdateDirective directive, Shell shell) {
    if (directive == UpdateDirective.UNKNOWN) {
      // check if the shell faded away
      return shell != null && !shell.isDisposed() && shell.getAlpha() == 0;
    }
    return directive == UpdateDirective.INSTALL;
  }
}
