/*
 * Copyright (C) 2017 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *
 */

package com.google.cloud.tools.eclipse.dataflow.core.project;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeFalse;
import static org.junit.Assume.assumeTrue;

import com.google.common.collect.Iterables;
import org.apache.maven.artifact.versioning.ArtifactVersion;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.apache.maven.artifact.versioning.Restriction;
import org.apache.maven.artifact.versioning.VersionRange;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import java.util.Arrays;

/**
 * Tests for {@link MajorVersion}.
 */
@RunWith(Parameterized.class)
public class MajorVersionTest {
  @Rule public ExpectedException thrown = ExpectedException.none();

  @Parameters
  public static Iterable<? extends Object> majorVersions() {
    return Arrays.asList(MajorVersion.values());
  }

  private final MajorVersion mv;
  public MajorVersionTest(MajorVersion mv) {
    this.mv = mv;
  }

  @Test
  public void testTruncatedVersionAtBeginningInternallyBackwardsCompatible() {
    assumeTrue(mv.hasStableApi());
    assertEquals(
        mv.getVersionRange(),
        mv.getTruncatedVersionRange(mv.getInitialVersion()));
  }

  @Test
  public void testTruncatedVersionAtBeginningInternallyBackwardsIncompatible() throws Exception {
    assumeFalse(mv.hasStableApi());
    assertEquals(
        VersionRange.createFromVersion(mv.getInitialVersion().toString()),
        mv.getTruncatedVersionRange(mv.getInitialVersion()));
  }

  @Test
  public void testTruncatedVersionAtEnd() {
    assumeFalse(mv.getMaxVersion().toString().trim().isEmpty());
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("it does not contain");
    thrown.expectMessage(mv.getMaxVersion().toString());
    thrown.expectMessage(mv.getVersionRange().toString());
    mv.getTruncatedVersionRange(mv.getMaxVersion());
  }

  @Test
  public void testTruncatedVersionBeforeBeginning() {
    String version = "0.0.0-alpha";
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("it does not contain");
    thrown.expectMessage(version);
    thrown.expectMessage(mv.getVersionRange().toString());
    mv.getTruncatedVersionRange(new DefaultArtifactVersion(version));
  }

  // This runs three times even though it doesn't use the parameters
  @Test
  public void testTruncatedVersionIntermediate() throws Exception {
    assertEquals(
        VersionRange.createFromVersionSpec("[1.2.3, " + MajorVersion.ONE.getMaxVersion() + ")"),
        MajorVersion.ONE.getTruncatedVersionRange(new DefaultArtifactVersion("1.2.3")));
    assertEquals(
        VersionRange.createFromVersionSpec("[2.4.8, " + MajorVersion.TWO.getMaxVersion() + ")"),
        MajorVersion.TWO.getTruncatedVersionRange(new DefaultArtifactVersion("2.4.8")));
    assertEquals(
        VersionRange.createFromVersion("3.9.27-beta81"),
        MajorVersion.THREE_PLUS.getTruncatedVersionRange(
            new DefaultArtifactVersion("3.9.27-beta81")));
  }

  @Test
  public void testInitialMaxVersionRange() throws Exception {
    VersionRange expectedRange =
        VersionRange.createFromVersionSpec(
            String.format("[%s, %s)", mv.getInitialVersion(), mv.getMaxVersion()));
    assertEquals(
        "Major Versions should produce a version specification from "
            + "their initial version (inclusive) to their max version (exclusive)",
        expectedRange,
        mv.getVersionRange());
  }

  @Test
  public void testVersionRangeFromSpec() throws Exception {
    VersionRange versionRange = mv.getVersionRange();
    Restriction restriction = Iterables.getOnlyElement(versionRange.getRestrictions());
    assertEquals(mv.getVersionRange(), mv.getVersionRange());

    assertTrue(restriction.isLowerBoundInclusive());
    assertEquals(mv.getInitialVersion(), restriction.getLowerBound());

    assertFalse(restriction.isUpperBoundInclusive());
    if (mv.getMaxVersion().toString().trim().isEmpty()) {
      assertNull(
          "No Upper Bound should be specified if the max version is empty",
          restriction.getUpperBound());
    } else {
      assertEquals(mv.getMaxVersion(), restriction.getUpperBound());
    }
  }

  @Test
  public void testTruncateVersionRange() {
    ArtifactVersion initialVersion = mv.getInitialVersion();
    ArtifactVersion newStart =
        new DefaultArtifactVersion(
            String.format(
                "%s.%s.%s",
                initialVersion.getMajorVersion(),
                initialVersion.getMajorVersion(),
                initialVersion.getIncrementalVersion() + 5));
    assumeTrue(mv.getMaxVersion().compareTo(newStart) > 0);
    VersionRange updatedRange = MajorVersion.truncateAtLatest(newStart, mv.getVersionRange());
    assertFalse(updatedRange.containsVersion(mv.getInitialVersion()));
    ArtifactVersion afterStart =
        new DefaultArtifactVersion(
            String.format(
                "%s.%s.%s",
                initialVersion.getMajorVersion(),
                initialVersion.getMajorVersion(),
                initialVersion.getIncrementalVersion() + 6));
    if (mv.hasStableApi()) {
      assertTrue(updatedRange.containsVersion(afterStart));
    } else {
      assertFalse(updatedRange.containsVersion(afterStart));
    }
  }

  @Test
  public void testTruncateVersionRangeNotInRange() {
    assumeTrue(!mv.getMaxVersion().toString().isEmpty());
    thrown.expect(IllegalArgumentException.class);
    MajorVersion.truncateAtLatest(mv.getMaxVersion(), mv.getVersionRange());
  }

  @Test
  public void testStableVersionGetStableSelf() {
    assumeTrue(mv.hasStableApi());
    assertEquals(mv, mv.getStableVersion());
  }

  @Test
  public void testUnstableVersionGetStableStartsAtMaxVersion() {
    assumeFalse(mv.hasStableApi());
    assumeFalse(mv.getMaxVersion().toString().isEmpty());
    assertEquals(
        "The Stable Version Range for a version without a stable API should "
            + "start at the end of the Unstable Version Range",
        mv.getStableVersion().getInitialVersion(),
        mv.getMaxVersion());
  }

  @Test
  public void testStableApiOrdering() {
    assumeFalse(mv.hasStableApi());
    try {
      MajorVersion stable = mv.getStableVersion();
      assertTrue(
          "A version that is not stable should always precede the stable version that replaces it",
          mv.compareTo(stable) < 0);
    } catch (IllegalArgumentException ignored) {
      // Not all Major Versions have a stable version, even if they also do not have a stable API.
      // This test only demonstrates the relation between versions without a stable API that have a
      // future stable API.
    }
  }
}
