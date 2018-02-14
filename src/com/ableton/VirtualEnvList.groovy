package com.ableton


class VirtualEnvList implements Serializable {
  @SuppressWarnings('FieldTypeRequired')
  def script
  List<VirtualEnv> venvs = []

  void add(String python) {
    venvs.add(VirtualEnv.create(script, python))
  }

  void run(String command) {
    venvs.each { venv ->
      venv.run(command)
    }
  }
}
