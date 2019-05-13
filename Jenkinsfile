// TODO: when the old job has been retired, remove this block.
if (env.HEAD_REF || env.BASE_REF) {
  return
}

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
    )
  },
  publish: { data ->
    docs.publish(data['docs'], 'AbletonDevTools/python-pipeline-utils')
  },
  deploy: { data ->
    if (runTheBuilds.isPushTo(['master'])) {
      String versionNumber = readFile('VERSION').trim()
      version.tag(versionNumber)
      version.forwardMinorBranch(versionNumber)
    }
  },
)
