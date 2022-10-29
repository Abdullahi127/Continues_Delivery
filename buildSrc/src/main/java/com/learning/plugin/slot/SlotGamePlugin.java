package com.learning.plugin.slot;

import com.learning.plugin.slot.digest.DigestExtension;
import com.learning.plugin.slot.digest.TaskDigestOnFiles;
import com.learning.plugin.slot.digest.TaskDigestOnGeneratedFiles;
import com.learning.plugin.slot.version.TaskSetCurrentVersion;
import com.learning.plugin.slot.version.TaskUpdateVersion;
import com.learning.plugin.slot.version.VersionExtension;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.api.tasks.bundling.Jar;
import org.gradle.api.tasks.bundling.Zip;

public class SlotGamePlugin implements Plugin<Project> {

	// --- Digest tasks.
	public static final Class<Zip> CLASS_ZIP = Zip.class;
	public static final Class<Jar> CLASS_JAR = Jar.class;
	public static final String TASK_JAR = "jar";
	public static final String TASK_SOURCE_JAR = "sourcesJar";
	public static final String TASK_SOURCE_ZIP = "sourceZip";

	public static final String TASK_DIGEST_ON_GENERATED_FILES = "digestOnGeneratedFiles";
	public static final Class<TaskDigestOnGeneratedFiles> CLASS_DIGEST_ON_GENERATED_FILES = TaskDigestOnGeneratedFiles.class;

	public static final String TASK_DIGEST_ON_FILES = "digestOnFiles";
	public static final Class<TaskDigestOnFiles> CLASS_DIGEST_ON_FILES = TaskDigestOnFiles.class;

	// --- Version tasks.
	public static final String TASK_UPDATE_VERSION = "updateVersion";
	public static final Class<TaskUpdateVersion> CLASS_UPDATE_VERSION = TaskUpdateVersion.class;

	public static final String TASK_SET_CURRENT_VERSION = "setCurrentVersion";
	public static final Class<TaskSetCurrentVersion> CLASS_SET_CURRENT_VERSION = TaskSetCurrentVersion.class;

	// --- Slot tasks.
	public static final String TASK_SLOT_BUILD = "slotBuild";
	public static final String TASK_NEXT_VERSION = "nextVersion";
	public static final String TASK_TAG_N_PUSH = "tagAndPush";

	// Common task variables.
	public static final String GROUP_THUNDERKICK = "thunderkick";
	public static final String CLEAN_TASK = "clean";
	public static final String BUILD_TASK = "build";
	public static final String ASSEMBLE_TASK = "assemble";
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
		registerSetCurrentVersion(project, versionExtension);

		// Slot build tasks.
		registerSlotBuild(project); // Must be the last task. Includes tasks: NextVersion, tagAndPush.
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
			task.setDescription("Digest on source files (in and out), the jars (with and without source), and the source zip file.");
			task.mustRunAfter(ASSEMBLE_TASK, TASK_SOURCE_ZIP);

			task.getSourceDirectory().set(extension.getSourceDirectory());
			task.getClassesDirectory().set(extension.getClassesDirectory().get());
			task.getJarFile().set(extension.getJarFile().get());
			task.getSourceJarFile().set(extension.getSourceJarFile().get());
			task.getZipFile().set(extension.getZipFile().get());

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
			task.getMessage().set(extension.getAdditionalDigestFiles().getMessage());

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
			task.setDescription("Increases current version by one.");
			task.mustRunAfter(TASK_SET_CURRENT_VERSION);

			// Update the properties.
			task.setTomlFile(extension.getTomlFile().get());
			task.setAlias(extension.getAlias().get());
			task.setProjectVersion(extension.getProjectVersion().get());

			// Update project version on runtime
			task.doLast(Task-> project.setVersion(task.getProjectVersion()));
		});
	}

	private void registerSetCurrentVersion(Project project, VersionExtension extension) {
		project.getTasks().register(TASK_SET_CURRENT_VERSION, CLASS_SET_CURRENT_VERSION, task -> {

			// Group, description and dependency order.
			task.setGroup(GROUP_THUNDERKICK);
			task.setDescription("Sets the current version by reading the input string.");

			// Update the properties.
			task.setTomlFile(extension.getTomlFile().get());
			task.setAlias(extension.getAlias().get());
			task.setProjectVersion(extension.getProjectVersion().get());

			// Update project version on runtime
			task.doLast(Task-> project.setVersion(task.getProjectVersion()));
		});

	}

	private void registerSlotBuild(Project project) {

		// Slot build task triggers other tasks.
		project.getTasks().register(TASK_SLOT_BUILD, task -> {
			task.setGroup(GROUP_THUNDERKICK);
			task.setDescription("build and publish jars");
			task.dependsOn(
					CLEAN_TASK,
					BUILD_TASK,
					PUBLISH_TASK // Cannot publish until JFrog artifactory is configured.
			);
		});

		// Execution order = TASK_NEXT_VERSION, CLEAN_TASK, BUILD_TASK, PUBLISH_TASK, TASK_TAG_N_PUSH.
		project.getTasks().named(BUILD_TASK, task -> task.mustRunAfter(CLEAN_TASK));

	}
}
