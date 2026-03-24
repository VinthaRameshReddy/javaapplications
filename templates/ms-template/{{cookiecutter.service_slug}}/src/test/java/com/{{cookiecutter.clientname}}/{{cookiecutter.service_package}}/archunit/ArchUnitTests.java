package com.

{{cookiecutter.clientname}}.{{cookiecutter.service_package}}.archunit;

import com.{{cookiecutter.clientname}}.archunit.AbstractArchUnitTests;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;

@AnalyzeClasses(packages = "com",
        importOptions = {ImportOption.DoNotIncludeTests.class, ImportOption.DoNotIncludeJars.class})
public class ArchUnitTests extends AbstractArchUnitTests {

}
