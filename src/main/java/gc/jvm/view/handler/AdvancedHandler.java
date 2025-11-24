package gc.jvm.view.handler;

import gc.jvm.YoonJVM;
import gc.jvm.classs.JVMObject;
import gc.jvm.classs.SumClass;
import gc.jvm.classs.type.ClassFileVersion;
import gc.jvm.view.View;

/**
 * 고급 기능을 처리하는 핸들러
 */
public class AdvancedHandler {
    private final YoonJVM jvm;
    private final View view;

    public AdvancedHandler(YoonJVM jvm, View view) {
        this.jvm = jvm;
        this.view = view;
    }

    /**
     * 여러 객체 생성 (스트레스 테스트)
     * 생성된 객체들은 자동으로 GC Root에 "stress_1", "stress_2" ... 형태로 저장됨
     */
    public void createMultipleObjects() {
        int count = view.readInt("\n생성할 객체의 개수를 입력하세요: ");
        int created = 0;
        int stored = 0;

        for (int i = 0; i < count; i++) {
            JVMObject obj = jvm.createObject(new SumClass(ClassFileVersion.JAVA_17));
            if (obj != null) {
                created++;
                // GC Root에 자동 저장 (객체가 수집되지 않도록)
                String objName = "stress_" + (i + 1);
                jvm.storeLocal(objName, obj);
                stored++;
            }
        }

        view.println("\n[Success] " + count + "개 중 " + created + "개의 객체를 생성했습니다");
        view.println("  " + stored + "개의 객체가 GC Root에 저장되었습니다 (stress_1 ~ stress_" + stored + ")");
    }

    /**
     * 메서드 호출 (새 프레임 푸시)
     */
    public void invokeMethod() {
        String methodName = view.readLine("\n메서드 이름을 입력하세요: ");
        int maxLocals = view.readInt("최대 로컬 변수 개수를 입력하세요: ");
        jvm.invokeMethod(methodName, maxLocals);
    }

    /**
     * 메서드 복귀 (프레임 팝)
     */
    public void returnFromMethod() {
        jvm.returnFromMethod();
    }
}
