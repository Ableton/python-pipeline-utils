import com.ableton.PythonPackage


/**
 * Read the version of a Python package.
 * @param arguments Map of arguments. See the documentation for the
 *                  {@link com.ableton.PythonPackage#readVersion} method for valid and
 *                  required arguments.
 * @return The extracted Python package version.
 */
String readVersion(Map arguments) {
  return new PythonPackage(script: this).readVersion(arguments)
}
