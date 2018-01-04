@Library(['codenarc@0.2.0', 'runthebuilds@0.5.0']) _


def addStages() {
  runTheBuilds.timedStage('Checkout') {
    // Print out all environment variables for debugging purposes
    sh 'env'
    checkout scm
  }

  runTheBuilds.timedStage('Test') {
    parallel(failFast: false,
      codenarc: {
        codenarc.check('**/Jenkinsfile,**/*.gradle,**/*.groovy')
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
    }
    catch (error) {
      runTheBuilds.report('failure', env.CALLBACK_URL)
      throw error
    }
    finally {
      stage('Cleanup') {
        dir(env.WORKSPACE) {
          deleteDir()
        }
      }
    }
  }
}
