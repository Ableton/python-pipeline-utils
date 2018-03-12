import com.ableton.PythonBuilder


/**
 * Download, build, and install a specific version of Python.
 * @param arguments Map of arguments. See the documentation for fields in the class
 *                  {@link com.ableton.PythonBuilder} for valid and required arguments.
 * @return Path to Python installation directory. The actual Python executable will be
 *         found in the {@code bin} subdirectory of this path.
 */
String install(Map arguments) {
  arguments['script'] = this
  return new PythonBuilder(arguments).install()
}
