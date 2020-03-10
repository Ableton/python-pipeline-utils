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
  Object script

  @Override
  @Before
  void setUp() throws Exception {
    super.setUp()

    this.script = loadScript('test/resources/EmptyPipeline.groovy')
    assertNotNull(script)
    script.env = ['BUILD_NUMBER': 1, 'JOB_BASE_NAME': 'mock']

    helper.registerAllowedMethod('error', [String], JenkinsMocks.error)
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
  void createPyenv() throws Exception {
    String pythonVersion = '1.2.3'
    String pyenvRoot = '/mock/pyenv/root'
    helper.registerAllowedMethod('fileExists', [String]) { return true }
    helper.registerAllowedMethod('isUnix', []) { return true }
    // Note: This empty string allows us to compensate for trailing whitespace, which is
    // needed to match the string given to the sh mock.
    String empty = ''
    JenkinsMocks.addShMock("""
      ${empty}
      export PYENV_ROOT=${pyenvRoot}
      export PATH=\$PYENV_ROOT/bin:\$PATH
      eval "\$(pyenv init -)"
    ${empty}
      pyenv install --skip-existing ${pythonVersion}
      pyenv shell ${pythonVersion}
      pip install virtualenv
      virtualenv /tmp/mock/1/${pythonVersion}
    """, '', 0)

    VirtualEnv venv = VirtualEnv.create(script, pythonVersion, pyenvRoot)

    assertTrue(venv.activateCommands.contains(pyenvRoot))
  }

  @Test(expected = Exception)
  void createPyenvInvalidRoot() throws Exception {
    String pyenvRoot = '/mock/pyenv/root'
    helper.registerAllowedMethod('fileExists', [String]) { return false }
    helper.registerAllowedMethod('isUnix', []) { return true }

    VirtualEnv.create(script, '1.2.3', pyenvRoot)
  }

  @Test(expected = Exception)
  void createPyenvWindows() throws Exception {
    helper.registerAllowedMethod('isUnix', []) { return false }

    VirtualEnv.create(script, '1.2.3', 'C:\\pyenv')
  }

  @Test(expected = AssertionError)
  void createPyenvNoRoot() throws Exception {
    helper.registerAllowedMethod('isUnix', []) { return true }

    VirtualEnv.create(script, '1.2.3', null)
  }

  @Test
  void cleanup() throws Exception {
    script.env['TEMP'] = 'C:\\Users\\whatever\\AppData\\Temp'

    helper.registerAllowedMethod('deleteDir', [], JenkinsMocks.deleteDir)
    helper.registerAllowedMethod('dir', [String], JenkinsMocks.dir)

    VirtualEnv venv = new VirtualEnv(script, 'python3.6')

    venv.cleanup()
  }
}
