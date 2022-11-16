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
	@InputFiles
	public abstract Property<TaskProvider<Jar>> getJarTask();

	@Optional
	@InputFiles
	public abstract Property<TaskProvider<Jar>> getSourceJarTask();

	@Optional
	@InputFiles
	public abstract Property<TaskProvider<Zip>> getZipTask();

	@TaskAction
	public void execute(){

		DigestTaskHelper.printFileCollection(getSourceDirectory().get(), "\nCompile files:");
		DigestTaskHelper.printFileCollection(getSourceDirectory().get(), "\nJava compile input digests:");
		DigestTaskHelper.printFileCollection(getClassesDirectory().get(), "\nJava compile output digests:");

		DigestTaskHelper.digestOnFile(getJarTask().get().get().getArchiveFile().get().getAsFile(), "\nJar output digest:");
		DigestTaskHelper.digestOnFile(getSourceJarTask().get().get().getArchiveFile().get().getAsFile(), "\nSource jar output digest:");
		DigestTaskHelper.digestOnFile(getZipTask().get().get().getArchiveFile().get().getAsFile(), "\nZip output digest:");

	}

}
