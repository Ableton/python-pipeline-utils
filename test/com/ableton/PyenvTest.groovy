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
    List shMocks = [
      new Tuple(installCommands(pyenvRoot, pythonVersion), '', 0),
      new Tuple("${pyenvRoot}/bin/pyenv install --list", '1.2.3', 0),
    ]
    shMocks.each { mock -> helper.addShMock(mock[0], mock[1], mock[2]) }

    Object venv = new Pyenv(script, pyenvRoot).createVirtualEnv(pythonVersion, 1)

    assertEquals("/workspace/.venv/${TEST_RANDOM_NAME}" as String, venv.venvRootDir)
    shMocks.each { mock -> assertCallStackContains(mock[0]) }
  }

  @Test
  void createVirtualEnvWithTrailingNewline() {
    String pythonVersion = '1.2.3'
    String pyenvRoot = '/mock/pyenv/root'
    helper.registerAllowedMethod('fileExists', [String]) { return true }
    List shMocks = [
      new Tuple(installCommands(pyenvRoot, pythonVersion), '', 0),
      new Tuple("${pyenvRoot}/bin/pyenv install --list", '1.2.3', 0),
    ]
    shMocks.each { mock -> helper.addShMock(mock[0], mock[1], mock[2]) }

    Object venv = new Pyenv(script, pyenvRoot).createVirtualEnv("${pythonVersion}\n", 1)

    assertEquals("/workspace/.venv/${TEST_RANDOM_NAME}" as String, venv.venvRootDir)
    shMocks.each { mock -> assertCallStackContains(mock[0]) }
  }

  @Test
  void createVirtualEnvInstallationFails() {
    String pythonVersion = '1.2.3'
    String pyenvRoot = '/mock/pyenv/root'
    helper.registerAllowedMethod('fileExists', [String]) { return true }
    List shMocks = [
      new Tuple(installCommands(pyenvRoot, pythonVersion), '', 1),
      new Tuple("${pyenvRoot}/bin/pyenv install --list", '1.2.3', 0),
    ]
    shMocks.each { mock -> helper.addShMock(mock[0], mock[1], mock[2]) }

    assertThrows(Exception) {
      new Pyenv(script, pyenvRoot).createVirtualEnv(pythonVersion, 1)
    }

    shMocks.each { mock -> assertCallStackContains(mock[0]) }
  }

  @Test
  void createVirtualEnvWindows() {
    script.env['OS'] = 'Windows_NT'

    assertThrows(Exception) { new Pyenv(script, 'C:\\pyenv').createVirtualEnv('1.2.3') }
  }

  @Test
  void createVirtualEnvUnsupportedPythonVersion() {
    String pythonVersion = '6.6.6'
    String pyenvRoot = '/mock/pyenv/root'
    boolean errorCalled = false
    helper.registerAllowedMethod('error', [String]) { errorCalled = true }
    helper.registerAllowedMethod('fileExists', [String]) { return true }
    helper.addShMock("${pyenvRoot}/bin/pyenv install --list", '1.0.0', 0)
    helper.addShMock("${pyenvRoot}/bin/pyenv --version", 'pyenv 1.2.3', 0)

    new Pyenv(script, pyenvRoot).createVirtualEnv(pythonVersion, 1)

    assertTrue(errorCalled)
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

    assertTrue(new Pyenv(script, pyenvRoot).versionSupported('2.1.3'))
    assertFalse(new Pyenv(script, pyenvRoot).versionSupported('2.1.3333'))
    assertCallStackContains("${pyenvRoot}/bin/pyenv install --list")
  }

  @Test
  void versionSupportedWindows() {
    script.env['OS'] = 'Windows_NT'

    assertThrows(Exception) { new Pyenv(script, 'C:\\pyenv').versionSupported('1.2.3') }
  }

  private String installCommands(String pyenvRoot, String pythonVersion) {
    List installCommands = [
      "export PYENV_ROOT=${pyenvRoot}",
      "export PATH=\$PYENV_ROOT/bin:\$PATH",
      'eval "\$(pyenv init --path)"',
      'eval "\$(pyenv init -)"',
      "pyenv install --skip-existing ${pythonVersion}",
      'pyenv exec pip install virtualenv',
      "pyenv exec virtualenv /workspace/.venv/${TEST_RANDOM_NAME}",
    ]

    return installCommands.join('\n') + '\n'
  }
}
