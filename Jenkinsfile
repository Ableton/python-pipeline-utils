@SuppressWarnings('VariableTypeRequired') // For the declaration of the _ variable
@Library(['ableton-utils@0.3.0', 'groovylint@0.3.0']) _


runTheBuilds.runDevToolsProject(script: this,
  test: {
    parallel(failFast: false,
      groovylint: {
        groovylint.check('./Jenkinsfile,**/*.gradle,**/*.groovy')
      },
      junit: {
        try {
          sh './gradlew test'
        } finally {
          junit 'build/test-results/**/*.xml'
        }
      },
    )
  },
)
