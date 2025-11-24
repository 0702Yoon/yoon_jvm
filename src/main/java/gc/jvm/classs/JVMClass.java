package gc.jvm.classs;

import gc.jvm.classs.type.ClassFileVersion;
import java.util.ArrayList;
import java.util.List;

public class JVMClass {
    public static final int MAGIC_NUMBER = 0xCAFEBABE;
    public static final int DEFAULT_SIZE = 4;

    private final int magic;                              // 매직넘버 (0xCAFEBABE)
    private final int minorVersion;                       // 클래스 파일의 부 버전
    private final int majorVersion;                       // 클래스 파일의 주 버전
    private final String className;
    private final List<Field> fields;

    /**
     * 기본 생성자 매직넘버를 CAFEBABE로 초기화
     */
    public JVMClass(String className, ClassFileVersion version, List<Field> fields) {
        this.magic = MAGIC_NUMBER;
        this.minorVersion = version.getMinorVersion();
        this.majorVersion = version.getMajorVersion();
        this.fields = fields;
        this.className = className;
    }


    public boolean isValidMagicNumber() {
        return magic == MAGIC_NUMBER;
    }

    public String getName() {
        return className;
    }


    public int getClassSize() {
        return fields.size() * DEFAULT_SIZE;
    }

    public JVMObject toJVMObject(int objectId) {
        int totalSize = this.getClassSize() + JVMObject.OBJECT_HEADER_SIZE;
        return new JVMObject(
            objectId,
            this,
            totalSize,
            new ArrayList<>(this.fields)
        );
    }

    public boolean isValidFileVersion(ClassFileVersion classFileVersion) {
        return classFileVersion.isCompatibleWith(majorVersion, minorVersion);
    }

    public int getMajorVersion() {
        return majorVersion;
    }

    public int getMinorVersion() {
        return minorVersion;
    }
}
