/**
 * Get the name of the Python 3 executable depending on the OS platform.
 * @return Python executable name
 */
String exeName() {
  // We use `env` here instead of `isUnix()` because we don't want to pollute the Jenkins
  // log with a ton of steps.
  return env.OS == 'Windows_NT' ? 'py -3' : 'python3'
}
