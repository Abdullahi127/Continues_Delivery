package com.learning.plugin.slot.version;

import org.gradle.api.GradleException;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class VersionTaskHelper {

	public static final String UNSPECIFIED = "unspecified";
	public static final String VALID_TOML_FILE_NAME = "libs.versions.toml";
	public static final String REGULAR_EXPRESSION_DIGEST = "\\d+\\.\\d+\\.\\d+";
	public static final String REGULAR_EXPRESSION_DIGEST_WITH_SNAPSHOT = "\\d+\\.\\d+\\.\\d+-SNAPSHOT";
	public static final String REGULAR_EXPRESSION_DIGEST_WITH_RELEASE = "\\d+\\.\\d+\\.\\d+-RELEASE";
	public static final String[] VALID_TOML_ALIAS_VALUES = new String[4];
	public static final String[] VALID_GRADLE_VERSIONS = new String[3];

	static {
		VALID_TOML_ALIAS_VALUES[0] = "PLACEHOLDER";
		VALID_TOML_ALIAS_VALUES[1] = REGULAR_EXPRESSION_DIGEST;
		VALID_TOML_ALIAS_VALUES[2] = REGULAR_EXPRESSION_DIGEST_WITH_SNAPSHOT;
		VALID_TOML_ALIAS_VALUES[3] = REGULAR_EXPRESSION_DIGEST_WITH_RELEASE;

		VALID_GRADLE_VERSIONS[0] = REGULAR_EXPRESSION_DIGEST;
		VALID_GRADLE_VERSIONS[1] = REGULAR_EXPRESSION_DIGEST_WITH_SNAPSHOT;
		VALID_GRADLE_VERSIONS[2] = REGULAR_EXPRESSION_DIGEST_WITH_RELEASE;
	}

	/**
	 * Create and return an array list filled with valid TOML alias names.
	 *
	 * @param alias		the alias that will be used to create the regex names with.
	 * @return an array list filled with valid TOML alias names.
	 */
	public static List<String> createValidRegexNames(String alias){
		List<String> validRegexes = new ArrayList<>();
		for(String value : VALID_TOML_ALIAS_VALUES){
			String regex = alias + "=\"" + value + "\"";
			validRegexes.add(regex);
		}
		return validRegexes;
	}

	/**
	 * Replace the lines that matches with the given {@code regexes}.
	 *
	 * <li>The stream {@code lines} contains a reference to an open file. The file is closed by closing the stream.</li>
	 *
	 * @param pathToFile		the file that will be modified.
	 * @param replacementLine	the replacement line that will be used.
	 * @param regexes			an array list where the regexes that will be used to check each line in the {@code pathToFile}
	 */
	public static boolean replaceLinesMatching(String pathToFile, String replacementLine, List<String> regexes){
		Path path = Paths.get(pathToFile);
		List<String> newLines;
		try {
			Stream<String> lines = Files.lines(path, StandardCharsets.UTF_8);
			newLines = lines.map(line -> replaceLine(regexes, line, replacementLine)).collect(Collectors.toList());
			Files.write(path, newLines, StandardCharsets.UTF_8);
			lines.close();
		}catch (IOException e) {
			throw new RuntimeException(e);
		}
		return !newLines.contains(replacementLine);
	}

	/**
	 * Replace the line that match the given list {@code regexes}.
	 *
	 * @param regexes 		an array where the regexes can be found.
	 * @param line 	  		the string that will be modified.
	 * @param replaceWith   the string that will replace the given {@code line}.
	 */
	public static String replaceLine(List<String> regexes, String line, String replaceWith){
		String trim = line.replace(" ", "");
		for(String regex : regexes){
			if (trim.matches(regex)) {
				line = replaceWith;
				break;
			}
		}
		return line;
	}

	/**
	 * Throw runtime exception if any of the given {@code isValidFile}, {@code isValidAlias} and {@code isValidVersion} are true.
	 *
	 * @param isInvalidFile		True - The given file is not a correct toml-file.
	 * @param fileName			the invalid file name.
	 * @param isInvalidAlias	True - The given alias could not be found.
	 * @param alias				the alias that must exist in the given {@code file}.
	 * @param isInvalidVersion 	True - The given version is not a valid version.
	 * @param version			the version that must be correct.
	 */
	public static void isInvalidTomlProperties(boolean isInvalidFile, String fileName, boolean isInvalidAlias, String alias, boolean isInvalidVersion, String version) {
		if (isInvalidFile) {
			throw new GradleException("Invalid file name [" + fileName + "].The file name must be ["+ VALID_TOML_FILE_NAME+"].");
		}

		if (isInvalidAlias) {
			throw new GradleException("Invalid alias [" + alias + "].The alias could not be found in the toml file.");
		}

		if (isInvalidVersion) {
			throw new GradleException("Invalid version [" + version + "].The version must be one of this: " + Arrays.toString(VALID_GRADLE_VERSIONS) + ".");
		}
	}

	/**
	 * Check if the given file is valid.
	 *
	 * @param tomlFile	the file that will be checked.
	 * @return True - If the file name is libs.versions.toml.
	 */
	public static boolean isValidTomlFile(File tomlFile) {
		if(tomlFile.exists()){
			return tomlFile.getName().equalsIgnoreCase(VALID_TOML_FILE_NAME);
		}
		return false;
	}

	/**
	 * Check if the string is valid.
	 *
	 * @param str the string that will be checked.
	 * @return True - If the string is not null, empty and blank.
	 */
	public static boolean isValidString(String str) {
		return str != null && !str.isEmpty() && !str.isBlank();
	}

	/**
	 * Check if the given {@code version} is valid.
	 *
	 * @param version the version that will be checked.
	 * @return True - If the given {@code version} match with any of the {@code VALID_GRADLE_VERSIONS}.
	 */
	public static boolean isValidVersion(String version){
		final boolean validateString = isValidString(version);
		if(validateString){
			for(String regex : VALID_GRADLE_VERSIONS){
				if(version.matches(regex)){
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Check if the given {@code alias} is valid.
	 *
	 * @param alias	 the string that will be checked.
	 * @param file	 the file where the given {@code alias} must exists in i.e. the TOML file.
	 * @return True - If the given {@code alias} string is not null, empty, blank and exists in the given {@code file}.
	 */
	public static boolean isValidAlias(String alias, File file){
		boolean validateString = isValidString(alias);
		if(validateString){
			try {
				final List<String> lines = Files.readAllLines(Path.of(file.getAbsolutePath()));
				for(String line : lines){
					if(line.contains(alias)){
						return true;
					}
				}
			}catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		return false;
	}

	/**
	 * Check if the given string is unspecified.
	 *
	 * @param str	  the string that will be checked.
	 * @return True - the string is equal to the word 'unspecified'.
	 */
	public static boolean isUnspecified(String str){
		return str.equalsIgnoreCase(UNSPECIFIED);
	}

	/**
	 * Throw runtime exception if the given {@code str} is unspecified.
	 *
	 * @param str				the string that will be checked.
	 * @param exceptionMessage	the execution message that will be used.
	 */
	public static void isUnspecified(String str, String exceptionMessage){
		if(isUnspecified(str)){
			throw new GradleException(exceptionMessage);
		}
	}


	/**
	 * Throw runtime exception if the given {@code alias} version was not updated.
	 *
	 * @param alias		the name of the version.
	 * @param failed	True - The version was not updated.
	 */
	public static void failedVersionReplacement(String alias, boolean failed){
		if(failed){
			throw new GradleException("Failed to update the toml file. Line contain: " + alias);
		}
	}
}
