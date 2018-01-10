package com.ableton


/**
 * Provides functional mocks for some Jenkins functions, which is useful in combination
 * with the JenkinsPipelineUnit library.
 */
class JenkinsMocks {
  static Closure isUnix = {
    return !System.properties['os.name'].toLowerCase().contains('windows')
  }

  static Closure pwd = { args ->
    return System.properties[args?.tmp ? 'java.io.tmpdir' : 'user.dir']
  }
}
