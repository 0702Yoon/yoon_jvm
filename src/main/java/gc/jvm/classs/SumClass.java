package gc.jvm.classs;

import gc.jvm.classs.type.ClassFileVersion;
import java.util.List;

public class SumClass extends JVMClass {

    public SumClass(ClassFileVersion classFileVersion) {
        super("Sum", classFileVersion, List.of(new Field("Integer", "result")));
    }

}
