package com.learning.plugin.slot.digest;

import org.gradle.api.DefaultTask;
import org.gradle.api.provider.MapProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class TaskDigestOnFiles extends DefaultTask {

	@Optional
	@Input
	public abstract  Property<String> getMessage();

	@Optional
	@Input
	public abstract MapProperty<String, File> getMappedFiles();

	@TaskAction
	public void execute(){

		if(getMessage().isPresent() && !getMessage().get().isEmpty()){
			System.out.println(getMessage().get());
		}

		if(getMappedFiles().isPresent() && !getMappedFiles().get().isEmpty()){
			for (Map.Entry<String, File> entry : getMappedFiles().get().entrySet()) {
				String message = entry.getKey();
				File file = entry.getValue();
				List<File> theFiles = new ArrayList<>();

				DigestTaskHelper.recursiveFindFiles(theFiles, file, 100, 0);
				//theFiles.sort(Comparator.comparingLong(f -> f.getAbsolutePath().length())); // Optional, sort on path length, Ascending order.
				DigestTaskHelper.printDigest(theFiles, message);
			}
		}
	}
}
