package com.learning.plugin.slot;

import com.learning.plugin.slot.digest.DigestExtension;
import com.learning.plugin.slot.version.VersionExtension;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin;
import org.gradle.api.tasks.bundling.Jar;
import org.gradle.api.tasks.bundling.Zip;

import java.io.File;
import java.util.Collections;
import java.util.Map;

public class SlotGameBasePlugin implements Plugin<Project> {

	// Base Plugins.
	public static final Class<JavaPlugin> JAVA_PLUGIN_CLASS = JavaPlugin.class;
	public static final Class<MavenPublishPlugin> MAVEN_PUBLISH_PLUGIN_CLASS = MavenPublishPlugin.class;

	// Base Extensions
	public static final Class<JavaPluginExtension> JAVA_PLUGIN_EXTENSION_CLASS = JavaPluginExtension.class;

	// Custom Extensions.
	public static final String DIGEST_EXTENSION_NAME = "digest";
	public static final Class<DigestExtension> DIGEST_EXTENSION_CLASS = DigestExtension.class;

	public static final String EXTENSION_VERSION_NAME = "versionControl"; // Note: extension name such as 'version' is not allowed, leads to bug. Must be unique.
	public static final Class<VersionExtension> VERSION_EXTENSION_CLASS =  VersionExtension.class;

	// Default properties.
	public static final String BUILD_CLASSES_FOLDER = "build/classes/java/main/";
	public static final String PROJECT_SOURCE_FOLDER = "src/main/java/";
	public static final Map<String, File> EMPTY_MAPPED_FILES = Collections.emptyMap();
	public static final String UNSPECIFIED = "unspecified";
	public static final String DEFAULT_TOML_FILE_PATH = "./gradle/libs.versions.toml";

	public static final Class<Zip> CLASS_ZIP = Zip.class;
	public static final Class<Jar> CLASS_JAR = Jar.class;
	public static final String TASK_JAR = "jar";
	public static final String TASK_SOURCE_JAR = "sourcesJar";
	public static final String TASK_SOURCE_ZIP = "sourceZip";

	@Override
	public void apply(final Project project) {

		// Add additional plugins.
		project.getPluginManager().apply(JAVA_PLUGIN_CLASS); // Get JavaPlugin where the sourceSet, Jars, and Tars tasks can be found.
		project.getPluginManager().apply(MAVEN_PUBLISH_PLUGIN_CLASS);

		// Enable task from the additional plugins.
		project.getExtensions().getByType(JAVA_PLUGIN_EXTENSION_CLASS).withSourcesJar(); // Enable sources Jar task.

		// Create the extensions. The blocks that will expose the properties to the build script.
		DigestExtension digestExtension = project.getExtensions().create(DIGEST_EXTENSION_NAME, DIGEST_EXTENSION_CLASS);
		VersionExtension versionExtension = project.getExtensions().create(EXTENSION_VERSION_NAME, VERSION_EXTENSION_CLASS);

		// Out off box settings.
		configureDigestExtension(project, digestExtension);
		configureVersionExtension(project, versionExtension);
	}

	private void configureDigestExtension(Project project, DigestExtension extension) {
		project.afterEvaluate(exe -> {

			// Set the source directory.
			extension.getSourceDirectory().convention(
					project.getLayout().getProjectDirectory().files(PROJECT_SOURCE_FOLDER)
			);

			// Set the build classes directory.
			extension.getClassesDirectory().convention(
					project.getLayout().getBuildDirectory().files(BUILD_CLASSES_FOLDER)
			);

			// Set the output of the JAR task as input.
			extension.getJarArchiveFile().convention(project.getTasks().withType(CLASS_JAR).named(TASK_JAR));

			// Set the output of the SOURCE-JAR task as input.
			extension.getSourcesJarArchiveFile().convention(project.getTasks().withType(CLASS_JAR).named(TASK_SOURCE_JAR));

			// Set the output of the ZIP-FILE task as input.
			extension.getZipArchiveFile().convention(project.getTasks().withType(CLASS_ZIP).named(TASK_SOURCE_ZIP));

			// Must be overridden by the build script user. Default the map is empty.
			extension.getAdditionalDigestFiles().getMappedFiles().convention(EMPTY_MAPPED_FILES);

		});
	}

	private void configureVersionExtension(Project project, VersionExtension extension) {
		project.afterEvaluate(exe -> {

			// Default path to Toml file.
			extension.getTomlFile().convention(
					project.getRootProject().file(DEFAULT_TOML_FILE_PATH)
			);

			// Default version is set to current version.
			extension.getProjectVersion().convention(
					project.provider(() -> project.getVersion() == Project.DEFAULT_VERSION ? UNSPECIFIED : project.getVersion().toString())
			);

			// Default alias is set to empty string. The user must override the alias to update current version.
			extension.getAlias().convention(UNSPECIFIED);

		});
	}
}
