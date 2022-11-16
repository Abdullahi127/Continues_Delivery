package com.learning.plugin.slot.digest;

import org.gradle.api.provider.MapProperty;

import java.io.File;

public abstract class AdditionDigestFiles {
	public abstract MapProperty<String, File> getMappedFiles();
}
