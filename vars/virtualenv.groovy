import com.ableton.VirtualEnv


@SuppressWarnings('MethodParameterTypeRequired')
VirtualEnv create(def script, String python) {
  return VirtualEnv.create(script, python)
}
