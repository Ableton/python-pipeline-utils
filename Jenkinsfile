// TODO: when the old job has been retired, remove this block.
if (env.HEAD_REF || env.BASE_REF) {
  return
}

library 'ableton-utils@0.11'
library 'groovylint@0.4'


runTheBuilds.runDevToolsProject(
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
    )
  },
  deploy: { data ->
    runTheBuilds.withBranches(branches: ['master'], acceptPullRequests: false) {
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
