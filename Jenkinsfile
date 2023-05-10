library(identifier: 'ableton-utils@0.23', changelog: false)
library(identifier: 'groovylint@0.13', changelog: false)
// Get python-utils library from current commit so it can test itself in this Jenkinsfile
library "python-utils@${params.JENKINS_COMMIT}"


devToolsProject.run(
  defaultBranch: 'master',
  test: { data ->
    parallel(
      groovydoc: { data['docs'] = groovydoc.generate() },
      groovylint: { groovylint.check('./Jenkinsfile,./*.gradle,**/*.groovy') },
      junit: {
        try {
          sh './gradlew test'
        } finally {
          junit 'build/test-results/**/*.xml'
        }
      },
    )
  },
  publish: { data ->
    jupiter.publishDocs("${data['docs']}/", 'Ableton/python-pipeline-utils')
  },
)

eventRecorder.timedStage('Integration Test') {
  Map stages = [:]
  Map preinstalledPythonVersions = [linux: '3.10', mac: '3.9', win: '3.7']

  ['linux', 'mac', 'win'].each { osType ->
    stages[osType.capitalize()] = {
      // TODO: Fix this once the 12.5 (and newer) Macs are working
      String nodeLabel = "generic-${osType}"
      if (osType == 'mac') {
        nodeLabel = 'generic-mac-xcode12.2'
      }
      eventRecorder.timedNode(nodeLabel) {
        echo 'Test VirtualEnv.create'
        String preinstalledPythonVersion = preinstalledPythonVersions[osType]
        Object venv = virtualenv.create("python${preinstalledPythonVersion}")
        String venvVersion = venv.run(returnStdout: true, script: 'python --version')
        assert venvVersion.startsWith("Python ${preinstalledPythonVersion}")

        if (isUnix()) {
          echo 'Test VirtualEnv.createWithPyenv'
          Object pyvenv = pyenv.createVirtualEnv('3.10.3')
          String pyvenvVersion =
            pyvenv.run(returnStdout: true, script: 'python --version')
          echo pyvenvVersion
          assert pyvenvVersion.trim() == 'Python 3.10.3'
        }
      }
    }
  }

  parallel(stages)
}

if (runTheBuilds.isPushTo(['master'])) {
  devToolsProject.run(
    defaultBranch: 'master',
    deploy: { data ->
      String versionNumber = readFile('VERSION').trim()
      version.tag(versionNumber)
      version.forwardMinorBranch(versionNumber)
    },
  )
}
