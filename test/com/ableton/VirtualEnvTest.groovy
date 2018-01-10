package com.ableton

import com.lesfurets.jenkins.unit.BasePipelineTest
import org.junit.Before

import static org.junit.Assert.assertNotNull


/**
 * Tests for the VirtualEnv class.
 */
class VirtualEnvTest extends BasePipelineTest {
  def script

  @Override
  @Before
  void setUp() throws Exception {
    super.setUp()

    this.script = loadScript('test/resources/EmptyPipeline.groovy')
    assertNotNull(script)
  }
}
