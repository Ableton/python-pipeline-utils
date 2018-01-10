package com.ableton

import com.lesfurets.jenkins.unit.BasePipelineTest
import org.junit.Before
import org.junit.Test

import static org.junit.Assert.assertNotNull
import static org.junit.Assert.fail


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
    script.env = ['BUILD_NUMBER': 1]

    helper.registerAllowedMethod('isUnix', [], JenkinsMocks.isUnix)
    helper.registerAllowedMethod('pwd', [Map.class], JenkinsMocks.pwd)
    helper.registerAllowedMethod('sh', [String.class], JenkinsMocks.sh)
  }

  @Test
  void newObject() throws Exception {
    def venv = new VirtualEnv(script, 'python2.7')
    assertNotNull(venv)
    assertNotNull(venv.script)
    assertNotNull(venv.destDir)
  }
}
