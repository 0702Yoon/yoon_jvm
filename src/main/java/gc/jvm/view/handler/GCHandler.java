package gc.jvm.view.handler;

import gc.jvm.YoonJVM;

/**
 * GC 관련 작업을 처리하는 핸들러
 */
public class GCHandler {
    private final YoonJVM jvm;

    public GCHandler(YoonJVM jvm) {
        this.jvm = jvm;
    }

    /**
     * Minor GC 실행 (Young Generation)
     */
    public void runMinorGC() {
        jvm.runMinorGC();
    }

    /**
     * Major GC 실행 (Old Generation)
     */
    public void runMajorGC() {
        jvm.runMajorGC();
    }

    /**
     * Full GC 실행 (Young + Old 전체)
     */
    public void runFullGC() {
        jvm.runFullGC();
    }
}
