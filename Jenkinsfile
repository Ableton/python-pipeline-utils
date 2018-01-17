@Library(['ableton-utils@0.1.0', 'groovylint@0.1.1']) _


def addStages() {
  runTheBuilds.timedStage('Checkout') {
    // Print out all environment variables for debugging purposes
    sh 'env'
    checkout scm
  }

  runTheBuilds.timedStage('Test') {
    parallel(failFast: false,
      groovylint: {
        groovylint.check('./Jenkinsfile,**/*.gradle,**/*.groovy')
      },
      junit: {
        sh './gradlew test'
        junit 'build/test-results/**/*.xml'
      },
    )
  }
}


runTheBuilds.runForSpecificBranches(runTheBuilds.COMMON_BRANCH_FILTERS, true) {
  node('generic-linux') {
    try {
      runTheBuilds.report('pending', env.CALLBACK_URL)
      addStages()
      runTheBuilds.report('success', env.CALLBACK_URL)
    } catch (error) {
      runTheBuilds.report('failure', env.CALLBACK_URL)
      throw error
    } finally {
      stage('Cleanup') {
        dir(env.WORKSPACE) {
          deleteDir()
        }
      }
    }
  }
}
