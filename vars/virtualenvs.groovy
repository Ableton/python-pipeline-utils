import com.ableton.VirtualEnvList


/**
 * Create several {@link com.ableton.VirtualEnv} instances, one for each Python version.
 * @param pythonVersions List of Python versions.
 * @return New {@link com.ableton.VirtualEnvList} instance.
 */
VirtualEnvList create(List pythonVersions) {
  VirtualEnvList venvs = new VirtualEnvList(script: this)
  pythonVersions.each { python ->
    venvs.add(python)
  }
  return venvs
}
