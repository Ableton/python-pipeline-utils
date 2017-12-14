def create(String python, String destDir, boolean installRequirements = true) {
  sh("virtualenv --python=${python} ${destDir}")
  if (installRequirements && fileExists("requirements.txt")) {
    run(destDir, "pip install -r requirements.txt")
  }
}


def run(String destDir, String command) {
  sh("""
    . ./${destDir}/bin/activate
    ${command}
  """)
}
