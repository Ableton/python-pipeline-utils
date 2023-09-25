package com.ableton


/**
 * Uses pyenv (which must be installed on the node) to install a specific Python version
 * and create a virtualenv for it.
 *
 * @see com.ableton.VirtualEnv
 */
class Pyenv implements Serializable {
  static private final int INSTALLATION_RETRIES = 3

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
    this.pyenvRoot = script.env.OS == 'Windows_NT' ?
      pyenvRoot.replace('\\', '/') : pyenvRoot
  }

  /**
   * Create a virtualenv using a specific version of Python, installed via pyenv. pyenv
   * should be installed on the node, but the actual setup of any required environment
   * variables (e.g. PYENV_ROOT and PATH) will be done inside this function.
   * The Python installation will be retried up to three times.
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

    String trimmedPythonVersion = pythonVersion.trim()

    if (!versionSupported(trimmedPythonVersion)) {
      script.withEnv(["PYENV_ROOT=${pyenvRoot}"]) {
        String pyenvVersion = script.sh(
          label: 'Get Pyenv version on the node',
          returnStdout: true,
          script: "${pyenvRoot}/bin/pyenv --version",
        ).trim()
        script.error "The installed version of Pyenv (${pyenvVersion}) does not " +
          "support Python version ${trimmedPythonVersion}"
      }
    }

    VirtualEnv venv = new VirtualEnv(script, randomSeed)
    script.retry(INSTALLATION_RETRIES) {
      script.withEnv(["PYENV_VERSION=${trimmedPythonVersion}"]) {
        List installCommands = ["export PYENV_ROOT=${pyenvRoot}"]
        if (script.env.OS != 'Windows_NT') {
          installCommands += [
            "export PATH=\$PYENV_ROOT/bin:\$PATH",
            'eval "\$(pyenv init --path)"',
            'eval "\$(pyenv init -)"',
          ]
        } else {
          String posixPyenvRoot = pyenvRoot[1] == ':' ?
            "/${pyenvRoot[0].toLowerCase()}/${pyenvRoot.substring(3)}" : pyenvRoot
          installCommands.add(
            "export PATH=${posixPyenvRoot}/shims:${posixPyenvRoot}/bin:\$PATH"
          )
        }
        installCommands += [
          "pyenv install --skip-existing ${trimmedPythonVersion}",
          'pyenv exec pip install virtualenv',
          "pyenv exec virtualenv ${venv.venvRootDir}",
        ]
        venv.script.sh(
          label: "Install Python version ${trimmedPythonVersion} with pyenv",
          script: installCommands.join('\n') + '\n',
        )
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
