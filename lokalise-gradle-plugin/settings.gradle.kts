dependencyResolutionManagement {
  repositories {
    maven {
      url = uri("https://s01.oss.sonatype.org/content/repositories/snapshots/")
    }
    google()
    mavenCentral()
  }
  versionCatalogs {
    create("libs") {
      from(files("../gradle/libs.versions.toml"))
    }
  }
}

rootProject.name = "lokalise-gradle-plugin"

include(":lokalise")
