import com.ableton.Pyenv
import com.ableton.VirtualEnv


/**
 * Create a new {@link com.ableton.VirtualEnv} instance using pyenv.
 * @param python Python version, as given by pyenv versions --list.
 * @param pyenvRoot Path of the pyenv installation. If {@code null}, then the pyenv root
 *        will be detected from the environment in {@code PYENV_ROOT}.
 * @return New {@link com.ableton.VirtualEnv} instance.
 */
VirtualEnv createVirtualEnv(String python, String pyenvRoot = null) {
  return Pyenv.createVirtualEnv(this, python, pyenvRoot ?: env.PYENV_ROOT)
}
