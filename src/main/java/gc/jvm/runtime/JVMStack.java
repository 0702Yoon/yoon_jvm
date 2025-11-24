package gc.jvm.runtime;

import gc.jvm.classs.JVMObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;

/**
 * JVM Stack (스레드별 스택) Frame들을 관리하며, GC Root로 동작
 */
public class JVMStack {
    private final Stack<Frame> frames;
    private final int maxDepth;

    public JVMStack(int maxDepth) {
        this.frames = new Stack<>();
        this.maxDepth = maxDepth;
    }

    /**
     * Frame 푸시 (메서드 호출)
     */
    public void pushFrame(Frame frame) {
        if (frames.size() >= maxDepth) {
            throw new StackOverflowError("Stack depth exceeded: " + maxDepth);
        }
        frames.push(frame);
    }

    /**
     * Frame 팝 (메서드 리턴)
     */
    public Frame popFrame() {
        if (frames.isEmpty()) {
            throw new RuntimeException("Cannot pop from empty stack");
        }
        return frames.pop();
    }

    /**
     * 현재 Frame 가져오기
     */
    public Frame currentFrame() {
        if (frames.isEmpty()) {
            return null;
        }
        return frames.peek();
    }

    /**
     * 모든 Frame의 지역 변수를 GC Root로 수집
     */
    public Set<JVMObject> collectGCRoots() {
        Set<JVMObject> roots = new HashSet<>();

        for (Frame frame : frames) {
            for (JVMObject obj : frame.getLocalVariables().values()) {
                if (obj != null) {
                    roots.add(obj);
                }
            }
        }

        return roots;
    }

    /**
     * 스택 상태 출력
     */
    public void printStack() {
        System.out.println("\n=== JVM Stack ===");
        System.out.printf("Depth: %d / %d\n", frames.size(), maxDepth);

        if (frames.isEmpty()) {
            System.out.println("(Empty)");
            return;
        }

        System.out.println("\n[Stack Frames (Top -> Bottom)]");
        // 스택의 역순으로 출력 (top부터)
        List<Frame> frameList = new ArrayList<>(frames);
        Collections.reverse(frameList);

        for (int i = 0; i < frameList.size(); i++) {
            System.out.printf("\n#%d: ", i);
            frameList.get(i).printFrame();
        }
    }

    public boolean isEmpty() {
        return frames.isEmpty();
    }

    public int size() {
        return frames.size();
    }

}
