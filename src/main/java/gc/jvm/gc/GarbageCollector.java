package gc.jvm.gc;

import gc.jvm.classs.JVMObject;
import gc.jvm.memory.OldGeneration;
import gc.jvm.memory.ReferenceTracker;
import gc.jvm.memory.RunTimeMemory;
import gc.jvm.memory.YoungGeneration;
import gc.jvm.runtime.JVMStack;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Garbage Collector
 * - Minor GC: Young Generation 정리 (Mark-and-Copy)
 * - Major GC: Old Generation 정리 (Mark-and-Compact)
 * - Full GC: Young + Old 전체 정리 (Minor GC → Major GC 순차 실행)
 */
public class GarbageCollector {
    private static final int PROMOTION_AGE_THRESHOLD = 3;  // Old로 승격 기준 나이

    private final YoungGeneration youngGen;
    private final OldGeneration oldGen;
    private final JVMStack stack;

    private int minorGCCount;
    private int majorGCCount;

    public GarbageCollector(RunTimeMemory runTimeMemory) {
        this.youngGen = runTimeMemory.getYoungGeneration();
        this.oldGen = runTimeMemory.getOldGeneration();
        this.stack = runTimeMemory.getStack();
        this.minorGCCount = 0;
        this.majorGCCount = 0;
    }

    /**
     * Minor GC 실행 (Young Generation) Mark-and-Copy 알고리즘 사용
     */
    public int minorGC() {
        return minorGCInternal(true);
    }

    private int minorGCInternal(boolean allowMajorTrigger) {
        System.out.println("\n╔════════════════════════════════════════╗");
        System.out.println("║      Minor GC 시작 (신세대)            ║");
        System.out.println("╚════════════════════════════════════════╝");

        minorGCCount++;

        // Eden 스냅샷 (정리 전 상태 저장)
        List<JVMObject> edenSnapshot = new ArrayList<>(youngGen.getEden());

        // 1-2. GC Root 수집 및 Mark Phase
        Set<JVMObject> liveObjects = collectAndMarkLiveObjects();

        // Eden에 있던 객체 중 살아남은 객체 식별
        Set<JVMObject> liveEdenObjects = new HashSet<>();
        for (JVMObject obj : edenSnapshot) {
            if (liveObjects.contains(obj)) {
                liveEdenObjects.add(obj);
            }
        }

        // 3. Copy Phase - 살아있는 객체를 Survivor 또는 Old로 복사
        List<JVMObject> toSurvivor = new ArrayList<>();
        List<JVMObject> toOld = new ArrayList<>();
        List<JVMObject> youngLiveObjects = new ArrayList<>();

        for (JVMObject obj : liveObjects) {
            if (!youngGen.contains(obj)) {
                continue;  // 구세대 객체는 Minor GC 대상에서 제외
            }

            youngLiveObjects.add(obj);
            obj.incrementAge();  // 나이 증가

            if (obj.shouldPromoteToOld(PROMOTION_AGE_THRESHOLD)) {
                toOld.add(obj);
            } else {
                toSurvivor.add(obj);
            }
        }

        System.out.println("[3단계] 복사 단계:");
        System.out.println("  - Survivor로 이동: " + toSurvivor.size() + "개 객체");
        System.out.println("  - 구세대로 승격: " + toOld.size() + "개 객체");
        if (!toSurvivor.isEmpty()) {
            System.out.println("    [복사 로그]");
            for (JVMObject obj : toSurvivor) {
                System.out.printf("    • 객체 %d 복사 → Survivor (나이=%d)%n", obj.getId(), obj.getAge());
            }
        }
        if (!toOld.isEmpty()) {
            System.out.println("    [승격 로그]");
            for (JVMObject obj : toOld) {
                System.out.printf("    • 객체 %d 복사 → Old Generation (나이=%d)%n", obj.getId(), obj.getAge());
            }
        }

        // 4. 비활성 Survivor 비우기 및 Survivor 교체
        youngGen.clearInactiveSurvivor();

        // 5. 활성 Survivor에 살아있는 객체 복사
        for (JVMObject obj : toSurvivor) {
            youngGen.addToActiveSurvivor(obj);
        }

        // 6. Old Generation으로 승격
        int promotedCount = 0;
        for (JVMObject obj : toOld) {
            if (oldGen.allocate(obj)) {
                if (!youngGen.removeFromEden(obj)) { // 객체는 둘 중(에덴, 생존자 구역)에 한 곳에서만 살아있기에 조건문 처리
                    youngGen.removeFromSurvivor(obj);
                }
                promotedCount++;
            } else {
                System.out.println("  [경고] 구세대 메모리 가득 참, 객체 " + obj.getId() + " 승격 불가");
            }
        }

        // 7. Eden 영역 비우기
        int edenCollected = edenSnapshot.size() - liveEdenObjects.size();
        youngGen.clearEden();

        System.out.println("\n[4단계] 정리 완료:");
        System.out.println("  - Eden에서 수집됨: 약 " + edenCollected + "개 객체");
        System.out.println("  - 구세대로 승격됨: " + promotedCount + "개 객체");
        System.out.println("  - 신세대에 남음: " + (youngLiveObjects.size() - promotedCount) + "개 객체");

        resetMarks(liveObjects);
        System.out.println("\n[Success] Minor GC 완료 (총 " + minorGCCount + "회 실행)\n");

        if (allowMajorTrigger && oldGen.isFull()) {
            System.out.println("[경고] 구세대 사용량이 80%를 초과하여 Major GC를 자동 실행합니다.");
            majorGC();
        }
        return edenCollected;
    }

    /**
     * Major GC 실행 (Old Generation만 정리) Mark-and-Compact 알고리즘 사용
     */
    public void majorGC() {
        System.out.println("\n╔════════════════════════════════════════╗");
        System.out.println("║    Major GC 시작 (구세대)              ║");
        System.out.println("╚════════════════════════════════════════╝");

        majorGCCount++;

        // 1-2. GC Root 수집 및 Mark Phase (전체 힙)
        Set<JVMObject> liveObjects = collectAndMarkLiveObjects();

        // 3. Compact Phase - Old Generation 정리
        List<JVMObject> liveOldObjects = new ArrayList<>();
        int oldCollected = 0;

        for (JVMObject obj : oldGen.getObjects()) {
            if (liveObjects.contains(obj)) {
                liveOldObjects.add(obj);
            } else {
                oldCollected++;
            }
        }

        oldGen.compact(liveOldObjects);

        System.out.println("\n[3단계] 압축 완료:");
        System.out.println("  - 구세대에서 수집됨: " + oldCollected + "개 객체");
        System.out.println("  - 구세대에 남음: " + liveOldObjects.size() + "개 객체");

        resetMarks(liveObjects);
        System.out.println("\n[Success] Major GC 완료 (총 " + majorGCCount + "회 실행)\n");
    }

    /**
     * Full GC 실행 (Young + Old 전체 정리)
     * Minor GC를 먼저 실행하여 Young Generation을 정리한 후,
     * Major GC를 실행하여 Old Generation을 정리합니다.
     */
    public void fullGC() {
        System.out.println("\n╔════════════════════════════════════════╗");
        System.out.println("║    Full GC 시작 (전체 힙)              ║");
        System.out.println("╚════════════════════════════════════════╝");

        // 1. Young Generation 정리
        minorGCInternal(false);  // allowMajorTrigger = false (무한 재귀 방지)

        // 2. Old Generation 정리
        majorGC();

        System.out.println("[Success] Full GC 완료 (Young + Old 모두 정리됨)\n");
    }

    /**
     * GC Root 수집
     */
    private Set<JVMObject> collectGCRoots() {
        // JVM Stack의 모든 지역 변수가 GC Root
        return stack.collectGCRoots();
    }

    /**
     * GC Root 수집 및 Mark Phase 통합 메서드 (중복 코드 제거)
     */
    private Set<JVMObject> collectAndMarkLiveObjects() {
        // 1. GC Root 수집
        Set<JVMObject> roots = collectGCRoots();
        System.out.println("\n[1단계] GC Root 수집 완료: " + roots.size() + "개 객체");

        // 2. Mark Phase - 도달 가능한 객체 마킹
        Set<JVMObject> liveObjects = new HashSet<>();
        for (JVMObject root : roots) {
            markReachable(root, liveObjects);
        }
        markRememberedYoungReferences(liveObjects);
        System.out.println("[2단계] 마킹 완료: " + liveObjects.size() + "개 살아있는 객체 발견");

        return liveObjects;
    }

    /**
     * Mark Phase - 재귀적으로 도달 가능한 객체 마킹
     */
    private void markReachable(JVMObject obj, Set<JVMObject> liveObjects) {
        if (obj == null || liveObjects.contains(obj)) {
            return;
        }

        liveObjects.add(obj);
        obj.mark();

        for (JVMObject ref : obj.getReferences()) {
            markReachable(ref, liveObjects);
        }
    }

    private void markRememberedYoungReferences(Set<JVMObject> liveObjects) {
        for (JVMObject oldObj : ReferenceTracker.getRememberedSetSnapshot()) {
            for (JVMObject ref : oldObj.getReferences()) {
                if (youngGen.contains(ref)) {
                    markReachable(ref, liveObjects);
                }
            }
        }
    }

    private void resetMarks(Set<JVMObject> liveObjects) {
        for (JVMObject obj : liveObjects) {
            obj.unmark();
        }
    }

    /**
     * GC 통계 출력
     */
    public void printStatistics() {
        System.out.println("\n=== GC 통계 ===");
        System.out.println("Minor GC 실행 횟수: " + minorGCCount + "회");
        System.out.println("Major GC 실행 횟수: " + majorGCCount + "회");
        System.out.println("총 GC 실행 횟수: " + (minorGCCount + majorGCCount) + "회");
    }
}
