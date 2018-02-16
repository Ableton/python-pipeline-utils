package com.ableton

import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertNotNull

import com.lesfurets.jenkins.unit.BasePipelineTest
import org.junit.Before
import org.junit.Test


class PythonBuilderTest extends BasePipelineTest {
  @SuppressWarnings('FieldTypeRequired')
  def script

  @Override
  @Before
  void setUp() throws Exception {
    super.setUp()

    this.script = loadScript('test/resources/EmptyPipeline.groovy')
    assertNotNull(script)

    helper.with {
      registerAllowedMethod('error', [String], JenkinsMocks.error)
      registerAllowedMethod('deleteDir', [], null)
      registerAllowedMethod('httpRequest', [Map], null)
      registerAllowedMethod('isUnix', [], JenkinsMocks.isUnix)
      registerAllowedMethod('pwd', [Map], JenkinsMocks.pwd)
      registerAllowedMethod('sh', [String], JenkinsMocks.sh)
      registerAllowedMethod('withEnv', [List, Closure]) { vars, body ->
        body()
      }
    }
  }

  @Test
  void install() throws Exception {
    PythonBuilder pb = new PythonBuilder(script: script, version: '1.0.0')
    String sourcesPath = pb.buildDir() + '/' + pb.pythonBaseName()
    String pythonPath = pb.tempDir() + '/' + pb.pythonBaseName()
    JenkinsMocks.addShMock('tar xfz ' + sourcesPath + '.tgz', '', 0)
    JenkinsMocks.addShMock('./configure --prefix=' + pythonPath, '', 0)
    JenkinsMocks.addShMock('make -j1', '', 0)
    JenkinsMocks.addShMock('make install', '', 0)
    assertEquals(pythonPath, pb.install())
  }

  @Test(expected = Exception)
  void installWithBuildError() throws Exception {
    PythonBuilder pb = new PythonBuilder(script: script, version: '1.0.0')
    String sourcesPath = pb.buildDir() + '/' + pb.pythonBaseName()
    String pythonPath = pb.tempDir() + '/' + pb.pythonBaseName()
    JenkinsMocks.addShMock('tar xfz ' + sourcesPath + '.tgz', '', 0)
    JenkinsMocks.addShMock('./configure --prefix=' + pythonPath, '', 0)
    JenkinsMocks.addShMock('make -j1', '', 1)
    pb.install()
  }

  @Test(expected = AssertionError)
  void installWithNullScript() throws Exception {
    PythonBuilder pb = new PythonBuilder(version: '1')
    pb.install()
  }

  @Test(expected = AssertionError)
  void installWithNullVersion() throws Exception {
    PythonBuilder pb = new PythonBuilder(script: script)
    pb.install()
  }

  @Test(expected = ConnectException)
  void installWithDownloadError() throws Exception {
    helper.registerAllowedMethod('httpRequest', [Map]) {
      throw new ConnectException()
    }
    PythonBuilder pb = new PythonBuilder(script: script, version: '1')
    pb.install()
  }

  @Test(expected = Exception)
  void installWithInvalidDownloadWith() throws Exception {
    PythonBuilder pb = new PythonBuilder(script: script, version: '1', downloadWith: 'X')
    pb.install()
  }

  @Test(expected = IllegalArgumentException)
  void installWithEmptyMakeJobs() throws Exception {
    PythonBuilder pb = new PythonBuilder(script: script, version: '1', makeJobs: '')
    String pythonPath = pb.tempDir() + '/' + pb.pythonBaseName()
    JenkinsMocks.addShMock('tar xfz ' + pythonPath + '.tgz', '', 0)
    pb.install()
  }

  @Test(expected = NumberFormatException)
  void installWithInvalidMakeJobs() throws Exception {
    PythonBuilder pb = new PythonBuilder(script: script, version: '1', makeJobs: 'X')
    String sourcesPath = pb.buildDir() + '/' + pb.pythonBaseName()
    JenkinsMocks.addShMock('tar xfz ' + sourcesPath + '.tgz', '', 0)
    pb.install()
  }

  @Test
  void getBuildEnv() throws Exception {
    PythonBuilder pb = new PythonBuilder(script: script, version: '1.0.0')
    assertEquals([], pb.buildEnv())
  }

  @Test
  void getBuildEnvWithCcache() throws Exception {
    PythonBuilder pb = new PythonBuilder(
      script: script, version: '1.0.0', ccachePath: '/opt/ccache/bin')
    script.env = [PATH: '/bin']
    assertEquals(1, pb.buildEnv().size())
    assertEquals('PATH=/opt/ccache/bin:/bin', pb.buildEnv()[0].toString())
  }

  @Test
  void pythonBaseName() throws Exception {
    PythonBuilder pb = new PythonBuilder(script: script, version: '1.0.0', revision: 'a')
    assertEquals('Python-1.0.0a', pb.pythonBaseName())
  }
}
