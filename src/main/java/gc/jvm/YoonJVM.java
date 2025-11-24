package gc.jvm;

import gc.jvm.classs.ClassLoader;
import gc.jvm.classs.JVMClass;
import gc.jvm.classs.JVMObject;
import gc.jvm.classs.TempJVMClass;
import gc.jvm.classs.type.ClassFileVersion;
import gc.jvm.gc.GarbageCollector;
import gc.jvm.memory.RunTimeMemory;
import gc.jvm.runtime.Frame;
import java.util.Map;

public class YoonJVM {
    private final ClassLoader classLoader;
    private final RunTimeMemory runTimeMemory;
    private final GarbageCollector gc;

    private int objectId;

    public YoonJVM() {
        this.classLoader = new ClassLoader(ClassFileVersion.JAVA_17);
        this.runTimeMemory = new RunTimeMemory();
        this.gc = new GarbageCollector(runTimeMemory);
        this.objectId = 0;

        System.out.println("╔════════════════════════════════════════╗");
        System.out.println("║         Yoon JVM 초기화 완료           ║");
        System.out.println("╚════════════════════════════════════════╝");
    }

    public JVMObject createObject(JVMClass type) {
        objectId++;
        JVMObject obj = type.toJVMObject(objectId);

        try {
            runTimeMemory.allcate(obj);
            maybeTriggerAutoMinorGC("객체 생성");
            return obj;
        } catch (RuntimeException e) {
            System.out.println("[Fail] 객체 생성 실패: 신세대 메모리가 가득 참");
            System.out.println("  → GC를 먼저 실행해보세요");
            return null;
        }
    }

    @SuppressWarnings("unused")
    private void startMinorGC() {
        System.out.println("마이너 GC 실행");
        gc.minorGC();
    }

    /**
     * 메서드 호출 (Frame 생성 및 스택에 푸시)
     */
    public Frame invokeMethod(String methodName, int maxLocals) {
        Frame frame = new Frame(methodName, maxLocals);
        runTimeMemory.pushFrame(frame);
        System.out.println("메서드 호출됨: " + methodName);
        return frame;
    }

    /**
     * 메서드 리턴 (Frame 팝)
     */
    public Frame returnFromMethod() {
        if (runTimeMemory.isStackEmpty()) {
            System.out.println("[Fail] 리턴 불가: 스택이 비어있음");
            return null;
        }

        Frame frame = runTimeMemory.popFrame();
        System.out.println("[Success] 메서드 리턴됨: " + frame.getMethodName());
        return frame;
    }

    public void storeLocal(String identity, JVMObject obj) {
        Frame current = runTimeMemory.currentFrame();
        if (current == null) {
            System.out.println("[Fail] 활성화된 프레임이 없습니다. 먼저 메서드를 호출하세요.");
            return;
        }

        current.setLocalVariable(identity, obj);
        System.out.println("[Success] 지역 변수 '" + identity + "'가 " + current.getMethodName() + "에 설정됨\n");
    }

    public void runMinorGC() {
        gc.minorGC();
    }

    public void runMajorGC() {
        gc.majorGC();
    }

    public void runFullGC() {
        gc.fullGC();
    }

    public void printMemoryStatus() {
        System.out.println("\n" + "=".repeat(50));
        System.out.println("          메모리 상태");
        System.out.println("=".repeat(50));

        // GC Root 정보 출력
        Frame current = runTimeMemory.currentFrame();
        if (current != null) {
            Map<String, JVMObject> gcRoots = current.getAllLocalVariables();
            System.out.println("\n=== GC Roots (도달 가능한 객체) ===");
            System.out.printf("총 %d개의 GC Root\n", gcRoots.size());
            if (!gcRoots.isEmpty()) {
                for (Map.Entry<String, JVMObject> entry : gcRoots.entrySet()) {
                    System.out.printf("  • %s → Object[id=%d, type=%s, size=%dB]\n",
                        entry.getKey(),
                        entry.getValue().getId(),
                        entry.getValue().getClass().getSimpleName(),
                        entry.getValue().getSize());
                }
            }
            System.out.println();
        }

        runTimeMemory.printStatus();

        System.out.println("\n" + "=".repeat(50) + "\n");
    }

    /**
     * GC 통계 출력
     */
    public void printGCStatistics() {
        gc.printStatistics();
    }

    /**
     * 전체 상태 출력
     */
    public void printFullStatus() {
        printMemoryStatus();
        printGCStatistics();
    }

    public GarbageCollector getGc() {
        return gc;
    }

    public ClassLoader getClassLoader() {
        return classLoader;
    }


    public JVMClass getClassByName(String name) {
        return classLoader.findByName(name);
    }

    public void printCurrentObjects() {
        Frame current = runTimeMemory.currentFrame();
        Map<String, JVMObject> allLocalVariables = current.getAllLocalVariables();
        for (Map.Entry<String, JVMObject> entry : allLocalVariables.entrySet()) {
            System.out.println("id: " + entry.getKey() + " name: " + entry.getValue().toDetailString());
        }
    }

    public JVMObject createObjectWithReference(JVMClass jvmClass, String refNames) {
        objectId++;
        JVMObject obj = jvmClass.toJVMObject(objectId);

        // 메모리에 할당
        try {
            runTimeMemory.allcate(obj);
            maybeTriggerAutoMinorGC("객체 생성(참조 포함)");
        } catch (RuntimeException e) {
            System.out.println("[Fail] 객체 생성 실패: 신세대 메모리가 가득 참");
            System.out.println("  → GC를 먼저 실행해보세요");
            return null;
        }

        // 참조 추가
        Frame current = runTimeMemory.currentFrame();
        if (!refNames.isEmpty()) {
            String[] idList = refNames.split(",");
            for (String id : idList) {
                Map<String, JVMObject> allLocalVariables = current.getAllLocalVariables();
                JVMObject refObj = allLocalVariables.get(id.strip());
                if (refObj != null) {
                    obj.addReference(refObj);
                    System.out.println("  → 참조 추가됨: " + obj + " -> " + refObj);
                } else {
                    System.out.println("  [Fail] 객체 '" + id.strip() + "'를 찾을 수 없습니다");
                }
            }
        }

        return obj;
    }

    public void removeObjectReference(String name) {
        Frame current = runTimeMemory.currentFrame();
        if (current != null) {
            current.removeLocalVariable(name);
        }
    }

    public void registerClass(TempJVMClass tempClass) {
        classLoader.registerClass(tempClass);
    }

    private void maybeTriggerAutoMinorGC(String reason) {
        if (!runTimeMemory.isEdenFull()) {
            return;
        }
        logEdenUsage(reason + " 직전");
        gc.minorGC();
        System.out.println("자동 Minor GC 이후 메모리 상태:");
        runTimeMemory.printStatus();
    }

    private void logEdenUsage(String prefix) {
        int edenSize = runTimeMemory.getYoungGeneration().getEdenSize();
        int youngMax = runTimeMemory.getYoungGeneration().getMaxSize();
        double percent = edenSize * 100.0 / youngMax;
        System.out.printf("[%s] Eden 사용량: %d / %d 바이트 (%.1f%%)\n",
            prefix, edenSize, youngMax, percent);
    }
}
