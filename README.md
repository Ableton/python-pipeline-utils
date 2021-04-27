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


### `pipenv`

The `pipenv` singleton runs a closure using [`pipenv`](pipenv) for a list of Python
versions. Unlike the `virtualenv` singleton, it automatically installs packages from the
`pipenv` lockfile with `pipenv sync --dev --python`.

```groovy
pipenv.runWith(['python3.6', 'python3.7', 'python3.8']) { python ->
  sh(label: "Running unit tests with ${python}", script: 'pytest .')
}
```


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
  venv = virtualenv.create(this, 'python3.6')
  venv.run('pip install -r requirements.txt')
}

stage('Test') {
  venv.run(label: 'Run unit tests', script: 'pytest .')
}
```


### `virtualenvs`

This singleton is similar to `virtualenv`, but is a list of `virtualenv` objects. It is
intended to make testing code with multiple Python versions easier.

```groovy
Object venvs = virtualenvs.create(['python3.6', 'python3.7', 'python3.8'])
venvs.run('pip install -r requirements.txt')
venvs.run('pytest .')
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
[pipenv]: https://pypi.org/project/pipenv/
