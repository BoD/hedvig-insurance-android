plugins {
  id("hedvig.android.ktlint")
  id("hedvig.kotlin.library")
}

dependencies {
  implementation(projects.app.coreDatastore)

  implementation(libs.androidx.datastore.core)
  implementation(libs.androidx.datastore.preferencesCore)
  implementation(libs.coroutines.core)
  implementation(libs.coroutines.test)
}
