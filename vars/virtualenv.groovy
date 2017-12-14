import com.ableton.VirtualEnv


def create(script, String python) {
  venv = new VirtualEnv(script, python)
  venv.script.sh("virtualenv --python=${python} ${venv.destDir}")
  return venv
}
