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

  protected String activateSubDir

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
   * @param python Python version or absolute path to Python executable. On Windows, this
   *               should be a Cygwin-style path, but <strong>without the {@code .exe}
   *               extension</strong>, for example: {@code /c/Python27/python}
   * @see #create(Object, String)
   */
  @SuppressWarnings('MethodParameterTypeRequired')
  VirtualEnv(def script, String python) {
    assert script
    assert python

    this.script = script

    String tempDir = '/tmp'
    if (script.isUnix()) {
      activateSubDir = 'bin'
    } else {
      activateSubDir = 'Scripts'
      List tempDirParts = script.env.TEMP.split(':')
      tempDir = "/${tempDirParts[0]}${tempDirParts[1].replace('\\', '/')}"
    }

    this.destDir = "${tempDir}/${script.env.JOB_BASE_NAME}/${script.env.BUILD_NUMBER}/" +
      python.split('/').last()
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
      . ${destDir}/${activateSubDir}/activate
      ${command}
    """)
  }
}
