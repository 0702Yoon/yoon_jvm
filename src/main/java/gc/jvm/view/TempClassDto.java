package gc.jvm.view;

import gc.jvm.classs.Field;
import gc.jvm.classs.TempJVMClass;
import gc.jvm.classs.type.ClassFileVersion;
import java.util.List;

public record TempClassDto(
    String name,
    List<Field> fieldList
) {
    public TempJVMClass toEntity(ClassFileVersion classFileVersion) {
        return new TempJVMClass(name, classFileVersion, fieldList);
    }
}
