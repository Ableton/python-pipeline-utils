package com.ableton


/**
 * Provides functional mocks for some Jenkins functions, which is useful in combination
 * with the JenkinsPipelineUnit library.
 */
class JenkinsMocks {
  static Closure echo = { String message ->
    println message
  }

  static Closure isUnix = {
    return !System.properties['os.name'].toLowerCase().contains('windows')
  }

  static Closure pwd = { args ->
    return System.properties[args?.tmp ? 'java.io.tmpdir' : 'user.dir']
  }

  /**
   * Simple container for holding mock script output.
   * @see JenkinsMocks#addShMock
   */
  class MockScriptOutput {
    String stdout
    int exitValue

    MockScriptOutput(String stdout, int exitValue) {
      this.stdout = stdout
      this.exitValue = exitValue
    }
  }

  /** Holds configured mock output values for the `sh` command. */
  static Map<String, MockScriptOutput> mockScriptOutputs = [:]

  /**
   * Configure mock output for the `sh` command. This function should be called before
   * attempting to call `JenkinsMocks.sh()`.
   * @see JenkinsMocks#sh
   * @param script Script command to mock.
   * @param stdout Standard output text to return for the given command.
   * @param exitValue Exit value for the command.
   * @return
   */
  static void addShMock(String script, String stdout, int exitValue) {
    mockScriptOutputs[script] = new MockScriptOutput(null, stdout, exitValue)
  }

  @SuppressWarnings('Instanceof')
  static Closure sh = { args ->
    String script = null
    def returnStdout = false
    def returnStatus = false

    // The `sh` function can be called with either a string, or a map of key/value pairs.
    if (args instanceof String || args instanceof GString) {
      script = args
    } else if (args instanceof Map) {
      script = args['script']
      returnStatus = args['returnStatus'] ?: false
      returnStdout = args['returnStdout'] ?: false
      if (returnStatus && returnStdout) {
        throw new IllegalArgumentException(
          'returnStatus and returnStdout are mutually exclusive options')
      }
    }
    assert script

    MockScriptOutput output = mockScriptOutputs[script]
    if (!output) {
      throw new IllegalArgumentException('No mock output configured for script call ' +
        "'${script}', did you forget to call JenkinsMocks.addShMock()?")
    }
    if (!returnStdout) {
      println output.stdout
    }

    if (returnStdout) {
      return output.stdout
    } else if (returnStatus) {
      return output.exitValue
    }

    return output.exitValue == 0
  }
}
