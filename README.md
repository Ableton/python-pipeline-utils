# python-pipeline-utils

`python-pipeline-utils` is a Jenkins shared pipeline library which contains utilities for
working with Python.


## Using the Library

The easiest way to use this library in your Jenkins pipeline scripts is to add it to your
Jenkins configuration. See the [Jenkins shared library usage
documentation][jenkins-shared-lib-usage] for more information. Please note that this
library may not work with the GitHub Organization plugin, since it creates workspace
directories that exceed the shebang limit of 128 characters.


## Singletons

This library contains singleton wrappers for the classes so that the `Jenkinsfile` can be
a bit less verbose.


### `pyenv`

The `pyenv` singleton can be used to create a Python Virtualenv in combination with
[pyenv][pyenv]. For this to work, Pyenv must already be installed on the build node.

```groovy
Object venv

stage('Setup with environment variable') {
  // This assumes that there is a PYENV_ROOT environment variable with the correct path.
  // Note that Jenkins overrides environment variables, so this would need to be defined
  // in your Jenkins configuration for the given executor.
  venv = pyenv.createVirtualEnv('3.6.0')
  venv.run('pip install -r requirements.txt')
}

stage('Setup with manual path') {
  venv = pyenv.createVirtualEnv('3.6.0', '/path/to/pyenv/root')
  venv.run('pip install -r requirements.txt')
}

stage('Test') {
  venv.run(label: 'Run unit tests', script: 'pytest .')
}
```

Note that the `pyenv` singleton does not currently support Windows.


### `pythonPackage`

The `pythonPackage` singleton parses the version number from a package, returning it as a
string.

```groovy
String version = pythonPackage.readVersion(filename: 'mypackage/__init__.py')
```


### `virtualenv`

The `virtualenv` singleton creates a Python Virtualenv in the project's temporary folder.
Example usage might look something like this:

```groovy
Object venv

stage('Setup') {
  venv = virtualenv.create('python3.6')
  venv.run('pip install -r requirements.txt')
}

stage('Test') {
  venv.run(label: 'Run unit tests', script: 'pytest .')
}
```


## Building and Testing

The `python-pipeline-utils` library can be developed locally using the provided Gradle
wrapper. Likewise, the Gradle project can be imported by an IDE like IntelliJ IDEA. For
this, you'll need the Groovy plugin enabled in IDEA and to install Groovy SDK.

This library uses the [JenkinsPipelineUnit][jenkins-pipeline-unit] framework for unit
testing. The unit tests can be run locally with `./gradlew test`.


## Maintainers

This project is maintained by the following GitHub users:

- [@ala-ableton](https://github.com/ala-ableton)
- [@nre-ableton](https://github.com/nre-ableton)


[jenkins-pipeline-unit]: https://github.com/jenkinsci/JenkinsPipelineUnit
[jenkins-shared-lib-usage]: https://jenkins.io/doc/book/pipeline/shared-libraries/#using-libraries
[pyenv]: https://github.com/pyenv/pyenv
