/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.apikit;

import org.junit.Test;
import org.mule.module.apikit.utils.MuleVersionUtils;
import org.mule.runtime.core.api.config.MuleManifest;

import java.util.jar.Attributes;
import java.util.jar.Manifest;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mule.module.apikit.MockingUtils.setAccessible;

public class MuleVersionUtilsTest {

  @Test
  public void newerVersionIsAtLeastTest() throws Exception {
    setManifestImplementationVersion("4.3.0");
    assertTrue(MuleVersionUtils.isAtLeast("4.2.0"));
  }

  @Test
  public void newerSnapshotVersionIsAtLeastTest() throws Exception {
    setManifestImplementationVersion("4.3.0-SNAPSHOT");
    assertTrue(MuleVersionUtils.isAtLeast("4.2.0"));
  }

  @Test
  public void olderVersionIsAtLeastTest() throws Exception {
    setManifestImplementationVersion("4.1.0");
    assertFalse(MuleVersionUtils.isAtLeast("4.2.0"));
  }

  @Test
  public void olderSnapshotVersionIsAtLeastTest() throws Exception {
    setManifestImplementationVersion("4.1.0-SNAPSHOT");
    assertFalse(MuleVersionUtils.isAtLeast("4.2.0"));
  }

  @Test
  public void sameVersionIsAtLeastTest() throws Exception {
    setManifestImplementationVersion("4.2.0");
    assertTrue(MuleVersionUtils.isAtLeast("4.2.0"));
  }

  @Test
  public void snapshotVersionIsAtLeastTest() throws Exception {
    setManifestImplementationVersion("4.2.0-SNAPSHOT");
    assertTrue(MuleVersionUtils.isAtLeast("4.2.0"));
  }

  @Test
  public void hotFixVersionIsAtLeastTest() throws Exception {
    setManifestImplementationVersion("4.2.0-hf1");
    assertTrue(MuleVersionUtils.isAtLeast("4.2.0"));
  }

  @Test
  public void hotFixWithDateSuffixVersionIsAtLeastTest() throws Exception {
    setManifestImplementationVersion("4.2.0-20200525");
    assertTrue(MuleVersionUtils.isAtLeast("4.2.0"));
  }

  @Test
  public void releaseCandidateVersionIsAtLeastTest() throws Exception {
    setManifestImplementationVersion("4.2.0-rc1");
    assertTrue(MuleVersionUtils.isAtLeast("4.2.0"));
  }

  @Test
  public void invalidVersionIsAtLeastTest() throws Exception {
    setManifestImplementationVersion("4.2.0");
    assertFalse(MuleVersionUtils.isAtLeast("a.b.3"));
  }

  @Test
  public void blankVersionIsAtLeastTest() throws Exception {
    setManifestImplementationVersion("4.2.0");
    assertFalse(MuleVersionUtils.isAtLeast(" "));
  }

  @Test
  public void nullVersionIsAtLeastTest() throws Exception {
    setManifestImplementationVersion("4.2.0");
    assertFalse(MuleVersionUtils.isAtLeast(null));
  }

  private void setManifestImplementationVersion(String version) throws Exception {
    Manifest manifestMock = mock(Manifest.class);
    when(manifestMock.getMainAttributes()).thenReturn(mock(Attributes.class));
    when(manifestMock.getMainAttributes().getValue(new Attributes.Name("Implementation-Version"))).thenReturn(version);
    setAccessible(MuleManifest.class.getDeclaredField("manifest"), manifestMock);
  }


}
