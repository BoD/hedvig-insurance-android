@Suppress("DSL_SCOPE_VIOLATION")
plugins {
  id("hedvig.android.library")
  id("hedvig.android.library.compose")
  id("hedvig.android.ktlint")
  alias(libs.plugins.molecule)
  alias(libs.plugins.serialization)
}

android {
  namespace = "com.hedvig.android.core.ui"

  buildFeatures {
    viewBinding = true
  }
}

dependencies {
  implementation(projects.app.core.designSystem)
  implementation(projects.app.core.resources)
  implementation(projects.app.core.icons)
  implementation(projects.app.apollo.octopus)

  api(libs.androidx.compose.foundation)
  api(libs.androidx.compose.material3)
  api(libs.arrow.core)

  implementation(libs.accompanist.insetsUi)
  implementation(libs.androidx.compose.material)
  implementation(libs.androidx.compose.material3.windowSizeClass)
  implementation(libs.androidx.compose.materialIconsExtended)
  implementation(libs.androidx.compose.uiUtil)
  implementation(libs.androidx.lifecycle.viewModel)
  implementation(libs.androidx.other.appCompat)
  implementation(libs.coil.coil)
  implementation(libs.coil.compose)
  implementation(libs.kotlinx.immutable.collections)
  implementation(libs.kotlinx.serialization.core)
  implementation(libs.kotlinx.serialization.json)
  implementation(libs.slimber)
}
