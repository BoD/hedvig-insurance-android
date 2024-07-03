dependencyResolutionManagement {
  repositories {
    maven {
      url = uri("https://s01.oss.sonatype.org/content/repositories/snapshots/")
    }
    google()
    mavenCentral()
    maven("https://plugins.gradle.org/m2/")
  }
  versionCatalogs {
    create("libs") {
      from(files("../gradle/libs.versions.toml"))
    }
  }
}

rootProject.name = "build-logic"

include(":convention")
