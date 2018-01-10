package com.ableton

import com.lesfurets.jenkins.unit.BasePipelineTest
import org.junit.Before
import org.junit.Test

import static org.junit.Assert.assertEquals
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

  @Test
  void newObjectWithNullScript() throws Exception {
    try {
      def venv = new VirtualEnv(null, 'python2.7')
      fail('Expected exception, but none was thrown')
    } catch (AssertionError error) {
      assertNotNull(error)
    }
  }

  @Test
  void newObjectWithNullPython() throws Exception {
    try {
      def venv = new VirtualEnv(script, null)
      fail('Expected exception, but none was thrown')
    } catch (AssertionError error) {
      assertNotNull(error)
    }
  }

  @Test
  void create() throws Exception {
    def python = 'python2.7'
    def venv = new VirtualEnv(script, python)
    JenkinsMocks.addShMock("virtualenv --python=${python} ${venv.destDir}", '', 0)
    def createdVenv = VirtualEnv.create(script, python)
    assertEquals(venv.destDir, createdVenv.destDir)
  }
}
