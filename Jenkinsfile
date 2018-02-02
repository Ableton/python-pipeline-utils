@SuppressWarnings('VariableTypeRequired') // For the declaration of the _ variable
@Library(['ableton-utils@0.3.0', 'groovylint@0.3.0']) _

final String BRANCH = "${env.HEAD_REF}".replace('origin/', '').replace('refs/heads/', '')
library "python-utils@${BRANCH}"


runTheBuilds.runDevToolsProject(script: this,
  setup: {
    @SuppressWarnings('VariableTypeRequired')
    def venv = virtualenv.create('python3.6')
    echo "venv is ${venv.getClass().toString()}"
  },
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
