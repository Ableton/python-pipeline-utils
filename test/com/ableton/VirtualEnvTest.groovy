package com.ableton

import static org.junit.jupiter.api.Assertions.assertEquals
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
  void create() {
    String python = 'python2.7'

    VirtualEnv venv = new VirtualEnv(script, 1)

    helper.addShMock("virtualenv --python=${python} ${venv.venvRootDir}", '', 0)
    VirtualEnv createdVenv = VirtualEnv.create(script, python, 1)
    assertEquals(venv.venvRootDir, createdVenv.venvRootDir)
  }

  @Test
  void createWithWindowsPath() {
    script.env.OS = 'Windows_NT'
    script.env.WORKSPACE = 'C:\\workspace'

    VirtualEnv.create(script, 'C:\\Python27\\python.exe', 1)

    String expected =
      "virtualenv --python=C:/Python27/python.exe C:/workspace/.venv/${TEST_RANDOM_NAME}"
    assertEquals(
      expected, helper.callStack.find { call -> call.methodName == 'sh' }.args[0].script
    )
  }

  @Test
  void inside() {
    Map insideEnv

    new VirtualEnv(script, 1).inside { insideEnv = binding.getVariable('env') }

    assertTrue(insideEnv.keySet().contains('PATH+VENVBIN'))
    assertEquals(
      "/workspace/.venv/${TEST_RANDOM_NAME}/bin" as String, insideEnv['PATH+VENVBIN']
    )
    assertTrue(insideEnv.keySet().contains('VIRTUAL_ENV'))
    assertEquals(
      "/workspace/.venv/${TEST_RANDOM_NAME}" as String, insideEnv['VIRTUAL_ENV']
    )
  }

  @Test
  void newObjectUnix() {
    VirtualEnv venv = new VirtualEnv(script, 1)

    assertNotNull(venv)
    assertNotNull(venv.script)
    assertNotNull(venv.venvRootDir)
    assertEquals("/workspace/.venv/${TEST_RANDOM_NAME}" as String, venv.venvRootDir)
  }

  @Test
  void newObjectWindows() {
    script.env.OS = 'Windows_NT'
    script.env.WORKSPACE = 'C:\\workspace'

    VirtualEnv venv = new VirtualEnv(script, 1)

    assertNotNull(venv)
    assertNotNull(venv.script)
    assertNotNull(venv.venvRootDir)
    assertEquals("C:/workspace/.venv/${TEST_RANDOM_NAME}" as String, venv.venvRootDir)
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

    new VirtualEnv(script, 1).run(script: 'mock-script')

    assertEquals(1, helper.callStack.findAll { call ->
      call.methodName == 'sh'
    }.size())
  }

  @Test
  void runWithMapReturnStatus() {
    helper.addShMock('mock-script', 'mock output', 1234)

    int result = new VirtualEnv(script, 1).run(script: 'mock-script', returnStatus: true)

    assertEquals(1, helper.callStack.findAll { call ->
      call.methodName == 'sh'
    }.size())
    assertEquals(1234, result)
  }

  @Test
  void runWithMapReturnStdout() {
    helper.addShMock('mock-script', 'mock output', 0)

    String result = new VirtualEnv(script, 1)
      .run(script: 'mock-script', returnStdout: true)

    assertEquals(1, helper.callStack.findAll { call ->
      call.methodName == 'sh'
    }.size())
    assertEquals('mock output', result)
  }
}
