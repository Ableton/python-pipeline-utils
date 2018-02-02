import com.ableton.VirtualEnv


@SuppressWarnings('MethodParameterTypeRequired')
VirtualEnv create(String python) {
  echo "WE AREEEE: ${this.getClass().toString()}"
  return VirtualEnv.create(this, python)
}
