package com.learning.plugin.slot.version;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.options.Option;

import java.io.File;
import java.util.List;

public abstract class TaskUpdateVersion extends DefaultTask {

	private File tomlFile = null;
	private String alias = null;
	private String version = null;

	@InputFile
	public File getTomlFile(){
		return this.tomlFile;
	}

	public void setTomlFile(File tomlFile){
		this.tomlFile = tomlFile;
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

	@Option(option = "projectVersion", description = "Set the current version.")
	public void setProjectVersion(String version) {
		this.version = version;
	}

	@TaskAction
	public void run() {
		if (VersionTaskHelper.isSpecified(alias)) {

			boolean isValidAlias = VersionTaskHelper.isValidAlias(alias, tomlFile);
			boolean isValidFile = VersionTaskHelper.isValidTomlFile(tomlFile);
			boolean isValidVersion = VersionTaskHelper.isValidVersion(version, VersionTaskHelper.VALID_VERSIONS_WITHOUT_DEV);

			VersionTaskHelper.isInvalidTomlProperties(!isValidFile, tomlFile, !isValidAlias, alias, !isValidVersion, version, VersionTaskHelper.VALID_VERSIONS_WITHOUT_DEV);
			updateCurrentVersion(tomlFile, alias, version);

		}
	}

	private void updateCurrentVersion(File file, String alias, String currentVersion) {

		String[] versionAndType = currentVersion.split("-");

		String[] numbers = versionAndType[0].split("\\.");
		int MAJOR = Integer.parseInt(numbers[0]);
		int MINOR = Integer.parseInt(numbers[1]);
		int PATCH = Integer.parseInt(numbers[2]);


		if(MAJOR == 0 && MINOR == 0 && PATCH == 0){
			MAJOR++;
		}else {
			if (MINOR < 999) {
				if (PATCH < 999) {
					PATCH += 1;
				} else {
					MINOR += 1;
					PATCH = 0;
				}
			} else {
				if (PATCH == 999) {
					MAJOR += 1;
					MINOR = 0;
					PATCH = 0;
				} else {
					PATCH += 1;
				}
			}
		}

		VersionTypes TYPE = null;
		if(versionAndType.length == 2){
			VersionTypes currentType = VersionTypes.toEnum(versionAndType[1]);
			TYPE = currentType == VersionTypes.RELEASE ? VersionTypes.SNAPSHOT : currentType;
		}

		String nextVersion;
		if(TYPE == null || TYPE.getStr().isEmpty()) nextVersion = MAJOR + "." + MINOR + "." + PATCH;
		else nextVersion = MAJOR + "." + MINOR + "." + PATCH + "-" + TYPE.getStr();

		String replaceWith = alias + " = " + "\"" + nextVersion + "\"";
		List<String> validVersions = VersionTaskHelper.createValidVersions(alias, VersionTaskHelper.VALID_VERSIONS);
		VersionTaskHelper.replaceLinesMatching(file.getAbsolutePath(), replaceWith, validVersions);
		setProjectVersion(nextVersion);
	}

}