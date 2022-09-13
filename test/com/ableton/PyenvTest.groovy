package com.ableton

import static org.junit.jupiter.api.Assertions.assertNotNull
import static org.junit.jupiter.api.Assertions.assertThrows

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

    Pyenv.createVirtualEnv(script, pythonVersion, pyenvRoot)
  }

  @Test
  void createVirtualEnvInvalidRoot() {
    String pyenvRoot = '/mock/pyenv/root'
    helper.registerAllowedMethod('fileExists', [String]) { return false }
    helper.registerAllowedMethod('isUnix', []) { return true }

    assertThrows(Exception) { Pyenv.createVirtualEnv(script, '1.2.3', pyenvRoot) }
  }

  @Test
  void createVirtualEnvNoRoot() {
    helper.registerAllowedMethod('isUnix', []) { return true }

    assertThrows(AssertionError) { Pyenv.createVirtualEnv(script, '1.2.3', null) }
  }

  @Test
  void createVirtualEnvWindows() {
    helper.registerAllowedMethod('isUnix', []) { return false }

    assertThrows(Exception) { Pyenv.createVirtualEnv(script, '1.2.3', 'C:\\pyenv') }
  }
}
