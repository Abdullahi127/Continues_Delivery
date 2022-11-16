package com.learning.plugin.slot.digest;

import org.gradle.api.tasks.Nested;

import org.gradle.api.Action;
import org.gradle.api.file.FileCollection;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.bundling.Jar;
import org.gradle.api.tasks.bundling.Zip;

public abstract class DigestExtension {
	public abstract Property<FileCollection> getSourceDirectory();

	public abstract Property<FileCollection> getClassesDirectory();

	public abstract Property<TaskProvider<Jar>> getJarArchiveFile();

	public abstract Property<TaskProvider<Jar>> getSourcesJarArchiveFile();

	public abstract Property<TaskProvider<Zip>> getZipArchiveFile();

	@Nested
	public abstract AdditionDigestFiles getAdditionalDigestFiles();

	public void additionalDigestFiles(Action<? super AdditionDigestFiles> action){
		action.execute(getAdditionalDigestFiles());
	}
}
