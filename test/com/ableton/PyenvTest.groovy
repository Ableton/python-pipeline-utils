package com.ableton

import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertFalse
import static org.junit.jupiter.api.Assertions.assertNotNull
import static org.junit.jupiter.api.Assertions.assertThrows
import static org.junit.jupiter.api.Assertions.assertTrue

import com.lesfurets.jenkins.unit.BasePipelineTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test


class PyenvTest extends BasePipelineTest {
  // Expected random virtualenv directory name for the seed value of 1
  final static String TEST_RANDOM_NAME = 'venv-58734446'

  Object script

  @Override
  @BeforeEach
  @SuppressWarnings('ThrowException')
  void setUp() {
    super.setUp()

    this.script = loadScript('test/resources/EmptyPipeline.groovy')
    assertNotNull(script)
    script.env = ['WORKSPACE': '/workspace']

    helper.registerAllowedMethod('error', [String]) { message ->
      throw new Exception(message)
    }
  }

  @Test
  void assertPyenvRootInvalidRoot() {
    String pyenvRoot = '/mock/pyenv/root'
    helper.registerAllowedMethod('fileExists', [String]) { return false }
    helper.registerAllowedMethod('isUnix', []) { return true }

    assertThrows(Exception) { new Pyenv(script, '1.2.3', pyenvRoot).createVirtualEnv() }
  }

  @Test
  void assertPyenvRootNoRoot() {
    helper.registerAllowedMethod('isUnix', []) { return true }

    assertThrows(AssertionError) { new Pyenv(script, null).createVirtualEnv('1.2.3') }
  }

  @Test
  void createVirtualEnv() {
    String pythonVersion = '1.2.3'
    String pyenvRoot = '/mock/pyenv/root'
    helper.registerAllowedMethod('fileExists', [String]) { return true }
    helper.registerAllowedMethod('isUnix', []) { return true }
    helper.addShMock(installCommands(pyenvRoot, pythonVersion), '', 0)
    helper.addShMock("${pyenvRoot}/bin/pyenv --version", 'pyenv 1.2.3', 0)
    helper.addShMock("${pyenvRoot}/bin/pyenv install --list", '1.2.3', 0)

    Object venv = new Pyenv(script, pyenvRoot).createVirtualEnv(pythonVersion, 1)

    assertEquals("/workspace/.venv/${TEST_RANDOM_NAME}" as String, venv.venvRootDir)
  }

  @Test
  void createVirtualEnvWithTrailingNewline() {
    String pythonVersion = '1.2.3\n'
    String pyenvRoot = '/mock/pyenv/root'
    helper.registerAllowedMethod('fileExists', [String]) { return true }
    helper.registerAllowedMethod('isUnix', []) { return true }
    helper.addShMock(installCommands(pyenvRoot, pythonVersion), '', 1)
    helper.addShMock("${pyenvRoot}/bin/pyenv --version", 'pyenv 1.2.3', 0)
    helper.addShMock("${pyenvRoot}/bin/pyenv install --list", '1.2.3', 0)

    Object venv = new Pyenv(script, pyenvRoot).createVirtualEnv(pythonVersion, 1)

    assertEquals("/workspace/.venv/${TEST_RANDOM_NAME}" as String, venv.venvRootDir)
  }

  @Test
  void createVirtualEnvInstallationFails() {
    String pythonVersion = '1.2.3'
    String pyenvRoot = '/mock/pyenv/root'
    helper.with {
      registerAllowedMethod('fileExists', [String]) { return true }
      registerAllowedMethod('isUnix', []) { return true }
      addShMock(installCommands(pyenvRoot, pythonVersion), '', 1)
      addShMock("${pyenvRoot}/bin/pyenv --version", 'pyenv 1.2.3', 0)
      addShMock("${pyenvRoot}/bin/pyenv install --list", '1.2.3', 0)
    }

    assertThrows(Exception) {
      new Pyenv(script, pyenvRoot).createVirtualEnv(pythonVersion, 1)
    }

    assertEquals(3, helper.callStack.findAll { call ->
      call.methodName == 'sh' &&
        call.args[0].label == 'Install Python version 1.2.3 with pyenv'
    }.size())
  }

  @Test
  void createVirtualEnvFailedSupportedPythonVersion() {
    String pythonVersion = '1.2.3'
    String pyenvRoot = '/mock/pyenv/root'
    helper.with {
      registerAllowedMethod('fileExists', [String]) { return true }
      registerAllowedMethod('isUnix', []) { return true }
      addShMock("${pyenvRoot}/bin/pyenv --version", 'pyenv 1.2.3', 0)
      addShMock("${pyenvRoot}/bin/pyenv install --list", '''Available versions:
  1.2.3
''', 0)
      addShMock(installCommands(pyenvRoot, pythonVersion), '', 1)
    }

    assertThrows(Exception) {
      new Pyenv(script, pyenvRoot).createVirtualEnv(pythonVersion, 1)
    }
  }

  @Test
  void createVirtualEnvWindows() {
    helper.registerAllowedMethod('isUnix', []) { return false }

    assertThrows(Exception) { new Pyenv(script, 'C:\\pyenv').createVirtualEnv('1.2.3') }
  }

  @Test
  void createVirtualEnvUnsupportedPythonVersion() {
    String pythonVersion = '6.6.6'
    String pyenvRoot = '/mock/pyenv/root'
    helper.registerAllowedMethod('error', [String]) { errorCalled = true }
    helper.registerAllowedMethod('fileExists', [String]) { return true }
    helper.registerAllowedMethod('isUnix', []) { return true }
    helper.addShMock("${pyenvRoot}/bin/pyenv --version", 'pyenv 1.2.3', 0)
    helper.addShMock(installCommands(pyenvRoot, pythonVersion), '', 1)

    assertThrows(Exception) {
      new Pyenv(script, pyenvRoot).createVirtualEnv(pythonVersion, 1)
    }
  }

  @Test
  void versionSupported() {
    // Resembles pyenv's output, at least as of version 2.3.x
    String mockPyenvVersions = '''Available versions:
  2.1.3
  2.2.3
  2.3.7
'''
    String pyenvRoot = '/pyenv'
    helper.addShMock("${pyenvRoot}/bin/pyenv install --list", mockPyenvVersions, 0)
    helper.registerAllowedMethod('fileExists', [String]) { return true }
    helper.registerAllowedMethod('isUnix', []) { return true }

    assertTrue(new Pyenv(script, pyenvRoot).versionSupported('2.1.3'))
    assertFalse(new Pyenv(script, pyenvRoot).versionSupported('2.1.3333'))
  }

  @Test
  void versionSupportedWindows() {
    helper.registerAllowedMethod('isUnix', []) { return false }

    assertThrows(Exception) { new Pyenv(script, 'C:\\pyenv').versionSupported('1.2.3') }
  }

  private String installCommands(String pyenvRoot, String pythonVersion) {
    // Indentation must match the actual command
    return """
          export PYENV_ROOT=${pyenvRoot}
          export PATH=\$PYENV_ROOT/bin:\$PATH
          eval "\$(pyenv init --path)"
          eval "\$(pyenv init -)"
          pyenv install --skip-existing ${pythonVersion}
          pyenv shell ${pythonVersion}
          pip install virtualenv
          virtualenv /workspace/.venv/${TEST_RANDOM_NAME}
      """
  }
}
