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
   * Start of the line setting the Python package version.
   */
  String defaultVersionLineBeginning = "__version__ = '"

  /**
   * End of the line setting the Python package version.
   */
  String defaultVersionLineEnding = "'"

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
   *                 {@code versionLineBeginning} (optional): Start of the line setting
   *                 the Python package version.
   *               </li>
   *               <li>
   *                 {@code versionLineEnding} (optional): End of the line setting the
   *                 Python package version.
   *               </li>
   *             </ul>
   * @return The extracted Python package version.
   */
  String readVersion(Map args) {
    assert script
    assert args.filename
    String versionLineBeginning = args.versionLineBeginning ?: defaultVersionLineBeginning
    String versionLineEnding = args.versionLineEnding ?: defaultVersionLineEnding

    String fileContents = script.readFile(args.filename).trim()

    String versionLine = fileContents.split('\n').find { line ->
      line.startsWith(versionLineBeginning) && line.endsWith(versionLineEnding)
    }

    if (versionLine) {
      // Extract the version string between the beginning and ending
      return versionLine[
        versionLineBeginning.length()..-(versionLineEnding.length() + 1)
      ]
    }

    script.error("No ${versionLineBeginning} found in ${args.filename}")
    return null
  }
}
