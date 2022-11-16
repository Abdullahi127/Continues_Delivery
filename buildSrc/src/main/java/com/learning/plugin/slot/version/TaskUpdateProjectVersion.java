package com.learning.plugin.slot.version;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.options.Option;

import java.io.File;
import java.util.List;

import java.io.File;

public abstract class TaskUpdateProjectVersion extends DefaultTask {

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
        boolean isValidVersion = VersionTaskHelper.isValidVersion(version, VersionTaskHelper.VALID_VERSIONS_WITHOUT_DEV);
        VersionTaskHelper.isInvalidTomlProperties(!isValidFile, tomlFile.getName(), !isValidAlias, alias, !isValidVersion, version, VersionTaskHelper.VALID_VERSIONS_WITHOUT_DEV);

        String[] versionAndType = version.split("-");
        String[] numbers = versionAndType[0].split("\\.");

        String nextVersion;
        if (numbers.length == 3) {
            nextVersion = getNextVersion3(versionAndType, numbers);
        } else if (numbers.length == 2) {
            nextVersion = getNextVersion2(versionAndType, numbers);
        } else {
            nextVersion = getNextVersion1(versionAndType, numbers);
        }

        String replaceWith = alias + " = " + "\"" + nextVersion + "\"";
        List<String> validRegexVersions = VersionTaskHelper.createValidVersions(alias, VersionTaskHelper.VALID_VERSIONS);
        boolean failed = VersionTaskHelper.replaceLinesMatching(tomlFile.getAbsolutePath(), replaceWith, validRegexVersions);
        VersionTaskHelper.failedVersionReplacement(failed, alias);
        setProjectVersion(nextVersion);
    }

    private String getNextVersion1(String[] versionAndType, String[] numbers){

        VersionTypes TYPE = getVersionType(versionAndType);
        int MAJOR = Integer.parseInt(numbers[0]) + 1;
        if(TYPE == null || TYPE.getStr().isEmpty()){
            return "" + MAJOR;
        }
        return MAJOR + "-" + TYPE.getStr();
    }

    private String getNextVersion2(String[] versionAndType, String[] numbers){

        int MAJOR = Integer.parseInt(numbers[0]);
        int PATCH = Integer.parseInt(numbers[1]);

        if(MAJOR == 0 && PATCH == 0){
            MAJOR++;
        }else {

            if(PATCH < 999){
                PATCH += 1;
            }else {
                PATCH = 0;
                MAJOR += 1;
            }
        }

        VersionTypes TYPE = getVersionType(versionAndType);
        if(TYPE == null || TYPE.getStr().isEmpty()){
            return MAJOR + "." + PATCH;
        }
        return MAJOR + "." + PATCH + "-" + TYPE.getStr();
    }

    private String getNextVersion3(String[] versionAndType, String[] numbers){

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

        VersionTypes TYPE = getVersionType(versionAndType);

        if(TYPE == null || TYPE.getStr().isEmpty()) {
            return MAJOR + "." + MINOR + "." + PATCH;
        }
        return MAJOR + "." + MINOR + "." + PATCH + "-" + TYPE.getStr();
    }

    private VersionTypes getVersionType(String[] versionAndType) {
        if(versionAndType.length != 2) return null;

        VersionTypes currentType = VersionTypes.toEnum(versionAndType[1]);
        return currentType == VersionTypes.RELEASE ? VersionTypes.SNAPSHOT : currentType;
    }

}