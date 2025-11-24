package gc.jvm.classs;

import gc.jvm.classs.type.ClassFileVersion;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ClassLoader {
    private final List<JVMClass> classes;

    public ClassLoader(ClassFileVersion classFileVersion) {
        List<JVMClass> initialClasses = new ArrayList<>();
        initialClasses.add(new SumClass(classFileVersion));

        this.classes = initialClasses.stream()
            .filter(JVMClass::isValidMagicNumber)
            .filter(e -> e.isValidFileVersion(classFileVersion))
            .collect(Collectors.toList());
    }

    public List<String> getLoadClasses() {
        return classes.stream().map(JVMClass::getName).toList();
    }

    public JVMClass findByName(String name) {
        return classes.stream().filter(e -> e.getName().equals(name)).findFirst()
            .orElseThrow(() -> new IllegalArgumentException("없는 클래스명입니다."));
    }

    public void registerClass(TempJVMClass tempClass) {
        classes.add(tempClass);
    }
}
