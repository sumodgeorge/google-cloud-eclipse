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

package com.google.cloud.tools.eclipse.appengine.facets.ui.navigator.model;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.viewers.StyledString;

/**
 * Represents an {@code queue.xml} element.
 */
public class TaskQueuesDescriptor extends AppEngineResourceElement {
  public TaskQueuesDescriptor(IFile file) {
    super(file);
  }

  @Override
  public StyledString getStyledLabel() {
    return new StyledString("Task Queue Definitions")
        .append(" - " + getFile().getName(), StyledString.DECORATIONS_STYLER);
  }
}
