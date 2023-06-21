import java.net.URI

pluginManagement {
  repositories {
    gradlePluginPortal()
    google()
    mavenCentral()
  }
}

dependencyResolutionManagement {
  repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
  repositories {
    google()
    mavenCentral()
    maven {
      name = "Mozilla Nightly"
      url = URI("https://nightly.maven.mozilla.org/maven2")
      content {
        // Always fetch components from the snapshots repository
        includeGroup("org.mozilla.components")
      }
    }
    maven {
      name = "Mozilla"
      url = URI("https://maven.mozilla.org/maven2")
      content {
        // Never fetch components from here. We always want to use snapshots.
        excludeGroup("org.mozilla.components")
      }
    }
  }
}

include(":app-compose")
