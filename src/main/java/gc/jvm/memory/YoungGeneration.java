package gc.jvm.memory;

import gc.jvm.classs.JVMObject;
import java.util.ArrayList;
import java.util.List;

/**
 * Young Generation 메모리 영역
 * Eden + Survivor0 + Survivor1로 구성
 *  Mark-and-Copy 방식의 GC 사용
 */
public class YoungGeneration {
    private final int maxSize;               // 최대 크기 (바이트)
    private int currentSize;                 // 현재 사용 중인 크기

    private final List<JVMObject> eden;
    private final List<JVMObject> survivor0;
    private final List <JVMObject> survivor1;

    private boolean survivor0Active;  // 현재 활성화된 Survivor (true: S0, false: S1)

    private int edenSize;
    private int survivor0Size;
    private int survivor1Size;

    public YoungGeneration(int maxSize) {
        this.maxSize = maxSize;
        this.currentSize = 0;
        this.eden = new ArrayList<>();
        this.survivor0 = new ArrayList<>();
        this.survivor1 = new ArrayList<>();
        this.survivor0Active = true;
        this.edenSize = 0;
        this.survivor0Size = 0;
        this.survivor1Size = 0;
    }

    /**
     * Eden 영역에 객체 할당
     */
    public boolean allocate(JVMObject obj) {
        if (currentSize + obj.getSize() > maxSize || isEdenFull()) {
            return false;  // 공간 부족
        }

        eden.add(obj);
        edenSize += obj.getSize();
        currentSize += obj.getSize();
        return true;
    }

    public boolean isEdenFull() {
        return (double) edenSize / maxSize > 0.7;  // 70% 이상 사용 시
    }

    public List<JVMObject> getActiveSurvivor() {
        if (survivor0Active) {
            return survivor0;
        }
        return survivor1;
    }

    /**
     * 현재 비활성화된 Survivor 영역 반환 (복사 대상)
     */
    public List<JVMObject> getInactiveSurvivor() {
        if(survivor0Active){
            return survivor1;
        }
        return survivor0;
    }

    /**
     * Survivor 영역 교체 (From-Space <-> To-Space)
     */
    public void swapSurvivors() {
        survivor0Active = !survivor0Active;
    }

    /**
     * Eden 영역 비우기
     */
    public void clearEden() {
        for (JVMObject obj : eden) {
            currentSize -= obj.getSize();
        }
        eden.clear();
        edenSize = 0;
    }

    public int getAvailableSpace() {
        return Math.max(0, maxSize - currentSize);
    }

    /**
     * Eden 영역에서 특정 객체 제거 (Old로 승격 시 사용)
     */
    public boolean removeFromEden(JVMObject obj) {
        if (eden.remove(obj)) {
            edenSize -= obj.getSize();
            currentSize -= obj.getSize();
            return true;
        }
        return false;
    }

    /**
     * 비활성 Survivor 영역 비우기
     */
    public void clearInactiveSurvivor() {
        List<JVMObject> inactive = getInactiveSurvivor();

        // currentSize에서 객체 크기만큼 빼기
        for (JVMObject obj : inactive) {
            currentSize -= obj.getSize();
        }

        // 비활성 Survivor 크기를 0으로 초기화
        if (survivor0Active) {
            survivor1Size = 0;  // S1이 비활성
        } else {
            survivor0Size = 0;  // S0이 비활성
        }

        inactive.clear();
        swapSurvivors();  // Survivor 교체
    }

    /**
     * 활성 Survivor에 객체 추가 (GC 중 복사용)
     */
    public void addToActiveSurvivor(JVMObject obj) {
        List<JVMObject> active = getActiveSurvivor();
        active.add(obj);
        currentSize += obj.getSize();

        if (survivor0Active) {
            survivor0Size += obj.getSize();
        } else {
            survivor1Size += obj.getSize();
        }
    }

    /**
     * 메모리 상태 출력
     */
    public void printStatus() {
        System.out.println("=== 신세대(Young Generation) ===");
        System.out.printf("총 크기: %d / %d 바이트 (%.1f%%)\n",
            currentSize, maxSize, (currentSize * 100.0 / maxSize));
        System.out.printf("Eden: %d개 객체, %d 바이트\n", eden.size(), edenSize);
        System.out.printf("Survivor0 (S0): %d개 객체, %d 바이트 %s\n",
            survivor0.size(), survivor0Size, survivor0Active ? "[활성]" : "");
        System.out.printf("Survivor1 (S1): %d개 객체, %d 바이트 %s\n",
            survivor1.size(), survivor1Size, !survivor0Active ? "[활성]" : "");

        if (!eden.isEmpty()) {
            System.out.println("\n[Eden 영역 객체들]");
            for (JVMObject obj : eden) {
                System.out.println("  " + obj);
            }
        }

        List<JVMObject> active = getActiveSurvivor();
        if (!active.isEmpty()) {
            System.out.println("\n[활성 Survivor 객체들]");
            for (JVMObject obj : active) {
                System.out.println("  " + obj);
            }
        }
    }

    /**
     * 객체 제거 (Old로 승격 시)
     */
    public boolean removeFromSurvivor(JVMObject obj) {
        List<JVMObject> active = getActiveSurvivor();
        if (active.remove(obj)) {
            currentSize -= obj.getSize();
            if (survivor0Active) {
                survivor0Size -= obj.getSize();
            } else {
                survivor1Size -= obj.getSize();
            }
            return true;
        }
        return false;
    }

    public boolean contains(JVMObject obj) {
        return eden.contains(obj) || survivor0.contains(obj) || survivor1.contains(obj);
    }

    public List<JVMObject> getEden() {
        return eden;
    }

    public int getEdenSize() {
        return edenSize;
    }

    public int getMaxSize() {
        return maxSize;
    }

}
