package com.ableton


/**
 * Uses pyenv (which must be installed on the node) to install a specific Python version
 * and create a virtualenv for it.
 *
 * @see com.ableton.VirtualEnv
 */
class Pyenv implements Serializable {
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
  static VirtualEnv createVirtualEnv(Object script, String python, String pyenvRoot) {
    if (!script.isUnix()) {
      script.error 'This method is not supported on Windows'
    }

    VirtualEnv venv = new VirtualEnv(script)
    assert pyenvRoot

    if (!script.fileExists(pyenvRoot)) {
      script.error "pyenv root path '${pyenvRoot}' does not exist"
    }

    venv.script.sh(
      label: "Install Python version ${python} with pyenv",
      script: """
        export PYENV_ROOT=${pyenvRoot}
        export PATH=\$PYENV_ROOT/bin:\$PATH
        eval "\$(pyenv init --path)"
        eval "\$(pyenv init -)"
        pyenv install --skip-existing ${python}
        pyenv shell ${python}
        pip install virtualenv
        virtualenv ${venv.venvRootDir}
      """,
    )

    return venv
  }
}
