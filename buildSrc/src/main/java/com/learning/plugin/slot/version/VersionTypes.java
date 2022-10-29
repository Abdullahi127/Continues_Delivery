package com.learning.plugin.slot.version;

public enum VersionTypes {
	RELEASE("RELEASE"), SNAPSHOT("SNAPSHOT"), EMPTY("");

	private final String str;

	VersionTypes(String str) {
		this.str = str;
	}

	public String getStr() {
		return str;
	}

	public static String toString(VersionTypes type) {
		return switch (type) {
			case RELEASE -> VersionTypes.RELEASE.str;
			case SNAPSHOT -> VersionTypes.SNAPSHOT.str;
			default -> throw new RuntimeException("The type must be RELEASE or SNAPSHOT. The type is [" + type + "].");
		};
	}

	public static VersionTypes toEnum(String str) {
		return switch (str.toUpperCase()) {
			case "RELEASE" -> VersionTypes.RELEASE;
			case "SNAPSHOT" -> VersionTypes.SNAPSHOT;
			default -> throw new RuntimeException("The String must be RELEASE or SNAPSHOT. The String is [" + str + "].");
		};
	}
}
