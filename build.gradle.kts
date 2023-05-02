@Suppress("DSL_SCOPE_VIOLATION")
plugins {
  alias(libs.plugins.androidApplication) apply false
  alias(libs.plugins.androidLibrary) apply false
  alias(libs.plugins.cacheFix) apply false
  alias(libs.plugins.dependencyAnalysis)
  alias(libs.plugins.doctor)
  alias(libs.plugins.kotlin) apply false
  alias(libs.plugins.kotlinter) apply false
}

apply {
  from(file("gradle/projectDependencyGraph.gradle"))
}
