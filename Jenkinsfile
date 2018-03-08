@SuppressWarnings('VariableTypeRequired') // For the declaration of the _ variable
@Library([
  'ableton-utils@0.8',
  'groovylint@0.3.0',
]) _


runTheBuilds.runDevToolsProject(
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
