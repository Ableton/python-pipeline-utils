package com.ableton

import static org.junit.jupiter.api.Assertions.assertFalse
import static org.junit.jupiter.api.Assertions.assertNotNull
import static org.junit.jupiter.api.Assertions.assertThrows
import static org.junit.jupiter.api.Assertions.assertTrue

import com.lesfurets.jenkins.unit.BasePipelineTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test


class PyenvTest extends BasePipelineTest {
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
    helper.addShMock("""
      export PYENV_ROOT=${pyenvRoot}
      export PATH=\$PYENV_ROOT/bin:\$PATH
      eval "\$(pyenv init -)"
      pyenv install --skip-existing ${pythonVersion}
      pyenv shell ${pythonVersion}
      pip install virtualenv
      virtualenv /workspace/${pythonVersion}
    """, '', 0)

    new Pyenv(script, pyenvRoot).createVirtualEnv(pythonVersion)
  }

  @Test
  void createVirtualEnvWindows() {
    helper.registerAllowedMethod('isUnix', []) { return false }

    assertThrows(Exception) { new Pyenv(script, 'C:\\pyenv').createVirtualEnv('1.2.3') }
  }

  @Test
  void versionSupported() {
    // Resembles pyenv's output, at least as of version 2.3.x
    String mockPyenvVersions = '''Available versions:
  2.1.3
  2.2.3
  2.3.7
'''
    helper.addShMock('/pyenv/bin/pyenv install --list', mockPyenvVersions, 0)
    helper.registerAllowedMethod('fileExists', [String]) { return true }
    helper.registerAllowedMethod('isUnix', []) { return true }

    assertTrue(new Pyenv(script, '/pyenv').versionSupported('2.1.3'))
    assertFalse(new Pyenv(script, '/pyenv').versionSupported('2.1.3333'))
  }

  @Test
  void versionSupportedWindows() {
    helper.registerAllowedMethod('isUnix', []) { return false }

    assertThrows(Exception) { new Pyenv(script, 'C:\\pyenv').versionSupported('1.2.3') }
  }
}
