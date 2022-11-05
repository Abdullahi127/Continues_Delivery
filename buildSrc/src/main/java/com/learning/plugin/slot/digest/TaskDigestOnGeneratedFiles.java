package com.learning.plugin.slot.digest;

import org.gradle.api.DefaultTask;
import org.gradle.api.file.FileCollection;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.*;
import org.gradle.api.tasks.bundling.Jar;
import org.gradle.api.tasks.bundling.Zip;

public abstract class TaskDigestOnGeneratedFiles extends DefaultTask {

	@Optional
	@InputFiles
	public abstract Property<FileCollection> getSourceDirectory();

	@Optional
	@InputFiles
	public abstract Property<FileCollection> getClassesDirectory();

	@Optional
	@Input
	public abstract Property<TaskProvider<Jar>> getJarFile();

	@Optional
	@Input
	public abstract Property<TaskProvider<Jar>> getSourceJarFile();

	@Optional
	@Input
	public abstract Property<TaskProvider<Zip>> getZipFile();

	@TaskAction
	public void execute(){

		DigestTaskHelper.printFileCollection(getSourceDirectory(), "\nCompile files:");
		DigestTaskHelper.digestFileCollection(getSourceDirectory(), "\nJava compile input digests:");
		DigestTaskHelper.digestFileCollection(getClassesDirectory(), "\nJava compile output digests:");

		DigestTaskHelper.digestOnFile(getJarFile().get().get().getArchiveFile().get().getAsFile(), "\nJar output digest:");
		DigestTaskHelper.digestOnFile(getSourceJarFile().get().get().getArchiveFile().get().getAsFile(), "\nSource jar output digest:");
		DigestTaskHelper.digestOnFile(getZipFile().get().get().getArchiveFile().get().getAsFile(), "\nZip output digest:");

	}
}
