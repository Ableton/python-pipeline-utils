import com.ableton.VirtualEnv


/**
 * Create a new {@link com.ableton.VirtualEnv} instance.
 * @param python Python version or absolute path to Python executable.
 * @return New {@link com.ableton.VirtualEnv} instance.
 */
VirtualEnv create(String python) {
  return VirtualEnv.create(this, python)
}


/**
 * Create a new {@link com.ableton.VirtualEnv} instance using pyenv.
 * @param python Python version, as given by pyenv versions --list.
 * @param pyenvRoot Path of the pyenv installation. If {@code null}, then the pyenv root
 *        will be detected from the environment in {@code PYENV_ROOT}.
 * @return New {@link com.ableton.VirtualEnv} instance.
 */
VirtualEnv createWithPyenv(String python, String pyenvRoot = null) {
  return VirtualEnv.create(this, python, pyenvRoot ?: env.PYENV_ROOT)
}
