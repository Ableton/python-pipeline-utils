import com.ableton.VirtualEnvList


VirtualEnvList create(List pythonVersions) {
  VirtualEnvList venvs = new VirtualEnvList(script: this)
  pythonVersions.each { python ->
    venvs.add(python)
  }
  return venvs
}
