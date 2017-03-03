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

import com.google.api.services.storage.Storage;
import com.google.api.services.storage.model.Bucket;

import java.io.IOException;

/**
 * A proxy for {@code Storage}.
 *
 * <p>Hides that the underlying {@code Storage} implementation can be repackaged so
 * unit tests in another module can mock out the service calls appropriately.
 */
public class StorageProxy {
  private final Storage storage;

  private StorageProxy(Storage storage) {
    this.storage = storage;
  }

  public static StorageProxy of(Storage storage) {
    return new StorageProxy(storage);
  }

  public BucketsProxy listBuckets(String projectName) throws IOException {
    return new BucketsProxy(storage.buckets().list(projectName).execute());
  }

  public void insertBucket(String projectName, Bucket newBucket) throws IOException {
    storage.buckets().insert(projectName, newBucket).execute();
  }

  public void getBucket(String bucketName) throws IOException {
    storage.buckets().get(bucketName).execute();
  }
}

