package gc.jvm.classs.type;

/**
 * 클래스 파일 버전 JDK 버전별로 major.minor 버전 정의
 */
public enum ClassFileVersion {
    JAVA_1_1(45, 3, "JDK 1.1"),
    JAVA_1_2(46, 0, "JDK 1.2"),
    JAVA_1_3(47, 0, "JDK 1.3"),
    JAVA_1_4(48, 0, "JDK 1.4"),
    JAVA_5(49, 0, "Java SE 5"),
    JAVA_6(50, 0, "Java SE 6"),
    JAVA_7(51, 0, "Java SE 7"),
    JAVA_8(52, 0, "Java SE 8"),
    JAVA_9(53, 0, "Java SE 9"),
    JAVA_10(54, 0, "Java SE 10"),
    JAVA_11(55, 0, "Java SE 11"),
    JAVA_12(56, 0, "Java SE 12"),
    JAVA_13(57, 0, "Java SE 13"),
    JAVA_14(58, 0, "Java SE 14"),
    JAVA_15(59, 0, "Java SE 15"),
    JAVA_16(60, 0, "Java SE 16"),
    JAVA_17(61, 0, "Java SE 17");

    private final int majorVersion;
    private final int minorVersion;
    private final String description;

    ClassFileVersion(int majorVersion, int minorVersion, String description) {
        this.majorVersion = majorVersion;
        this.minorVersion = minorVersion;
        this.description = description;
    }

    public int getMajorVersion() {
        return majorVersion;
    }

    public int getMinorVersion() {
        return minorVersion;
    }

    public String getDescription() {
        return description;
    }

    /**
     * major.minor 형식의 문자열 반환
     */
    public String getVersionString() {
        return majorVersion + "." + minorVersion;
    }

    /**
     * major 버전으로 ClassFileVersion 찾기
     */
    public static ClassFileVersion fromMajorVersion(int major) {
        for (ClassFileVersion version : values()) {
            if (version.majorVersion == major) {
                return version;
            }
        }
        return null;
    }

    /**
     * major.minor 버전으로 ClassFileVersion 찾기
     */
    public static ClassFileVersion fromVersion(int major, int minor) {
        for (ClassFileVersion version : values()) {
            if (version.majorVersion == major && version.minorVersion == minor) {
                return version;
            }
        }
        return null;
    }

    /**
     * 버전 호환성 확인 (하위 버전은 상위 버전에서 실행 가능)
     */
    public boolean isCompatibleWith(int majorVersion, int minorVersion) {
        return this.majorVersion <= majorVersion && this.minorVersion <= minorVersion;
    }

    @Override
    public String toString() {
        return description + " (v" + getVersionString() + ")";
    }
}
