plugins {
  id("hedvig.android.ktlint")
  id("hedvig.kotlin.library")
  alias(libs.plugins.molecule)
}

dependencies {
  api(projects.moleculePublic)

  implementation(libs.turbine)
}
