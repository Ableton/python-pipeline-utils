package com.ableton

import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertNotNull
import static org.junit.jupiter.api.Assertions.assertThrows

import com.lesfurets.jenkins.unit.BasePipelineTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test


/**
 * Tests for the PythonPackage class.
 */
class PythonPackageTest extends BasePipelineTest {
  Object script

  @Override
  @BeforeEach
  void setUp() throws Exception {
    super.setUp()

    this.script = loadScript('test/resources/EmptyPipeline.groovy')
    assertNotNull(script)

    helper.addReadFileMock('__init__.py', '''
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

  @Test
  void readVersionDoubleQuotes() throws Exception {
    helper.addReadFileMock('__init__.py', '__version__ = "1.2.3"')

    String version = new PythonPackage(script: script).readVersion(
      filename: '__init__.py',
    )

    assertEquals('1.2.3', version)
  }

  @Test
  void readVersionFunkyFormatting() throws Exception {
    helper.addReadFileMock('__init__.py', '__version__      =\n"1.2.3"')

    String version = new PythonPackage(script: script).readVersion(
      filename: '__init__.py',
    )

    assertEquals('1.2.3', version)
  }

  @Test
  void readVersionOtherName() throws Exception {
    helper.addReadFileMock('__init__.py', 'foo = "1.2.3"')

    String version = new PythonPackage(script: script).readVersion(
      filename: '__init__.py', versionVariableName: 'foo'
    )

    assertEquals('1.2.3', version)
  }

  @Test
  void noScript() throws Exception {
    assertThrows(AssertionError) {
      new PythonPackage().readVersion(filename: '__init__.py')
    }
  }

  @Test
  void noFilename() throws Exception {
    assertThrows(AssertionError) {
      new PythonPackage(script: script).readVersion(filename: null)
    }
  }
}
