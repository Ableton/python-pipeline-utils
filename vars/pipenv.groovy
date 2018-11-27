import com.ableton.Pipenv


/**
 * Run a closure with Pipenv using multiple versions of Python.
 * @param pythonVersions List of Python versions.
 * @param body Closure body to execute.
 * @return Map with return output or values for each Python version.
 * @see com.ableton.Pipenv#runWith(List, Closure)
 */
Map runWith(List pythonVersions, Closure body) {
  return new Pipenv(script: this).runWith(pythonVersions, body)
}
