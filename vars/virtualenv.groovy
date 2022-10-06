import com.ableton.VirtualEnv


/**
 * Create a new {@link com.ableton.VirtualEnv} instance.
 * @param python Python version or absolute path to Python executable.
 * @return New {@link com.ableton.VirtualEnv} instance.
 */
VirtualEnv create(String python) {
  return VirtualEnv.create(this, python)
}
