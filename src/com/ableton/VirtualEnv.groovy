package com.ableton


/**
 * Provides a minimal wrapper around a Python Virtualenv environment. The Virtualenv is
 * stored under the system temporary directory is unique for each build number.
 */
class VirtualEnv implements Serializable {
  /**
   * Script context.
   * <strong>Required value, may not be null!</strong>
   */
  @SuppressWarnings('FieldTypeRequired')
  def script
  /**
   * Destination directory for the virtualenv. This value is set during construction of
   * the object, and is under the system temporary directory.
   */
  String destDir

  /**
   * Factory method to create new class instance and properly initialize it.
   *
   * @param script Script context.
   *               <strong>Required value, may not be null!</strong>
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
   * Construct a new instance of this class. This method <strong>does not</strong>
   * initialize the environment by running {@code virtualenv}. Use the factory method
   * {@link #create(Object, String)} instead.
   *
   * @param script Script context.
   *               <strong>Required value, may not be null!</strong>
   * @param python Python version or absolute path to Python executable.
   * @see #create(Object, String)
   */
  @SuppressWarnings('MethodParameterTypeRequired')
  VirtualEnv(def script, String python) {
    assert script
    assert python

    this.script = script

    String pathSep = script.isUnix() ? '/' : '\\\\'
    String tempDir = script.isUnix() ? '/tmp' : script.env.TEMP
    this.destDir = tempDir +
      pathSep +
      script.env.JOB_BASE_NAME +
      pathSep +
      script.env.BUILD_NUMBER +
      pathSep +
      python.split(pathSep).last()
  }

  /**
   * Removes the Virtualenv from disk. You should call this method in the cleanup stage
   * of the pipeline to avoid cluttering the build node with temporary files.
   */
  void cleanup() {
    script.dir(destDir) {
      script.deleteDir()
    }
  }

  /**
   * Run a command in the virtualenv.
   *
   * @param command Command to run.
   */
  void run(String command) {
    script.sh("""
      . ${destDir}/bin/activate
      ${command}
    """)
  }
}
