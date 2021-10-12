package com.ableton


/**
 * Class to interact with local Python packages.
 */
class PythonPackage implements Serializable {
  /**
   * Script context.
   * <strong>Required value, may not be null!</strong>
   */
  Object script = null

  /**
   * Read a Python file setting the version of a package (usually {@code __init__.py})
   * and return its version as a {@code String}.
   * @param args Map of arguments. Valid arguments include:
   *             <ul>
   *               </li>
   *                 {@code filename} (required): The file to read and parse the version
   *                 out of.
   *               </li>
   *               <li>
   *                 {@code versionVariableName} (optional): Name of the Python variable
   *                 for the version. Defaults to {@code __version__}.
   *               </li>
   *             </ul>
   * @return The extracted Python package version.
   */
  String readVersion(Map args) {
    assert script
    assert args.filename
    String versionVariableName = args.versionVariableName ?: '__version__'

    String fileContents = script.readFile(args.filename).trim()

    Object matcher = (fileContents =~ /${versionVariableName}\s*=\s*['"](\S+)['"]/)
    if (matcher.find()) {
      return matcher.group(1)
    }

    script.error("No ${versionVariableName} found in ${args.filename}")
    return null
  }
}
