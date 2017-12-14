def create(String python, String path = "venv", boolean installRequirements = true) {
  sh("virtualenv --python=${python} ${path}")
  if (installRequirements && fileExists("requirements.txt")) {
    run(path, "pip install -r requirements.txt")
  }
}


def run(String path = "venv", String command) {
  sh("""
    . ./${path}/bin/activate
    ${command}
  """)
}
