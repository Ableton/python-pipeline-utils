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
  Object script
  /**
   * Destination directory for the virtualenv. This value is set during construction of
   * the object, and is under the system temporary directory.
   */
  String destDir = null
  /**
   * Series of commands needed to activate a virtualenv inside the
   * current shell.
   */
  String activateCommands = null

  protected String activateSubDir = null

  /**
   * Create a virtualenv using a specific version of Python, installed via pyenv. pyenv
   * should be installed on the node, but the actual setup of any required environment
   * variables (e.g. PYENV_ROOT and PATH) will be done inside this function.
   *
   * @param script Script context.
   *               <strong>Required value, may not be null!</strong>
   * @param python Python version, as given by pyenv versions --list.
   * @param pyenvRoot Path to the installation of pyenv.
   * @return New instance of VirtualEnv object.
   */
  static VirtualEnv create(Object script, String python, String pyenvRoot) {
    if (!script.isUnix()) {
      script.error 'This method is not supported on Windows'
    }

    VirtualEnv venv = new VirtualEnv(script, python)
    assert pyenvRoot

    if (!script.fileExists(pyenvRoot)) {
      script.error "pyenv root path '${pyenvRoot}' does not exist"
    }

    venv.activateCommands = """
      export PYENV_ROOT=${pyenvRoot}
      export PATH=\$PYENV_ROOT/bin:\$PATH
      eval "\$(pyenv init -)"
    """

    venv.script.sh("""
      ${venv.activateCommands}
      pyenv install --skip-existing ${python}
      pyenv shell ${python}
      pip install virtualenv
      virtualenv ${venv.destDir}
    """)

    venv.activateCommands += ". ${venv.destDir}/${venv.activateSubDir}/activate"

    return venv
  }

  /**
   * Create a virtualenv using a specific locally installed version of Python.
   *
   * @param script Script context.
   *               <strong>Required value, may not be null!</strong>
   * @param python Python version or absolute path to Python executable.
   * @return New instance of VirtualEnv object.
   */
  static VirtualEnv create(Object script, String python) {
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
  VirtualEnv(Object script, String python) {
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

    this.activateCommands = ". ${destDir}/${activateSubDir}/activate"
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
   * @param arguments Arguments to pass to the {@code sh} command. See the documentation
   *        for the {@code sh} step for valid arguments.
   */
  @SuppressWarnings('MethodReturnTypeRequired')
  def run(Map arguments) {
    assert arguments
    assert arguments.containsKey('script')

    String scriptCommand = """
      ${this.activateCommands}
      ${arguments.script}
    """
    // We shouldn't modify the original arguments map
    Map newArguments = arguments.clone()
    // Replace the original `script` command with the venv-activated one
    newArguments['script'] = scriptCommand
    return script.sh(newArguments)
  }

  /**
   * Run a command in the virtualenv.
   *
   * @param command Command to run.
   */
  void run(String command) {
    script.sh("""
      ${this.activateCommands}
      ${command}
    """)
  }
}
