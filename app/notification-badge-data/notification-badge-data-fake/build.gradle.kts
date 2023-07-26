plugins {
  id("hedvig.android.library")
  id("hedvig.android.ktlint")
  alias(libs.plugins.squareSortDependencies)
}

dependencies {
  implementation(libs.turbine)
  implementation(projects.notificationBadgeDataPublic)
}

android {
  namespace = "com.hedvig.android.notification.badge.data.test"
}
