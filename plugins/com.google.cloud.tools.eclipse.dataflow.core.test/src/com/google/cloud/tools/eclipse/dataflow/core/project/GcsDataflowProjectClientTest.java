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

package com.google.cloud.tools.eclipse.dataflow.core.project;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.cloud.tools.eclipse.dataflow.core.internal.proxy.BucketProxy;
import com.google.cloud.tools.eclipse.dataflow.core.internal.proxy.BucketsProxy;
import com.google.cloud.tools.eclipse.dataflow.core.internal.proxy.StorageProxy;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Tests for {@link GcsDataflowProjectClient}.
 */
@RunWith(JUnit4.class)
public class GcsDataflowProjectClientTest {
  private static final String PROJECT = "myProject";

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Mock
  private StorageProxy storage;

  @Mock
  private BucketsProxy buckets;

  private GcsDataflowProjectClient client;

  @Before
  public void setup() {
    MockitoAnnotations.initMocks(this);

    client = new GcsDataflowProjectClient(storage);
  }

  @Test
  public void testGetStagingLocationsCallsGcsAndReturnsBucketNames() throws Exception {
    when(storage.listBuckets(PROJECT)).thenReturn(buckets);
    BucketProxy fooB = bucket("foo");
    BucketProxy barB = bucket("bar");
    BucketProxy bazB = bucket("baz");
    BucketProxy quuxB = bucket("quux");
    List<BucketProxy> resultBuckets = Arrays.asList(fooB, barB, bazB, quuxB);
    when(buckets.getItems()).thenReturn(resultBuckets);

    Set<String> stagingLocations = client.getPotentialStagingLocations(PROJECT);

    Set<String> expectedResult = new HashSet<>();
    expectedResult.addAll(Arrays.asList("gs://foo", "gs://bar", "gs://baz", "gs://quux"));

    assertEquals(expectedResult, stagingLocations);
  }

  @Test
  public void testGetStagingLocationsWithExceptionRethrows() throws Exception {
    doThrow(IOException.class).when(storage).listBuckets(PROJECT);

    thrown.expect(IOException.class);

    Set<String> stagingLocations = client.getPotentialStagingLocations(PROJECT);

    assertTrue(stagingLocations.isEmpty());
  }

  @Test
  public void testToGcsLocationUriWithFullUriReturnsUri() {
    String location = "gs://foo-bar/baz";
    assertEquals(location, client.toGcsLocationUri(location));
  }

  @Test
  public void testToGcsLocationUriWithNullReturnsNull() {
    assertEquals(null, client.toGcsLocationUri(null));
  }

  @Test
  public void testToGcsLocationUriWithEmptyInputReturnsEmpty() {
    assertEquals("", client.toGcsLocationUri(""));
  }

  @Test
  public void testToGcsLocationUriWithoutPrefixReturnsWithPrefix() {
    String location = "foo-bar/baz";
    assertEquals("gs://" + location, client.toGcsLocationUri(location));
  }

  @Test
  public void testToGcsLocationUriWithBucketNameOnlyReturnsWithPrefix() {
    String location = "foo-bar";
    assertEquals("gs://foo-bar", client.toGcsLocationUri(location));
  }

  private BucketProxy bucket(String bucketName) {
    BucketProxy result = mock(BucketProxy.class);
    when(result.getName()).thenReturn(bucketName);
    return result;
  }
}

