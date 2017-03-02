/*
 * Copyright 2015 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.google.cloud.tools.eclipse.dataflow.core.internal.proxy;

import com.google.api.services.storage.model.Bucket;

/**
 * A proxy class for {@code Bucket}.
 *
 * <p>Used to hide the fact that the underlying {@code Bucket} implementation can be repackaged so
 * unit tests in another module can mock out the service calls appropriately.
 */
public class BucketProxy {
  private final Bucket bucket;

  public BucketProxy(Bucket bucket) {
    this.bucket = bucket;
  }

  public static BucketProxy of(Bucket bucket) {
    return new BucketProxy(bucket);
  }

  public String getName() {
    return bucket.getName();
  }
}

