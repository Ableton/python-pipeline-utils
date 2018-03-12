package com.ableton


/**
 * Wrapper class which manages a list of {@link com.ableton.VirtualEnv} objects. This can
 * be convenient when running tests against multiple Python versions.
 */
class VirtualEnvList implements Serializable {
  /**
   * Script context.
   * <strong>Required value, may not be nulL!</strong>
   */
  @SuppressWarnings('FieldTypeRequired')
  def script
  /**
   * List of {@link com.ableton.VirtualEnv} objects.
   * @see #add(String)
   */
  List<VirtualEnv> venvs = []

  /**
   * Create a new {@link com.ableton.VirtualEnv} and add it to the list.
   * @param python Python version or absolute path to Python executable.
   * @see com.ableton.VirtualEnv#create(Object, String)
   */
  void add(String python) {
    venvs.add(VirtualEnv.create(script, python))
  }

  /**
   * Run the given {@code command} in all VirtualEnv instances.
   * @param command Command to run.
   */
  void run(String command) {
    venvs.each { venv ->
      venv.run(command)
    }
  }
}
