package gc.jvm.view.handler;

import gc.jvm.YoonJVM;

/**
 * 메모리 및 통계 관련 작업을 처리하는 핸들러
 */
public class MemoryHandler {
    private final YoonJVM jvm;

    public MemoryHandler(YoonJVM jvm) {
        this.jvm = jvm;
    }

    /**
     * 메모리 상태 출력
     */
    public void printMemoryStatus() {
        jvm.printMemoryStatus();
    }

    /**
     * GC 통계 출력
     */
    public void printGCStatistics() {
        jvm.printGCStatistics();
    }

    /**
     * 전체 상태 출력 (메모리 + GC 통계)
     */
    public void printFullStatus() {
        jvm.printFullStatus();
    }
}
