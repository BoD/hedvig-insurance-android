plugins {
  id("hedvig.android.ktlint")
  id("hedvig.android.library")
  alias(libs.plugins.molecule)
  alias(libs.plugins.squareSortDependencies)
}

dependencies {
  api(projects.moleculePublic)

  implementation(libs.androidx.lifecycle.viewModel)
  implementation(libs.coroutines.core)
}

android {
  namespace = "com.hedvig.android.molecule.android"
}
