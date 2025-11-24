package gc.jvm.memory;

import gc.jvm.classs.JVMObject;
import gc.jvm.runtime.Frame;
import gc.jvm.runtime.JVMStack;

public class RunTimeMemory {

    // 메모리 크기 설정 (바이트)
    private static final int YOUNG_GEN_SIZE = 128;
    private static final int OLD_GEN_SIZE = 128;
    private static final int STACK_DEPTH = 10;

    // 런타임 메모리 영역
    private final YoungGeneration youngGeneration;
    private final OldGeneration oldGeneration;
    private final JVMStack stack;

    public YoungGeneration getYoungGeneration() {
        return youngGeneration;
    }

    public OldGeneration getOldGeneration() {
        return oldGeneration;
    }

    public JVMStack getStack() {
        return stack;
    }

    public RunTimeMemory() {
        this.youngGeneration = new YoungGeneration(YOUNG_GEN_SIZE);
        this.oldGeneration = new OldGeneration(OLD_GEN_SIZE);
        this.stack = new JVMStack(STACK_DEPTH);
        ReferenceTracker.initialize(youngGeneration, oldGeneration);

        System.out.println();
        System.out.printf("신세대(Young Generation): %d KB\n", YOUNG_GEN_SIZE / 1024);
        System.out.printf("구세대(Old Generation): %d KB\n", OLD_GEN_SIZE / 1024);
        System.out.printf("최대 스택 깊이: %d\n", STACK_DEPTH);
        System.out.println();
    }

    public JVMObject allcate(JVMObject obj) {
        int remainingYoungSpace = youngGeneration.getAvailableSpace();

        if (obj.getSize() > remainingYoungSpace) {
            System.out.printf("️[Issue] 객체 크기(%dB)가 신세대 잔여 공간(%dB)을 초과합니다. 구세대에 직접 할당합니다.%n",
                obj.getSize(), remainingYoungSpace);
            if (oldGeneration.allocate(obj)) {
                System.out.println("[Success] 객체 생성됨: " + obj.toDetailString());
                System.out.println("  → 대형 객체가 구세대에 직접 할당됨");
                return obj;
            }
            throw new RuntimeException("구세대 메모리가 가득 참");
        }

        if (youngGeneration.allocate(obj)) {
            System.out.println("[Success] 객체 생성됨: " + obj.toDetailString());
            System.out.println("  → Eden 영역(신세대)에 할당됨");
            return obj;
        }

        throw new RuntimeException("Eden 영역이 가득 참");
    }

    public void pushFrame(Frame frame) {
        stack.pushFrame(frame);
    }

    public boolean isStackEmpty() {
        return stack.isEmpty();
    }

    public Frame popFrame() {
        return stack.popFrame();
    }

    public Frame currentFrame() {
        return stack.currentFrame();
    }

    public void printStatus() {
        youngGeneration.printStatus();
        oldGeneration.printStatus();
        stack.printStack();
    }

    public boolean isEdenFull() {
        return youngGeneration.isEdenFull();
    }
}

