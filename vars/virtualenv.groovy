def create(String python, String destDir) {
  sh("virtualenv --python=${python} ${destDir}")
}


def installRequirements(String destDir) {
  run(destDir, "pip install -r requirements.txt")
}


def run(String destDir, String command) {
  sh("""
    . ./${destDir}/bin/activate
    ${command}
  """)
}
