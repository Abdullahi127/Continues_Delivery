package com.learning.plugin.slot.digest;

import org.gradle.api.file.FileCollection;
import org.gradle.api.file.FileTree;
import org.gradle.api.provider.Property;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;
import java.util.Objects;

public class DigestTaskHelper {

	private static final MessageDigest SHA1;
	private static final MessageDigest SHA256;

	static {
		try {
			SHA1 = MessageDigest.getInstance("SHA-1");
			SHA256 = MessageDigest.getInstance("SHA-256");
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}

	public static void printDigest(List<File> files, String message){
		System.out.println(message);
		for(File file : files){
			printDigest(file);
		}
	}

	public static void recursiveFindFiles(List<File> files, File nextFile, int maxDepthCount, int count){
		if(count >= maxDepthCount) return;
		if (nextFile.isFile()) {
			files.add(nextFile);
		} else {
			for (File file : Objects.requireNonNull(nextFile.listFiles())) {
				recursiveFindFiles(files, file, maxDepthCount, ++count);
			}
		}
	}

	public static void printFileCollection(Property<FileCollection> fileCollection, String message){
		if(fileCollection.isPresent()){
			FileTree fileTree = fileCollection.get().getAsFileTree();
			if(!fileTree.isEmpty()){
				printFiles(fileTree, message);
			}
		}
	}

	public static void printFiles(FileTree fileTree, String message){
		System.out.println(message);
		for(File file : fileTree) {
			System.out.println(file.getAbsolutePath());
		}
	}


	public static void digestFileCollection(Property<FileCollection> fileCollection, String message) {
		if(fileCollection.isPresent()){
			FileTree fileTree = fileCollection.get().getAsFileTree();
			if(!fileTree.isEmpty()){
				printDigest(fileTree, message);
			}
		}
	}

	public static void digestOnFile(File file, String message) {
		if (file.exists() && file.isFile()) {
			printDigest(file, message);
		}
	}

	public static void printDigest(FileTree fileTree, String message) {
		System.out.println(message);
		for(File file : fileTree) {
			printDigest(file);
		}
	}

	public static void printDigest(File file, String message){
		System.out.println(message);
		printDigest(file);
	}

	public static void printDigest(File file) {
		try {
			final byte[] bytes = Files.readAllBytes(Path.of(file.getAbsolutePath()));
			final String sha1AsString = HexFormat.of().formatHex(SHA1.digest(bytes));
			final String sha256AsString = HexFormat.of().formatHex(SHA256.digest(bytes));
			System.out.println(file.getAbsolutePath()+" [SHA-1]: "+sha1AsString+" [SHA-256]: "+sha256AsString);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
