package gc.jvm.classs;

import gc.jvm.classs.type.ClassFileVersion;
import java.util.List;

/**
 * 임시 JVM 클래스 사용자가 입력한 정보로 동적으로 생성되는 클래스 JVMObject를 상속받아 GC 대상이 되며, 필드 정보를 가짐
 */
public class TempJVMClass extends JVMClass {


    public TempJVMClass(String className, ClassFileVersion classFileVersion, List<Field> fields) {
        super(className, classFileVersion, fields);
    }
}
