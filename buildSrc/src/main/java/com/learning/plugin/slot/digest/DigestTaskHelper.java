package com.learning.plugin.slot.digest;

import org.gradle.api.file.FileCollection;
import org.gradle.api.file.FileTree;
import org.gradle.api.provider.Property;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;

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

	public static void recursiveFindFiles(List<File> files, File nextFile, int count, int maxDepthCount){
		if(count >= maxDepthCount) return;
		if (nextFile.isFile()) {
			files.add(nextFile);
		} else {
			File[] nextFiles = nextFile.listFiles();
			if (nextFiles == null) {
				throw new RuntimeException("The list of files must not be null. Path to the directory: " + nextFile.getAbsolutePath());
			} else {
				for (File file : nextFiles) {
					recursiveFindFiles(files, file, ++count, maxDepthCount);
				}
			}
		}
	}

	public static void printFileCollection(FileCollection fileCollection, String message){
		if(!fileCollection.isEmpty()){
			FileTree fileTree = fileCollection.getAsFileTree();
			if(!fileTree.isEmpty()){
				printFiles(fileTree, message);
			}
		}
	}

	public static void printFileCollection(Property<FileCollection> fileCollection, String message, String property){
		FileTree fileTree = fileCollection.get().getAsFileTree();
		if(fileTree.isEmpty()){
			throw new RuntimeException("The file collection must not be empty. Property: "+property);
		}
		printDigest(fileTree, message);
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

	public static void digestOnFileCollections(Property<FileCollection> fileCollection, String message, String property){
		FileTree fileTree = fileCollection.get().getAsFileTree();
		if(fileTree.isEmpty()){
			throw new RuntimeException("The file collection must not be empty. Property: "+property);
		}
		printDigest(fileTree, message);
	}

	public static void digestFileCollection(FileCollection fileCollection, String message) {
		if(!fileCollection.isEmpty()){
			FileTree fileTree = fileCollection.getAsFileTree();
			if(!fileTree.isEmpty()){
				printDigest(fileTree, message);
			}
		}
	}

	public static void digestOnFile(Property<File> fileProperty, String message)  {
		if(fileProperty.isPresent()){
			File file = fileProperty.get().getAbsoluteFile();
			if (file.exists() && file.isFile()) {
				printDigest(file, message);
			}
		}
	}

	public static void digestOnFile(File file, String message){
		if(!file.exists()) throw new RuntimeException("The abstract pathname to the file must exist.");
		if(!file.isFile()) throw new RuntimeException("The abstract pathname to the file must not be a directory. Path: "+file.getAbsolutePath());

		printDigest(file, message);
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
