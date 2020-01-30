package com.ableton

import static com.lesfurets.jenkins.unit.MethodCall.callArgsToString
import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertNotNull
import static org.junit.Assert.assertTrue

import com.lesfurets.jenkins.unit.BasePipelineTest
import org.junit.After
import org.junit.Before
import org.junit.Test


class PipenvTest extends BasePipelineTest {
  Object script

  @Override
  @Before
  void setUp() throws Exception {
    super.setUp()

    this.script = loadScript('test/resources/EmptyPipeline.groovy')
    assertNotNull(script)
    helper.registerAllowedMethod('sh', [String], JenkinsMocks.sh)

    JenkinsMocks.addShMock('pipenv --rm', '', 0)
  }

  @After
  void tearDown() {
    JenkinsMocks.clearStaticData()
  }

  @Test
  void runWith() throws Exception {
    List pythonVersions = ['2.7', '3.5']
    pythonVersions.each { python ->
      JenkinsMocks.addShMock("pipenv sync --dev --python ${python}", '', 0)
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

  @Test(expected = AssertionError)
  void runWithNoScript() throws Exception {
    new Pipenv().runWith(['2.7']) {}
  }

  @Test(expected = AssertionError)
  void runWithEmptyPythonVersions() throws Exception {
    new Pipenv(script: script).runWith([]) {}
  }
}
