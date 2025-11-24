package gc.jvm.runtime;

import gc.jvm.classs.JVMObject;
import java.util.HashMap;
import java.util.Map;

/**
 * JVM Stack Frame 메서드 호출 시 생성되며, 지역 변수와 피연산자 스택을 포함
 */
public class Frame {
    private final String methodName;                    // 메서드 이름
    private final Map<String, JVMObject> localVariables; // 지역 변수 테이블
    private final int maxLocals;                        // 최대 지역 변수 개수

    public Frame(String methodName, int maxLocals) {
        this.methodName = methodName;
        this.maxLocals = maxLocals;
        this.localVariables = new HashMap<>();
    }

    public void setLocalVariable(String identity, JVMObject obj) {
        if (localVariables.size() < maxLocals || localVariables.containsKey(identity)) {
            localVariables.put(identity, obj);
        } else {
            throw new RuntimeException("Local variable table is full");
        }
    }

    /**
     * 지역 변수에서 객체 가져오기
     */
    public JVMObject getLocalVariable(String name) {
        return localVariables.get(name);
    }

    /**
     * 지역 변수 제거
     */
    public void removeLocalVariable(String name) {
        JVMObject remove = localVariables.remove(name);
        if (remove == null) {
            throw new RuntimeException("Local variable table is empty");
        }
    }

    /**
     * 모든 지역 변수 가져오기 (GC Root로 사용)
     */
    public Map<String, JVMObject> getAllLocalVariables() {
        return new HashMap<>(localVariables);
    }

    /**
     * Frame 정보 출력
     */
    public void printFrame() {
        System.out.printf("Frame[method=%s, locals=%d/%d]\n",
            methodName, localVariables.size(), maxLocals);

        if (!localVariables.isEmpty()) {
            System.out.println("  Local Variables:");
            localVariables.forEach((name, obj) -> {
                System.out.printf("    %s -> %s\n", name, obj);
            });
        }
    }

    public String getMethodName() {
        return methodName;
    }

    public Map<String, JVMObject> getLocalVariables() {
        return localVariables;
    }

}
