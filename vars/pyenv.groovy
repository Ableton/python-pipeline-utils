import com.ableton.Pyenv
import com.ableton.VirtualEnv


/**
 * Create a new {@link com.ableton.VirtualEnv} instance using pyenv.
 * @param pythonVersion Python version, as given by pyenv versions --list.
 * @param pyenvRoot Path of the pyenv installation. If {@code null}, then the pyenv root
 *        will be detected from the environment in {@code PYENV_ROOT}.
 * @return New {@link com.ableton.VirtualEnv} instance.
 */
VirtualEnv createVirtualEnv(String pythonVersion, String pyenvRoot = null) {
  return new Pyenv(this, pyenvRoot ?: env.PYENV_ROOT).createVirtualEnv(pythonVersion)
}


/**
 * Check if a given Python version is supported by the installed pyenv version.
 *
 * @param pythonVersion Python version.
 * @return True if the Python version is available, false otherwise.
 */
boolean versionSupported(String pythonVersion, String pyenvRoot = null) {
  return new Pyenv(this, pyenvRoot ?: env.PYENV_ROOT).versionSupported(pythonVersion)
}
