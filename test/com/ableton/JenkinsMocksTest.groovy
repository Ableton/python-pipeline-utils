package com.ableton

import com.lesfurets.jenkins.unit.BasePipelineTest
import org.junit.Before
import org.junit.Test

import static org.junit.Assert.assertNotNull
import static org.junit.Assert.assertTrue


/**
 * Tests for the JenkinsMocks class.
 */
class JenkinsMocksTest extends BasePipelineTest {
  @Override
  @Before
  void setUp() {
    super.setUp()
  }

  @Test
  void isUnix() throws Exception {
    // It would be pretty redundant to basically re-implement this method in its own test
    // case, so instead we just call the function and see that it didn't go haywire.
    JenkinsMocks.isUnix()
  }

  @Test
  void pwd() throws Exception {
    String result = JenkinsMocks.pwd()
    assertNotNull(result)
    File f = new File(result)
    assertTrue(f.exists())
  }

  @Test
  void pwdTmp() throws Exception {
    String result = JenkinsMocks.pwd(tmp: true)
    assertNotNull(result)
    File f = new File(result)
    assertTrue(f.exists())
  }
}
