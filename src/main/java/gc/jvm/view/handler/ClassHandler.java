package gc.jvm.view.handler;

import gc.jvm.YoonJVM;
import gc.jvm.classs.TempJVMClass;
import gc.jvm.classs.type.ClassFileVersion;
import gc.jvm.view.TempClassDto;
import gc.jvm.view.View;

/**
 * 클래스 관리 작업을 처리하는 핸들러
 */
public class ClassHandler {
    private final YoonJVM jvm;
    private final View view;

    public ClassHandler(YoonJVM jvm, View view) {
        this.jvm = jvm;
        this.view = view;
    }

    /**
     * 새로운 클래스 생성
     */
    public void createClass() {
        TempClassDto tempClass = view.createTempClass();
        if (tempClass == null) {
            view.println("[Fail] 클래스 생성이 취소되었습니다.");
            return;
        }
        TempJVMClass jvmClass = tempClass.toEntity(ClassFileVersion.JAVA_17);
        jvm.registerClass(jvmClass);
        view.println("[Success] " + jvmClass.getName() + "가 성공적으로 등록되었습니다.");
    }
}
