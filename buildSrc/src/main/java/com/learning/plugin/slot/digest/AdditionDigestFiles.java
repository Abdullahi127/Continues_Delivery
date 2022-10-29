package com.learning.plugin.slot.digest;

import org.gradle.api.provider.MapProperty;
import org.gradle.api.provider.Property;

import java.io.File;

public abstract class AdditionDigestFiles {
	public abstract Property<String> getMessage();
	public abstract MapProperty<String, File> getMappedFiles();
}
