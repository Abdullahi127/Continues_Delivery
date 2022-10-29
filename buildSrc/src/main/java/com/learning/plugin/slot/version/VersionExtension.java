package com.learning.plugin.slot.version;

import org.gradle.api.provider.Property;

import java.io.File;

public abstract class VersionExtension {
	public abstract Property<File> getTomlFile();
	public abstract Property<String> getAlias();
	public abstract Property<String> getProjectVersion();
}
