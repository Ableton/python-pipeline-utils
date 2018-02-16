package com.ableton


class PythonBuilder implements Serializable {
  @SuppressWarnings('FieldTypeRequired')
  def script = null
  /**
   * Python major version to download (required value). Example: '3.7.0'
   */
  String version = null

  /**
   * Specifies the path for the ccache executable, which will be prepended to the PATH
   * when building Python.
   */
  String ccachePath = null
  /**
   * Mirror to use when downloading Python.
   */
  String downloadMirror = 'https://www.python.org/ftp/python'
  /**
   * Method to use to download Python. Currently supported methods include:
   * - 'curl': Requires curl to be installed on the build node.
   * - 'httpRequest': Requires the Jenkins HTTP Request Plugin to be installed (default).
   */
  String downloadWith = 'httpRequest'
  /**
   * Number of parallel jobs to use when building Python (ie, `make -jN`). Default: '1'
   */
  String makeJobs = '1'
  /**
   * Python version revision, optional. This value is appended to the major version, but
   * because of the way that Python's downloads are organized, this value must be separate
   * from the major version. So for Python 3.7.0a4, use '3.7.0' for the version and 'a4'
   * for the revision.
   */
  String revision = ''

  String install() {
    // Sanity check required values
    assert script
    assert version

    if (!script.isUnix()) {
      throw new UnsupportedOperationException('This function is only supported on Unix')
    }

    String sourcesTarball = download()
    String sourcesPath = extract(sourcesTarball)
    return build(sourcesPath)
  }

  protected String pythonBaseName() {
    return 'Python-' + version + revision
  }

  protected String tempDir() {
    return script.pwd(tmp: true)
  }

  protected String buildDir() {
    return tempDir() + '/python-build'
  }

  protected String download() {
    String downloadUrl = downloadMirror + '/' + version + '/' + pythonBaseName() + '.tgz'
    String outputFile = buildDir() + '/' + pythonBaseName() + '.tgz'
    script.echo 'Downloading python from: ' + downloadUrl

    script.dir(buildDir()) {
      switch (downloadWith) {
        case 'curl':
          script.sh "curl -s -o ${outputFile} ${downloadUrl}"
          break
        case 'httpRequest':
          script.httpRequest(outputFile: outputFile, url: downloadUrl)
          break
        default:
          script.error 'Unknown downloadWith type: ' + downloadWith
      }
    }

    return outputFile
  }

  protected String extract(String sourcesTarball) {
    script.echo 'Extracting python sources'
    script.dir(buildDir()) {
      script.sh 'tar xfz ' + sourcesTarball
    }
    return buildDir() + '/' + pythonBaseName()
  }

  protected String build(String sourcesPath) {
    // Ensure that makeJobs is not an empty string, since make is a bit dumb and running
    // `make -j` will spawn one job for every file, potentially forkbombing the node.
    if (!makeJobs || !makeJobs.toInteger()) {
      throw new IllegalArgumentException('Invalid argument for makeJobs: ' + makeJobs)
    }
    script.echo 'Building python from: ' + sourcesPath

    String installPath = tempDir() + '/' + pythonBaseName()
    script.dir(sourcesPath) {
      script.withEnv(buildEnv()) {
        script.sh './configure --prefix=' + installPath
        script.sh 'make -j' + makeJobs
        script.sh 'make install'
      }
      script.deleteDir()
    }
    return installPath
  }

  protected List<String> buildEnv() {
    return ccachePath ? ["PATH=${ccachePath}:${script.env.PATH}"] : []
  }
}
