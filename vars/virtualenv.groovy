def create(String python, String path = "venv", boolean installRequirements = true) {
  sh("virtualenv -p ${python} ${path}")
  if (installRequirements && fileExists("requirements.txt")) {
    run(path:path, command:"pip install -r requirements.txt")
  }
}


def run(String path = "venv", String command) {
  sh("""
    . ./${path}/bin/activate
    ${command}
  """)
}