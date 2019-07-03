import com.moowork.gradle.node.npm.NpmTask
import org.gradle.api.DefaultTask
import org.gradle.api.logging.LogLevel
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import java.io.File

/**
 * Based on https://github.com/apollographql/apollo-android/blob/master/apollo-gradle-plugin/src/main/groovy/com/apollographql/apollo/gradle/ApolloCodegenInstallTask.groovy
 */
@CacheableTask
open class WebGenInstallTask : DefaultTask() {

    companion object {
        const val TAG = "frost-web-gen"
        const val NAME = "installWebGen"
        val INSTALLATION_PATH = TAG + File.separator + "node_modules"
        val PACKAGE_FILE_PATH = TAG + File.separator + "package.json"

        const val TYPESCRIPT_VERSION = "3.3.1"
        const val SASS_VERSION = "1.19.0"
    }

    @OutputDirectory
    val installDir = project.layout.directoryProperty()
    @OutputFile
    val packageFile = project.layout.fileProperty()

    val npmTask = NpmTask()

    init {
        group = "frost"
        description = "Runs npm install for $TAG"

        installDir.set(project.file(File(project.buildDir, INSTALLATION_PATH)))
        packageFile.set(project.file(File(project.buildDir, PACKAGE_FILE_PATH)))
        npmTask.setWorkingDir(File(project.buildDir, TAG))
    }

    @TaskAction
    fun exec() {
        installDir.get().asFile.takeIf { it.isDirectory }?.deleteRecursively()
        writePackageFile(packageFile.get().asFile)
        npmTask.setArgs(
            listOf(
                "install",
                "typescript@$TYPESCRIPT_VERSION",
                "sass@$SASS_VERSION",
                "--save",
                "--save-exact"
            )
        )
        npmTask.logging.captureStandardOutput(LogLevel.INFO)
        npmTask.exec()
    }

    private fun writePackageFile(packageFile: File) {
        packageFile.writeText(
            """
            {
                "name": "$TAG",
                "version": "1.0"
            }
            """.trimIndent()
        )
    }
}