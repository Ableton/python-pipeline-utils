package com.ableton

import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertFalse
import static org.junit.jupiter.api.Assertions.assertNotNull
import static org.junit.jupiter.api.Assertions.assertTrue

import com.lesfurets.jenkins.unit.BasePipelineTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test


/**
 * Tests for the VirtualEnv class.
 */
class VirtualEnvTest extends BasePipelineTest {
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
  void cleanup() {
    VirtualEnv venv = new VirtualEnv(script)

    venv.cleanup()
  }

  @Test
  void create2() {
    String python = 'python2.7'

    VirtualEnv venv = new VirtualEnv(script, 1)

    helper.addShMock("virtualenv --python=${python} ${venv.venvRootDir}", '', 0)
    VirtualEnv createdVenv = VirtualEnv.create(script, python, 1)
    assertEquals(venv.venvRootDir, createdVenv.venvRootDir)
  }

  @Test
  void create3() {
    String python = 'python3'

    VirtualEnv venv = new VirtualEnv(script, 1)

    helper.addShMock("python3 -m venv ${venv.venvRootDir}", '', 0)
    VirtualEnv createdVenv = VirtualEnv.create(script, python, 1)
    assertEquals(venv.venvRootDir, createdVenv.venvRootDir)
  }

  @Test
  void inside() {
    helper.registerAllowedMethod('isUnix', []) { return true }
    Map insideEnv

    new VirtualEnv(script, 1).inside { insideEnv = binding.getVariable('env') }

    assertTrue(insideEnv.keySet().contains('PATH+VENVBIN'))
    assertEquals(
      "/workspace/.venv/${TEST_RANDOM_NAME}/bin" as String, insideEnv['PATH+VENVBIN']
    )
  }

  @Test
  void newObjectUnix() {
    helper.registerAllowedMethod('isUnix', []) { return true }

    VirtualEnv venv = new VirtualEnv(script, 1)

    assertNotNull(venv)
    assertNotNull(venv.script)
    assertNotNull(venv.venvRootDir)
    assertEquals("/workspace/.venv/${TEST_RANDOM_NAME}" as String, venv.venvRootDir)
  }

  @Test
  void newObjectWindows() {
    helper.registerAllowedMethod('isUnix', []) { return false }
    script.env.WORKSPACE = 'C:\\workspace'

    VirtualEnv venv = new VirtualEnv(script, 1)

    assertNotNull(venv)
    assertNotNull(venv.script)
    assertNotNull(venv.venvRootDir)
    assertEquals("C:/workspace/.venv/${TEST_RANDOM_NAME}" as String, venv.venvRootDir)
  }

  @Test
  void newObjectWithAbsolutePath() {
    helper.registerAllowedMethod('isUnix', []) { return true }

    VirtualEnv venv = new VirtualEnv(script, 1)

    // Expect that the dirname of the python installation is stripped from the
    // virtualenv directory, but that it still retains the correct python version.
    assertFalse(venv.venvRootDir.contains('usr/bin'))
    assertTrue(venv.venvRootDir.endsWith(TEST_RANDOM_NAME))
  }

  @Test
  void newObjectWithAbsolutePathWindows() {
    helper.registerAllowedMethod('isUnix', []) { return false }

    VirtualEnv venv = new VirtualEnv(script, 1)

    assertFalse(venv.venvRootDir.startsWith('/c'))
    assertTrue(venv.venvRootDir.endsWith(TEST_RANDOM_NAME))
  }

  @Test
  void newObjectWithNullScript() {
    boolean exceptionThrown = false
    try {
      new VirtualEnv(null)
    } catch (AssertionError error) {
      exceptionThrown = true
      assertNotNull(error)
    }
    assertTrue(exceptionThrown)
  }

  @Test
  void randomName() {
    assertEquals('58734446', VirtualEnv.randomName(1))
  }

  @Test
  void run() {
    String mockScriptCall = '''
      . /workspace/.venv/python/bin/activate
      mock-script
    '''
    helper.addShMock(mockScriptCall, 'mock output', 0)
    helper.registerAllowedMethod('isUnix', []) { return true }

    new VirtualEnv(script, 1).run('mock-script')

    assertEquals(1, helper.callStack.findAll { call ->
      call.methodName == 'sh'
    }.size())
  }

  @Test
  void runWithMap() {
    String mockScriptCall = '''
      . /workspace/.venv/python/bin/activate
      mock-script
    '''
    helper.addShMock(mockScriptCall, 'mock output', 0)
    helper.registerAllowedMethod('isUnix', []) { return true }

    new VirtualEnv(script, 1).run(script: 'mock-script')

    assertEquals(1, helper.callStack.findAll { call ->
      call.methodName == 'sh'
    }.size())
  }

  @Test
  void runWithMapReturnStatus() {
    helper.addShMock('mock-script', 'mock output', 1234)
    helper.registerAllowedMethod('isUnix', []) { return true }

    int result = new VirtualEnv(script, 1).run(script: 'mock-script', returnStatus: true)

    assertEquals(1, helper.callStack.findAll { call ->
      call.methodName == 'sh'
    }.size())
    assertEquals(1234, result)
  }

  @Test
  void runWithMapReturnStdout() {
    helper.addShMock('mock-script', 'mock output', 0)
    helper.registerAllowedMethod('isUnix', []) { return true }

    String result = new VirtualEnv(script, 1)
      .run(script: 'mock-script', returnStdout: true)

    assertEquals(1, helper.callStack.findAll { call ->
      call.methodName == 'sh'
    }.size())
    assertEquals('mock output', result)
  }
}
