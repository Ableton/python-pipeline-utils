package com.ableton


class VirtualEnv implements Serializable {
  def script
  String destDir


  VirtualEnv(script, String python) {
    this.script = script
    final PATH_SEP = script.isUnix() ? "/" : "\\"
    this.destDir = script.pwd(tmp:true) +
      PATH_SEP +
      script.env.BUILD_NUMBER +
      PATH_SEP +
      python
  }


  def run(String command) {
    script.sh("""
      . ${destDir}/bin/activate
      ${command}
    """)
  }
}
