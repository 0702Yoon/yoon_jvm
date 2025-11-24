package gc.jvm.memory;

import gc.jvm.classs.JVMObject;
import java.util.ArrayList;
import java.util.List;

/**
 * Old Generation 메모리 영역
 * Young Generation에서 승격(Promotion)된 오래된 객체 저장
 * Mark-and-Compact 또는 Mark-and-Copy 방식 사용
 */
public class OldGeneration {
    private final int maxSize;               // 최대 크기 (바이트)
    private int currentSize;                 // 현재 사용 중인 크기
    private final List<JVMObject> objects;         // 저장된 객체들

    public OldGeneration(int maxSize) {
        this.maxSize = maxSize;
        this.currentSize = 0;
        this.objects = new ArrayList<>();
    }

    /**
     * Old Generation에 객체 추가 (Young에서 승격)
     */
    public boolean allocate(JVMObject obj) {
        if (currentSize + obj.getSize() > maxSize) {
            return false;  // 공간 부족
        }

        objects.add(obj);
        currentSize += obj.getSize();
        return true;
    }



    public boolean isFull() {
        return (double) currentSize / maxSize > 0.8;  // 80% 이상 사용 시
    }

    /**
     * 메모리 상태 출력
     */
    public void printStatus() {
        System.out.println("\n=== 구세대(Old Generation) ===");
        System.out.printf("총 크기: %d / %d 바이트 (%.1f%%)\n",
            currentSize, maxSize, (currentSize * 100.0 / maxSize));
        System.out.printf("객체 수: %d개\n", objects.size());

        if (!objects.isEmpty()) {
            System.out.println("\n[구세대 객체들]");
            for (JVMObject obj : objects) {
                System.out.println("  " + obj);
            }
        }
    }
    /**
     * 살아있는 객체를 받아서 기존 객체 제거 후 재 할당.
     */
    public void compact(List<JVMObject> liveObjects) {
        objects.clear();
        currentSize = 0;

        for (JVMObject obj : liveObjects) {
            objects.add(obj);
            currentSize += obj.getSize();
        }
        ReferenceTracker.pruneStaleEntries();
    }

    public boolean contains(JVMObject obj) {
        return objects.contains(obj);
    }

    public List<JVMObject> getObjects() {
        return objects;
    }

}
