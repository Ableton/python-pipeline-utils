package com.ableton

import com.cloudbees.groovy.cps.NonCPS


/**
 * Provides a minimal wrapper around a Python virtualenv environment. The virtualenv is
 * stored under the workspace.
 */
class VirtualEnv implements Serializable {
  /**
   * Script context.
   * <strong>Required value, may not be null!</strong>
   */
  Object script
  /**
   * Binary directory for the virtualenv on disk. This value is set during construction of
   * the object.
   */
  String venvBinDir = null
  /**
   * Root directory for the virtualenv on disk. This value is set during construction of
   * the object, and is under the workspace.
   */
  String venvRootDir = null

  /**
   * Create a virtualenv using a specific locally installed version of Python.
   *
   * @param script Script context.
   *               <strong>Required value, may not be null!</strong>
   * @param python Python version or absolute path to Python executable.
   * @param randomSeed If non-zero, use this seed for the random number generator.
   * @return New instance of VirtualEnv object.
   */
  static VirtualEnv create(Object script, String python, long randomSeed = 0) {
    VirtualEnv venv = new VirtualEnv(script, randomSeed)
    String commandLine = "virtualenv --python=${python} ${venv.venvRootDir}"
    if (script.env.OS == 'Windows_NT') {
      commandLine = commandLine.replace('\\', '/')
    }
    venv.script.sh(label: "Create virtualenv for ${python}", script: commandLine)
    return venv
  }

  /**
   * Construct a new instance of this class. This method <strong>does not</strong>
   * initialize the environment by running {@code virtualenv}. Use the factory method
   * {@link #create(Object, String)} instead.
   *
   * @param script Script context. <strong>Required value, may not be null!</strong>
   * @param randomSeed If non-zero, use this seed for the random number generator.
   * @see #create(Object, String)
   */
  VirtualEnv(Object script, long randomSeed = 0) {
    assert script

    this.script = script
    String activateSubDir
    String workspace = script.env.WORKSPACE

    if (script.env.OS == 'Windows_NT') {
      activateSubDir = 'Scripts'
      workspace = workspace.replace('\\', '/')
    } else {
      activateSubDir = 'bin'
    }

    long seed = randomSeed ?: System.currentTimeMillis() * this.hashCode()
    this.venvRootDir = "${workspace}/.venv/venv-${randomName(seed)}"
    this.venvBinDir = "${venvRootDir}/${activateSubDir}"
  }

  /**
   * Removes the virtualenv from disk. You can call this method in the cleanup stage of
   * the pipeline to avoid cluttering the build node with temporary files. Note that the
   * virtualenv is stored underneath the workspace, so removing the workspace will have
   * the same effect.
   */
  void cleanup() {
    script.dir(venvRootDir) { script.deleteDir() }
  }

  /**
   * Run a closure body inside of the virtualenv.
   *
   * @param body Closure body to execute.
   */
  void inside(Closure body) {
    script.withEnv(["PATH+VENVBIN=${venvBinDir}", "VIRTUAL_ENV=${venvRootDir}"]) {
      body()
    }
  }

  /**
   * Run a command in the virtualenv.
   *
   * @param arguments Arguments to pass to the {@code sh} command. See the documentation
   *        for the {@code sh} step for valid arguments.
   */
  @SuppressWarnings('MethodReturnTypeRequired')
  def run(Map arguments) {
    assert arguments
    assert arguments.containsKey('script')

    if (!arguments.label) {
      arguments['label'] = "Run command in virtualenv: ${arguments.script}"
    }
    @SuppressWarnings('VariableTypeRequired')
    def result = null
    inside {
      result = script.sh(arguments)
    }
    return result
  }

  /**
   * Run a command in the virtualenv.
   *
   * @param command Command to run.
   */
  void run(String command) {
    inside { script.sh(label: "Run command in virtualenv: ${command}", script: command) }
  }

  @NonCPS
  protected static String randomName(long seed) {
    String pool = '0123456789'
    int length = 8
    @SuppressWarnings('InsecureRandom')
    Random random = new Random(seed)
    List randomChars = (0..length - 1).collect {
      pool[random.nextInt(pool.size())]
    }
    return randomChars.join('')
  }
}
