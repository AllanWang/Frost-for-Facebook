package com.pitchedapps.frost.gradle

import com.moowork.gradle.node.npm.NpmTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.OutputFile

/**
 * Based on https://github.com/apollographql/apollo-android/blob/master/apollo-gradle-plugin/src/main/groovy/com/apollographql/apollo/gradle/ApolloCodegenInstallTask.groovy
 */
class WebGenInstallTask extends NpmTask {

    static final String TAG = "frost-web-gen"
    static final String NAME = "installWebGen"
    static final String INSTALLATION_PATH = TAG + File.separator + "node_modules"
    static final String PACKAGE_FILE_PATH = TAG + File.separator + "package.json"

    static final String TYPESCRIPT_VERSION = "3.3.1"
    static final String SASS_VERSION = "1.19.0"

    @OutputDirectory
    final DirectoryProperty installDir = ObjectFactory.directoryProperty()
    @OutputFile
    final RegularFileProperty packageFile = ObjectFactory.fileProperty()

    WebGenInstallTask() {
        setGroup("frost")
        setDescription("Runs npm install for $TAG")
        installDir.set(project.file(new File(project.buildDir, INSTALLATION_PATH)))
        packageFile.set(project.file(new File(project.buildDir, PACKAGE_FILE_PATH)))
    }

    @Override
    void exec() {
        installDir.get().getAsFile().deleteDir()
        writePackageFile(packageFile.get().getAsFile())

        setArgs(Lists.newArrayList("install", "typescript@" + TYPESCRIPT_VERSION, "sass@" + SASS_VERSION, "--save", "--save-exact"))
        getLogging().captureStandardOutput(LogLevel.INFO)

        super.exec()
    }

    /**
     * Generates a dummy package.json file to silence npm warnings
     */
    private static void writePackageFile(File apolloPackageFile) {
        apolloPackageFile.write(
                '''{
  "name": "frost-web-gen"
}
'''
        )
    }
}