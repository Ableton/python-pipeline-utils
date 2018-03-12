package com.ableton


/**
 * Provides a minimal wrapper around a Python Virtualenv environment. The Virtualenv is
 * stored in the job's temporary directory and is unique for each build number.
 */
class VirtualEnv implements Serializable {
  /**
   * Script context.
   * <strong>Required value, may not be nulL!</strong>
   */
  @SuppressWarnings('FieldTypeRequired')
  def script
  /**
   * Destination directory for the virtualenv. This value is set during construction of
   * the object, and is under the job's temporary working directory.
   */
  String destDir

  /**
   * Construct a new instance of this class. This method <strong>does not</strong>
   * initialize the environment by running {@code virtualenv}. Use the factory method
   * {@link #create(Object, String)} instead.
   * @param script Script context.
   *               <strong>Required value, may not be nulL!</strong>
   * @param python Python version or absolute path to Python executable.
   * @see #create(Object, String)
   */
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

  /**
   * Factory method to create new class instance and properly initialize it.
   * @param script Script context.
   *               <strong>Required value, may not be nulL!</strong>
   * @param python Python version or absolute path to Python executable.
   * @return New instance of VirtualEnv object.
   */
  @SuppressWarnings('MethodParameterTypeRequired')
  static VirtualEnv create(def script, String python) {
    VirtualEnv venv = new VirtualEnv(script, python)
    venv.script.sh("virtualenv --python=${python} ${venv.destDir}")
    return venv
  }

  /**
   * Run a command in the virtualenv.
   * @param command Command to run.
   */
  void run(String command) {
    script.sh("""
      . ${destDir}/bin/activate
      ${command}
    """)
  }
}
