package com.ableton

import static com.lesfurets.jenkins.unit.MethodCall.callArgsToString
import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertNotNull
import static org.junit.jupiter.api.Assertions.assertThrows
import static org.junit.jupiter.api.Assertions.assertTrue

import com.lesfurets.jenkins.unit.BasePipelineTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test


class PipenvTest extends BasePipelineTest {
  Object script

  @Override
  @BeforeEach
  void setUp() throws Exception {
    super.setUp()

    this.script = loadScript('test/resources/EmptyPipeline.groovy')
    assertNotNull(script)

    helper.addShMock('pipenv --rm', '', 0)
  }

  @Test
  void runWith() throws Exception {
    List pythonVersions = ['2.7', '3.5']
    pythonVersions.each { python ->
      helper.addShMock("pipenv sync --dev --python ${python}", '', 0)
    }

    int numCalls = 0
    Map result = new Pipenv(script: script).runWith(pythonVersions) { p ->
      numCalls++
      return p
    }

    // Ensure that pipenv sync was called for each Python version
    pythonVersions.each { python ->
      assertTrue(helper.callStack.findAll { call ->
        call.methodName == 'sh'
      }.any { call ->
        callArgsToString(call).contains("pipenv sync --dev --python ${python}")
      })
    }

    // Ensure that the closure body was evaluated for each Python version
    assertEquals(pythonVersions.size(), numCalls)
    pythonVersions.each { python ->
      assert result[python]
      assertEquals(python, result[python])
    }

    // Ensure that pipenv --rm was called
    assertTrue(helper.callStack.findAll { call ->
      call.methodName == 'sh'
    }.any { call ->
      callArgsToString(call).contains('pipenv --rm')
    })
  }

  @Test
  void runWithNoScript() throws Exception {
    assertThrows(AssertionError) { new Pipenv().runWith(['2.7']) {} }
  }

  @Test
  void runWithEmptyPythonVersions() throws Exception {
    assertThrows(AssertionError) { new Pipenv(script: script).runWith([]) {} }
  }
}
