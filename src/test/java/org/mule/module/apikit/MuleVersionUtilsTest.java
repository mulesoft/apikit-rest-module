/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit;

import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mule.module.apikit.utils.MuleVersionUtils;
import org.mule.runtime.core.api.config.MuleManifest;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class MuleVersionUtilsTest {

  @Test
  public void newerVersionIsAtLeastTest() {
    assertTrue(isAtLeast("4.3.0", "4.2.0"));
  }

  @Test
  public void newerSnapshotVersionIsAtLeastTest() {
    assertTrue(isAtLeast("4.3.0-SNAPSHOT", "4.2.0"));
  }

  @Test
  public void olderVersionIsAtLeastTest() {
    assertFalse(isAtLeast("4.1.0", "4.2.0"));
  }

  @Test
  public void olderSnapshotVersionIsAtLeastTest() {
    assertFalse(isAtLeast("4.1.0-SNAPSHOT", "4.2.0"));
  }

  @Test
  public void sameVersionIsAtLeastTest() {
    assertTrue(isAtLeast("4.2.0", "4.2.0"));
  }

  @Test
  public void snapshotVersionIsAtLeastTest() {
    assertTrue(isAtLeast("4.2.0-SNAPSHOT", "4.2.0"));
  }

  @Test
  public void hotFixVersionIsAtLeastTest() {
    assertTrue(isAtLeast("4.2.0-hf1", "4.2.0"));
  }

  @Test
  public void hotFixWithDateSuffixVersionIsAtLeastTest() {
    assertTrue(isAtLeast("4.2.0-20200525", "4.2.0"));
  }

  @Test
  public void releaseCandidateVersionIsAtLeastTest() {
    assertTrue(isAtLeast("4.2.0-rc1", "4.2.0"));
  }

  @Test
  public void invalidVersionIsAtLeastTest() {
    assertFalse(isAtLeast("4.2.0", "a.b.3"));
  }

  @Test
  public void blankVersionIsAtLeastTest() {
    assertFalse(isAtLeast("4.2.0", " "));
  }

  @Test
  public void nullVersionIsAtLeastTest() {
    assertFalse(isAtLeast("4.2.0", null));
  }

  private Boolean isAtLeast(String productVersion, String version) {
    try (MockedStatic<MuleManifest> mockedManifest = Mockito.mockStatic(MuleManifest.class)) {
      mockedManifest.when(MuleManifest::getProductVersion).thenReturn(productVersion);
      return MuleVersionUtils.isAtLeast(version);
    }
  }
}
