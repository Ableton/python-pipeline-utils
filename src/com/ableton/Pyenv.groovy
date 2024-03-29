package com.ableton


/**
 * Uses pyenv (which must be installed on the node) to install a specific Python version
 * and create a virtualenv for it.
 *
 * @see com.ableton.VirtualEnv
 */
class Pyenv implements Serializable {
  /**
   * Script context.
   * <strong>Required value, may not be null!</strong>
   */
  Object script
  /**
   * Pyenv root directory.
   * <strong>Required value, may not be null!</strong>
   */
  String pyenvRoot

  Pyenv(Object script, String pyenvRoot) {
    this.script = script
    this.pyenvRoot = pyenvRoot
  }

  /**
   * Create a virtualenv using a specific version of Python, installed via pyenv. pyenv
   * should be installed on the node, but the actual setup of any required environment
   * variables (e.g. PYENV_ROOT and PATH) will be done inside this function.
   *
   * @param script Script context.
   *               <strong>Required value, may not be null!</strong>
   * @param pythonVersion Python version, as given by pyenv versions --list.
   * @return New instance of VirtualEnv object.
   */
  VirtualEnv createVirtualEnv(String pythonVersion, long randomSeed = 0) {
    assert script
    assert pythonVersion
    assertPyenvRoot()

    if (!script.isUnix()) {
      script.error 'This method is not supported on Windows'
    }

    VirtualEnv venv = new VirtualEnv(script, randomSeed)
    int result = venv.script.sh(
      label: "Install Python version ${pythonVersion} with pyenv",
      returnStatus: true,
      script: """
        export PYENV_ROOT=${pyenvRoot}
        export PATH=\$PYENV_ROOT/bin:\$PATH
        eval "\$(pyenv init --path)"
        eval "\$(pyenv init -)"
        pyenv install --skip-existing ${pythonVersion}
        pyenv shell ${pythonVersion}
        pip install virtualenv
        virtualenv ${venv.venvRootDir}
      """,
    )

    if (result != 0) {
      // If we failed to create the virtualenv, test to see if the requested Python
      // version is supported. We don't do this pre-emptively to spare some work in the
      // pipeline, but if the operation failed, it is nice to fail with a clear error.
      if (versionSupported(pythonVersion)) {
        script.error "Installation of Python ${pythonVersion} failed with code ${result}"
      } else {
        script.withEnv(["PYENV_ROOT=${pyenvRoot}"]) {
          String pyenvVersion = script.sh(
            label: 'Get Pyenv version on the node',
            returnStdout: true,
            script: "${pyenvRoot}/bin/pyenv --version",
          ).trim()
          script.error "The installed version of Pyenv (${pyenvVersion}) does not " +
            "support Python version ${pythonVersion}"
        }
      }
    }

    return venv
  }

  /**
   * Check if a given Python version is supported by the installed pyenv version.
   *
   * @param pythonVersion Python version.
   * @return True if the Python version is available, false otherwise.
   */
  boolean versionSupported(String pythonVersion) {
    assert script
    assert pythonVersion
    assertPyenvRoot()
    boolean result = false

    if (!script.isUnix()) {
      script.error 'This method is not supported on Windows'
    }

    script.withEnv(["PYENV_ROOT=${pyenvRoot}"]) {
      String allVersions = script.sh(
        label: 'Get Python versions supported by Pyenv',
        returnStdout: true,
        script: "${pyenvRoot}/bin/pyenv install --list",
      )

      allVersions.split('\n').each { version ->
        if (version.trim() == pythonVersion) {
          result = true
        }
      }
    }

    return result
  }

  protected void assertPyenvRoot() {
    assert pyenvRoot

    if (!script.fileExists(pyenvRoot)) {
      script.error "pyenv root path '${pyenvRoot}' does not exist"
    }
  }
}
