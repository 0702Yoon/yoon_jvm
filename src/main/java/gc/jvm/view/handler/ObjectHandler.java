package gc.jvm.view.handler;

import gc.jvm.YoonJVM;
import gc.jvm.classs.JVMClass;
import gc.jvm.classs.JVMObject;
import gc.jvm.view.View;
import java.util.List;

/**
 * 객체 관리 작업을 처리하는 핸들러
 */
public class ObjectHandler {
    private final YoonJVM jvm;
    private final View view;

    public ObjectHandler(YoonJVM jvm, View view) {
        this.jvm = jvm;
        this.view = view;
    }

    /**
     * 새 객체 생성
     */
    public void createObject() {
        List<String> loadClasses = jvm.getClassLoader().getLoadClasses();
        String selectClass = view.selectClass(loadClasses);
        JVMClass jvmClass = jvm.getClassByName(selectClass);

        if (jvmClass == null) {
            view.println("클래스를 찾을 수 없습니다.");
            return;
        }

        JVMObject createdObj = jvm.createObject(jvmClass);

        if (createdObj != null) {
            String objName = view.readLine("\n생성된 객체의 변수명을 입력하세요: ");
            if (!objName.isEmpty()) {
                jvm.storeLocal(objName, createdObj);
                view.println("[Success] 객체 '" + objName + "'가 GC Root에 저장되었습니다");
            }
        }
    }

    /**
     * 참조를 가진 객체 생성
     */
    public void createObjectWithReference() {
        List<String> loadClasses = jvm.getClassLoader().getLoadClasses();
        String selectClass = view.selectClass(loadClasses);
        JVMClass classByName = jvm.getClassByName(selectClass);
        jvm.printCurrentObjects();
        String refNames = view.selectReferenceObj();

        JVMObject createdObj = jvm.createObjectWithReference(classByName, refNames);

        if (createdObj != null) {
            String objName = view.readLine("\n생성된 객체의 변수명을 입력하세요: ");
            if (!objName.isEmpty()) {
                jvm.storeLocal(objName, createdObj);
                view.println("[Success] 객체 '" + objName + "'가 참조와 함께 생성되고 GC Root에 저장되었습니다");
            }
        }
    }

    /**
     * 객체 참조 제거 (GC Root에서 제거)
     */
    public void removeObjectReference() {
        view.println("\nGC Root에서 제거할 객체 변수명을 입력하세요:");
        jvm.printCurrentObjects();
        String name = view.readLine();
        try {
            jvm.removeObjectReference(name);
            view.println("[Success] 객체 '" + name + "'가 GC Root에서 제거되었습니다");
        } catch (RuntimeException e) {
            view.println("[Fail] 객체 '" + name + "'를 찾을 수 없습니다");
        }
    }

    /**
     * 이름 지정된 모든 객체 목록 출력
     */
    public void listNamedObjects() {
        view.println("\n=== 이름 지정 객체 목록 (GC Roots) ===");
        jvm.printCurrentObjects();
    }
}
