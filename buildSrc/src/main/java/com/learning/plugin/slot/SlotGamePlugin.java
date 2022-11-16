package com.learning.plugin.slot;

import com.learning.plugin.slot.digest.DigestExtension;
import com.learning.plugin.slot.digest.TaskDigestOnFiles;
import com.learning.plugin.slot.digest.TaskDigestOnGeneratedFiles;
import com.learning.plugin.slot.version.TaskSetProjectVersion;
import com.learning.plugin.slot.version.TaskUpdateProjectVersion;
import com.learning.plugin.slot.version.VersionExtension;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.api.tasks.bundling.Zip;

public class SlotGamePlugin implements Plugin<Project> {

	// --- Digest tasks.
	public static final Class<Zip> CLASS_ZIP = Zip.class;
	public static final String TASK_SOURCE_ZIP = "sourceZip";

	public static final String TASK_DIGEST_ON_GENERATED_FILES = "digestOnGeneratedFiles";
	public static final Class<TaskDigestOnGeneratedFiles> CLASS_DIGEST_ON_GENERATED_FILES = TaskDigestOnGeneratedFiles.class;

	public static final String TASK_DIGEST_ON_FILES = "digestOnFiles";
	public static final Class<TaskDigestOnFiles> CLASS_DIGEST_ON_FILES = TaskDigestOnFiles.class;

	// --- Version tasks.
	public static final String TASK_UPDATE_VERSION = "updateProjectVersion";
	public static final Class<TaskUpdateProjectVersion> CLASS_UPDATE_VERSION = TaskUpdateProjectVersion.class;

	public static final String TASK_SET_PROJECT_VERSION = "setProjectVersion";
	public static final Class<TaskSetProjectVersion> CLASS_SET_CURRENT_VERSION = TaskSetProjectVersion.class;

	// --- Slot build task.
	public static final String TASK_BUILD_N_PUBLISH = "buildNPublish";

	// Common task variables.
	public static final String GROUP_THUNDERKICK = "thunderkick";
	public static final String BUILD_TASK = "build";
	public static final String ASSEMBLE_TASK = "assemble";
	public static final String CLEAN_TASK = "clean";
	public static final String PUBLISH_TASK = "publish";


	@Override
	public void apply(Project project) {

		// -- Apply other plugins.
		project.getPluginManager().apply(SlotGameBasePlugin.class);

		//---- Get extensions
		DigestExtension digestExtension = project.getExtensions().getByType(SlotGameBasePlugin.DIGEST_EXTENSION_CLASS);
		VersionExtension versionExtension = project.getExtensions().getByType(SlotGameBasePlugin.VERSION_EXTENSION_CLASS);

		//---- Register tasks.

		// Digest Tasks.
		registerSourceZip(project);
		registerDigestOnGeneratedFilesTask(project, digestExtension);
		registerDigestOnFilesTask(project, digestExtension);

		// Version tasks.
		registerUpdateVersion(project, versionExtension);
		registerSetProjectVersion(project, versionExtension);

		// Clean Build Publish task.
		registerBuildPublish(project);
	}

	private void registerSourceZip(Project project) {
		project.getTasks().register(TASK_SOURCE_ZIP, CLASS_ZIP, task->{

			// Group, describe and set dependency order.
			task.setGroup(GROUP_THUNDERKICK);
			task.setDescription("Zips the source files.");
			task.mustRunAfter(ASSEMBLE_TASK);

			// Configure the task.
			SourceSetContainer sourceSets = (SourceSetContainer) project.getExtensions().getByName("sourceSets");
			task.from(sourceSets.named("main").get().getAllSource());

		});

		// Add task to build task.
		project.getTasks()
			   .named(BUILD_TASK)
			   .get()
			   .dependsOn(TASK_SOURCE_ZIP);
	}

	private void registerDigestOnGeneratedFilesTask(Project project, DigestExtension extension) {

		project.getTasks().register(TASK_DIGEST_ON_GENERATED_FILES, CLASS_DIGEST_ON_GENERATED_FILES, task -> {
			task.setGroup(GROUP_THUNDERKICK);
			task.setDescription("Digest on source files (in and out), the jars (with and without sources), and the source zip file.");
			task.mustRunAfter(ASSEMBLE_TASK, TASK_SOURCE_ZIP);

			task.getSourceDirectory().set(extension.getSourceDirectory());
			task.getClassesDirectory().set(extension.getClassesDirectory().get());
			task.getJarTask().set(extension.getJarArchiveFile().get());
			task.getSourceJarTask().set(extension.getSourcesJarArchiveFile().get());
			task.getZipTask().set(extension.getZipArchiveFile().get());

		});

		// Add task to build task.
		project.getTasks()
			   .named(BUILD_TASK)
			   .get()
			   .dependsOn(TASK_DIGEST_ON_GENERATED_FILES);
	}

	private void registerDigestOnFilesTask(Project project, DigestExtension extension) {
		project.getTasks().register(TASK_DIGEST_ON_FILES, CLASS_DIGEST_ON_FILES, task -> {

			// Group, describe and set dependency order.
			task.setGroup(GROUP_THUNDERKICK);
			task.setDescription("Digest on additional files.");
			task.mustRunAfter(ASSEMBLE_TASK, TASK_DIGEST_ON_GENERATED_FILES);

			// Configure the task.
			task.getMappedFiles().set(extension.getAdditionalDigestFiles().getMappedFiles());

		});

		// Add task to build task.
		project.getTasks()
			   .named(BUILD_TASK)
			   .get()
			   .dependsOn(TASK_DIGEST_ON_FILES);
	}

	private void registerUpdateVersion(Project project, VersionExtension extension) {
		project.getTasks().register(TASK_UPDATE_VERSION, CLASS_UPDATE_VERSION, task -> {

			// Group, description and dependency order.
			task.setGroup(GROUP_THUNDERKICK);
			task.setDescription("Increases the project version by one in toml file.");
			task.mustRunAfter(TASK_SET_PROJECT_VERSION);

			// Update the properties.
			task.setAlias(extension.getAlias().get());
			task.setProjectVersion(extension.getProjectVersion().get());
			task.setTomlFile(extension.getTomlFile().get());

			task.doLast(exe-> {

				// Update the project version on runtime.
				project.setVersion(task.getProjectVersion());

			});

		});
	}

	private void registerSetProjectVersion(Project project, VersionExtension extension) {
		project.getTasks().register(TASK_SET_PROJECT_VERSION, CLASS_SET_CURRENT_VERSION, task -> {

			// Group, description and dependency order.
			task.setGroup(GROUP_THUNDERKICK);
			task.setDescription("Set the project version in toml file.");

			// Update the properties.
			task.setTomlFile(extension.getTomlFile().get());
			task.setAlias(extension.getAlias().get());
			task.setProjectVersion(extension.getProjectVersion().get());

			task.doLast(exe-> {

				// Update the project version on runtime.
				project.setVersion(task.getProjectVersion());

			});

		});

	}

	private void registerBuildPublish(Project project){
		project.getTasks().register(TASK_BUILD_N_PUBLISH, task -> {
			task.setGroup(GROUP_THUNDERKICK);
			task.setDescription("Builds and publish the project.");
			task.dependsOn(
					CLEAN_TASK,
					BUILD_TASK,
					PUBLISH_TASK
			);
		});

		// Execution order = CLEAN_TASK, BUILD_TASK, PUBLISH_TASK
		project.getTasks().named(BUILD_TASK, task -> task.mustRunAfter(CLEAN_TASK));
		project.getTasks().named(PUBLISH_TASK, task -> task.mustRunAfter(BUILD_TASK));
	}

}
