package com.ableton

import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertNotNull
import static org.junit.Assert.assertTrue

import com.lesfurets.jenkins.unit.BasePipelineTest
import org.junit.Before
import org.junit.Test


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
    helper.registerAllowedMethod('pwd', [Map], JenkinsMocks.pwd)
    helper.registerAllowedMethod('sh', [String], JenkinsMocks.sh)
  }

  @Test
  void newObject() throws Exception {
    def venv = new VirtualEnv(script, 'python2.7')
    assertNotNull(venv)
    assertNotNull(venv.script)
    assertNotNull(venv.destDir)
  }

  @Test
  void newObjectWithNullScript() throws Exception {
    def exceptionThrown = false
    try {
      new VirtualEnv(null, 'python2.7')
    } catch (AssertionError error) {
      exceptionThrown = true
      assertNotNull(error)
    }
    assertTrue(exceptionThrown)
  }

  @Test
  void newObjectWithNullPython() throws Exception {
    def exceptionThrown = false
    try {
      new VirtualEnv(script, null)
    } catch (AssertionError error) {
      exceptionThrown = true
      assertNotNull(error)
    }
    assertTrue(exceptionThrown)
  }

  @Test
  @SuppressWarnings('BuilderMethodWithSideEffects')
  void create() throws Exception {
    def python = 'python2.7'
    def venv = new VirtualEnv(script, python)
    JenkinsMocks.addShMock("virtualenv --python=${python} ${venv.destDir}", '', 0)
    def createdVenv = VirtualEnv.create(script, python)
    assertEquals(venv.destDir, createdVenv.destDir)
  }
}