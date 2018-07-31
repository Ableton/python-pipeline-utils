@SuppressWarnings('VariableTypeRequired') // For the declaration of the _ variable
@Library([
  'ableton-utils@0.8',
  'groovylint@0.3',
]) _


runTheBuilds.runDevToolsProject(
  test: { data ->
    parallel(failFast: false,
      groovydoc: {
        data['docs'] = groovydoc.generate()
      },
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
  deploy: { data ->
    runTheBuilds.runForSpecificBranches(['master'], false) {
      parallel(failFast: false,
        groovydoc: {
          docs.publish(data['docs'], 'AbletonDevTools/python-pipeline-utils')
        },
        version: {
          String versionNumber = readFile('VERSION').trim()
          version.tag(versionNumber)
          version.forwardMinorBranch(versionNumber)
        },
      )
    }
  },
)
