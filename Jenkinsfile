library(identifier: 'ableton-utils@0.29', changelog: false)
library(identifier: 'groovylint@0.16', changelog: false)
// Get python-utils library from current commit so it can test itself in this Jenkinsfile
library "python-utils@${params.JENKINS_COMMIT}"


devToolsProject.run(
  defaultBranch: 'main',
  test: { data ->
    parallel(
      groovydoc: { data['docs'] = groovydoc.generate() },
      groovylint: { groovylint.check('./Jenkinsfile,./*.gradle,**/*.groovy') },
      junit: {
        junitUtils.run(testResults: 'build/test-results/**/*.xml') {
          sh './gradlew test --warning-mode fail'
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

  ['linux', 'mac', 'win'].each { osType ->
    stages[osType.capitalize()] = {
      String nodeLabel = "generic-${osType}"
      eventRecorder.timedNode(nodeLabel) {
        echo 'Test VirtualEnv.create'
        Object venv = virtualenv.create('python3')
        String venvVersion = venv.run(returnStdout: true, script: 'python --version')
        assert venvVersion.startsWith('Python 3')

        echo 'Test VirtualEnv.createWithPyenv'
        Object pyvenv = pyenv.createVirtualEnv('3.10.3')
        String pyvenvVersion = pyvenv.run(returnStdout: true, script: 'python --version')
        echo pyvenvVersion
        assert pyvenvVersion.trim() == 'Python 3.10.3'
      }
    }
  }

  parallel(stages)
}

if (runTheBuilds.isPushTo(['main'])) {
  devToolsProject.run(
    defaultBranch: 'main',
    deploy: { data ->
      String versionNumber = readFile('VERSION').trim()
      version.tag(versionNumber)
      version.forwardMinorBranch(versionNumber)
    },
  )
}
