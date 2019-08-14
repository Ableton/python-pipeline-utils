library 'ableton-utils@0.13'
library 'groovylint@0.6'


devToolsProject.run(
  test: { data ->
    parallel(failFast: false,
      groovydoc: {
        data['docs'] = groovydoc.generate()
      },
      groovylint: {
        groovylint.check('./Jenkinsfile,./*.gradle,**/*.groovy')
      },
      junit: {
        try {
          sh './gradlew test'
        } finally {
          junit 'build/test-results/**/*.xml'
        }
      },
      'junit-win': {
        eventRecorder.timedNode('generic-win') {
          sh 'env' // Print out all environment variables for debugging purposes
          gitRepo.checkoutRevision(
            credentialsId: 'build-ssh-key',
            revision: env.JENKINS_COMMIT,
            url: "git@github.com:${env.JENKINS_REPO_SLUG}.git",
          )

          try {
            bat 'gradlew.bat test'
          } finally {
            junit 'build/test-results/**/*.xml'
          }
        }
      },
    )
  },
  publish: { data ->
    docs.publish(data['docs'], 'AbletonDevTools/python-pipeline-utils')
  },
  deployWhen: { return runTheBuilds.isPushTo(['master']) },
  deploy: { data ->
    String versionNumber = readFile('VERSION').trim()
    version.tag(versionNumber)
    version.forwardMinorBranch(versionNumber)
  },
)
