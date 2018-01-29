package com.ableton


/**
 * Provides a minimal wrapper around a Python Virtualenv environment. The Virtualenv is
 * stored in the job's temporary directory and is unique for each build number.
 */
class VirtualEnv implements Serializable {
  @SuppressWarnings('FieldTypeRequired')
  def script
  String destDir

  @SuppressWarnings('MethodParameterTypeRequired')
  VirtualEnv(def script, String python) {
    assert script
    assert python

    this.script = script

    String pathSep = script.isUnix() ? '/' : '\\'
    this.destDir = script.pwd(tmp: true) +
      pathSep +
      script.env.BUILD_NUMBER +
      pathSep +
      python.split(pathSep).last()
  }

  @SuppressWarnings('MethodParameterTypeRequired')
  static VirtualEnv create(def script, String python) {
    VirtualEnv venv = new VirtualEnv(script, python)
    venv.script.sh("virtualenv --python=${python} ${venv.destDir}")
    return venv
  }

  void run(String command) {
    script.sh("""
      . ${destDir}/bin/activate
      ${command}
    """)
  }
}
