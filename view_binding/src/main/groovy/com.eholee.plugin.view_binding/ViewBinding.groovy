package com.eholee.plugin.view_binding

import com.eholee.plugin.java.CommonUtil
import com.eholee.plugin.java.GenerateViewBinding
import groovy.json.JsonSlurper
import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.logging.Logging
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.tasks.SourceSet

class ViewBinding implements Plugin<Project> {
    private static final String CONFIG_NAME = "viewBinding"
    private static final String TASK_NAME = "generated_code"
    private List<String> sourceSets = new ArrayList<>();

    @Override
    void apply(Project project) {

        project.getPlugins().withType(JavaPlugin.class, new Action<JavaPlugin>() {
            void execute(JavaPlugin javaPlugin) {
                JavaPluginConvention javaConvention =
                        project.getConvention().getPlugin(JavaPluginConvention.class);
                SourceSet main = javaConvention.getSourceSets().getByName(SourceSet.MAIN_SOURCE_SET_NAME);
                Set<File> sourceSet = main.getJava().getSrcDirs()
                for (File file : sourceSet) {
                    sourceSets.add(file.getAbsolutePath())
                }
                sourceSets.add("build/generated/source/viewBinding")
                main.getJava().setSrcDirs(sourceSets)
            }
        });

        project.extensions.create(CONFIG_NAME, ViewBindingConfig.class)
        // Register a task
        project.tasks.register(TASK_NAME, new Action<Task>() {
            @Override
            void execute(Task task) {
                String configJson = project.getProjectDir().getAbsolutePath() + File.separator + "src" + File.separator + "main" + File.separator + "config.json";
                JsonSlurper jsonSlurper = new JsonSlurper()
                def configJsonInfo = jsonSlurper.parse(new File(configJson))
                def packageName = configJsonInfo["module"]["package"]
                Logging.getLogger(CommonUtil.class).error(String.format("featureName is %s,packageName is %s",project.name,packageName))
                GenerateViewBinding.execute(packageName as String,project, project.viewBinding.enable as boolean)
            }
        })
    }

}

