import com.moowork.gradle.node.NodeExtension
import com.moowork.gradle.node.NodePlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.create

class FrostPlugin : Plugin<Project> {

    companion object {
        private const val NODE_VERSION = "12.4.0"
    }

    override fun apply(project: Project) {
        project.plugins.withId("java-base") {
            project.applyWebGenPlugin()
        }
        project.gradle.taskGraph.whenReady {
            if (!project.plugins.hasPlugin("java-base")) {
                throw IllegalArgumentException("Frost plugin can't be applied without Android or Java or Kotlin plugin.")
            }
        }
    }

    private fun Project.applyWebGenPlugin() {
        setupNode()
        tasks.create(WebGenInstallTask.NAME, WebGenInstallTask::class)
    }

    private fun Project.setupNode() {
        plugins.apply(NodePlugin::class)
        val nodeConfig = extensions.findByName("node") as NodeExtension
        nodeConfig.download = true
        nodeConfig.version = NODE_VERSION
    }
}