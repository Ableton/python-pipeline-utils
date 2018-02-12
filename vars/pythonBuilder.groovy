import com.ableton.PythonBuilder


String install(Map arguments) {
  arguments['script'] = this
  return new PythonBuilder(arguments).install()
}
