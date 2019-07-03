import com.moowork.gradle.node.NodeExtension
import com.pitchedapps.frost.gradle.WebGenInstallTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.internal.file.FileResolver

import javax.inject.Inject

class WebGenPlugin implements Plugin<Project> {

    private static final String NODE_VERSION = "12.4.0"
    private Project project
    private final FileResolver fileResolver

    @Inject
    WebGenPlugin(FileResolver fileResolver) {
        this.fileResolver = fileResolver
    }

    /**
     * Based on https://github.com/apollographql/apollo-android/blob/master/apollo-gradle-plugin/src/main/groovy/com/apollographql/apollo/gradle/ApolloPlugin.groovy
     * @param target
     */
    @Override
    void apply(Project target) {
        this.project = project
        project.plugins.withId("java-base") {
            applyWebGenPlugin()
        }
        project.gradle.getTaskGraph().whenReady {
            if (!project.plugins.hasPlugin("java-base")) {
                throw new IllegalArgumentException(
                        "Frost Web Gen plugin can't be applied without Android or Java or Kotlin plugin.")
            }
        }
    }

    private void applyWebGenPlugin() {
        setupNode()
        project.tasks.create(WebGenInstallTask.NAME, WebGenInstallTask.class)
    }

    private void setupNode() {
        project.plugins.apply NodePlugin
        NodeExtension nodeConfig = project.extensions.findByName("node") as NodeExtension
        nodeConfig.download = true
        nodeConfig.version = NODE_VERSION
    }
}