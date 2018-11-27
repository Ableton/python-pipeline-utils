package com.ableton


/**
 * Provides an easy way to run a command with Pipenv using multiple Python versions.
 */
class Pipenv implements Serializable {
  /**
   * Script context.
   * <strong>Required value, may not be null!</strong>
   */
  @SuppressWarnings('FieldTypeRequired')
  def script

  /**
   * Run a closure with Pipenv using multiple versions of Python. Because the virtualenv
   * created by Pipenv must be wiped out between runs, this function cannot be
   * parallelized and therefore the commands are run serially for each Python version.
   *
   * This function also installs the development packages for each Python version (in
   * other words, it runs {@code pipenv install --dev}. Also, it removes the virtualenv
   * after the last Python version has been run.
   *
   * @param pythonVersions List of Python versions to run the command with. This argument
   *                       is passed to Pipenv via {@code pipenv --python}. See
   *                       {@code pipenv --help} for supported syntax.
   * @param body Closure body to execute. The closure body is passed the Python version
   *             as an argument.
   * @return Map of return values. The keys in the map correspond to the Python versions
   *         given in {@code args.pythonVersions}, and the values are the results of
   *         executing the closure body for each version.
   */
  Map runWith(List pythonVersions, Closure body) {
    assert script
    assert pythonVersions

    Map results = [:]

    try {
      pythonVersions.each { python ->
        script.sh "pipenv install --dev --python ${python}"
        results[python] = body(python)
      }
    } finally {
      try {
        script.sh 'pipenv --rm'
      } catch (ignored) {}
    }

    return results
  }
}
