package com.ableton

import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertFalse
import static org.junit.Assert.assertNotNull
import static org.junit.Assert.assertTrue

import com.lesfurets.jenkins.unit.BasePipelineTest
import org.junit.Before
import org.junit.Test


/**
 * Tests for the VirtualEnv class.
 */
class VirtualEnvTest extends BasePipelineTest {
  @SuppressWarnings('FieldTypeRequired')
  def script

  @Override
  @Before
  void setUp() throws Exception {
    super.setUp()

    this.script = loadScript('test/resources/EmptyPipeline.groovy')
    assertNotNull(script)
    script.env = ['BUILD_NUMBER': 1]

    helper.registerAllowedMethod('deleteDir', [], JenkinsMocks.deleteDir)
    helper.registerAllowedMethod('dir', [String], JenkinsMocks.dir)
    helper.registerAllowedMethod('isUnix', [], JenkinsMocks.isUnix)
    helper.registerAllowedMethod('sh', [String], JenkinsMocks.sh)
  }

  @Test
  void newObjectUnix() throws Exception {
    if (JenkinsMocks.isUnix()) {
      helper.registerAllowedMethod('isUnix', []) {
        return true
      }

      VirtualEnv venv = new VirtualEnv(script, 'python2.7')

      assertNotNull(venv)
      assertNotNull(venv.script)
      assertNotNull(venv.destDir)
    }
  }

  @Test
  void newObjectWindows() throws Exception {
    if (!JenkinsMocks.isUnix()) {
      script.env['TEMP'] = 'C:\\Users\\whatever\\AppData\\Temp'
      helper.registerAllowedMethod('isUnix', []) {
        return false
      }

      VirtualEnv venv = new VirtualEnv(script, 'python2.7')

      assertNotNull(venv)
      assertNotNull(venv.script)
      assertNotNull(venv.destDir)
    }
  }

  @Test
  void newObjectWithNullScript() throws Exception {
    boolean exceptionThrown = false
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
    boolean exceptionThrown = false
    try {
      new VirtualEnv(script, null)
    } catch (AssertionError error) {
      exceptionThrown = true
      assertNotNull(error)
    }
    assertTrue(exceptionThrown)
  }

  @Test
  void newObjectWithAbsolutePath() throws Exception {
    if (JenkinsMocks.isUnix()) {
      String python = '/usr/bin/python3.5'

      VirtualEnv venv = new VirtualEnv(script, python)

      // Expect that the dirname of the python installation is stripped from the
      // virtualenv directory, but that it still retains the correct python version.
      assertFalse(venv.destDir.contains('usr/bin'))
      assertTrue(venv.destDir.endsWith('python3.5'))
    }
  }

  @Test
  void newObjectWithAbsolutePathWindows() throws Exception {
    if (!JenkinsMocks.isUnix()) {
      script.env['TEMP'] = 'C:\\Users\\whatever\\AppData\\Temp'
      String python = '/c/Python27/python'

      VirtualEnv venv = new VirtualEnv(script, python)

      assertFalse(venv.destDir.startsWith('/c'))
      assertTrue(venv.destDir.endsWith('python'))
    }
  }

  @Test
  void create() throws Exception {
    script.env['TEMP'] = 'C:\\Users\\whatever\\AppData\\Temp'
    String python = 'python2.7'

    VirtualEnv venv = new VirtualEnv(script, python)

    JenkinsMocks.addShMock("virtualenv --python=${python} ${venv.destDir}", '', 0)
    VirtualEnv createdVenv = VirtualEnv.create(script, python)
    assertEquals(venv.destDir, createdVenv.destDir)
  }

  @Test
  void cleanup() throws Exception {
    script.env['TEMP'] = 'C:\\Users\\whatever\\AppData\\Temp'

    VirtualEnv venv = new VirtualEnv(script, 'python3.6')

    venv.cleanup()
  }
}
