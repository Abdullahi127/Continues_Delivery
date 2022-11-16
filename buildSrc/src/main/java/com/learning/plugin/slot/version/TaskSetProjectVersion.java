package com.learning.plugin.slot.version;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.options.Option;

import java.io.File;
import java.util.List;

public abstract class TaskSetProjectVersion extends DefaultTask {

	private File tomlFile = null;
	private String alias = null;
	private String version = null;

	@InputFile
	public File getTomlFile(){
		return this.tomlFile;
	}

	public void setTomlFile(File file){
		this.tomlFile = file;
	}

	@Input
	public String getAlias(){
		return this.alias;
	}

	@Option(option = "alias", description = "Set the alias that will be updated.")
	public void setAlias(String alias){
		this.alias = alias;
	}

	@Input
	public String getProjectVersion() {
		return this.version;
	}

	@Option(option = "projectVersion", description = "Set the project version.")
	public void setProjectVersion(String version) {
		this.version = version;
	}

	@TaskAction
	public void run() {
		VersionTaskHelper.isUnspecified(alias, "The alias of the version must be specified!");
		VersionTaskHelper.isUnspecified(version, "The project version must be specified!");

		boolean isValidFile = VersionTaskHelper.isValidTomlFile(tomlFile);
		boolean isValidAlias = VersionTaskHelper.isValidAlias(alias, tomlFile);
		boolean isValidVersion = VersionTaskHelper.isValidVersion(version, VersionTaskHelper.VALID_VERSIONS);

		VersionTaskHelper.isInvalidTomlProperties(!isValidFile, tomlFile.getName(), !isValidAlias, alias, !isValidVersion, version, VersionTaskHelper.VALID_VERSIONS);

		String replaceWith = alias + " = " + "\"" + version + "\"";
		List<String> validVersions = VersionTaskHelper.createValidVersions(alias, VersionTaskHelper.VALID_VERSIONS);

		boolean failed = VersionTaskHelper.replaceLinesMatching(getTomlFile().getAbsolutePath(), replaceWith, validVersions);
		VersionTaskHelper.failedVersionReplacement(failed, alias);

	}
}