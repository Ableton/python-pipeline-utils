package com.ableton

import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertNotNull
import static org.junit.Assert.assertNull
import static org.junit.Assert.assertTrue

import com.lesfurets.jenkins.unit.BasePipelineTest
import org.junit.Before
import org.junit.Test


/**
 * Tests for the PythonPackage class.
 */
class PythonPackageTest extends BasePipelineTest {
  Object script

  @Override
  @Before
  void setUp() throws Exception {
    super.setUp()

    this.script = loadScript('test/resources/EmptyPipeline.groovy')
    assertNotNull(script)

    helper.registerAllowedMethod('readFile', [String], JenkinsMocks.readFile)

    JenkinsMocks.addReadFileMock('__init__.py', '''
# Copyright (c) 2018 Ableton AG, Berlin. All rights reserved.\n
\n
"""gruberflask is designed to help you obtain $640 million in bearer bonds."""\n
\n
__version__ = '1.2.3'\n
''',
    )
  }

  @Test
  void readVersion() throws Exception {
    String version = new PythonPackage(script: script).readVersion(
      filename: '__init__.py',
    )

    assertEquals('1.2.3', version)
  }

  @Test(expected = AssertionError)
  void noScript() throws Exception {
    new PythonPackage().readVersion(filename: '__init__.py')
  }

  @Test(expected = AssertionError)
  void noFilename() throws Exception {
    new PythonPackage(script: script).readVersion(filename: null)
  }

  @Test
  void settingBeginning() throws Exception {
    Boolean errorCalled = false
    helper.registerAllowedMethod('error', [String]) {
      errorCalled = true
    }
    new PythonPackage(script: script).readVersion(
        filename: '__init__.py',
        versionLineBeginning: 'fakeBeginniung',
      )

    assertTrue(errorCalled)
  }

  @Test
  void settingEnding() throws Exception {
    String version = new PythonPackage(script: script).readVersion(
      filename: '__init__.py',
      versionLineEnding: ".3'",
    )

    assertEquals('1.2', version)  // Trailing .3' is stripped.
  }

  @Test
  void settingNonexistentEnding() throws Exception {
    String version = new PythonPackage(script: script).readVersion(
      filename: '__init__.py',
      versionLineEnding: 'fakeEnding',
    )

    assertNull(version)
  }
}
