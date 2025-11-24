### 왜 Mark-and-Compact인가?

Old Generation의 특징:

- 객체 대부분이 **장수**: 많은 객체가 살아남음
- Copying 방식 사용 시 문제: 복사할 객체가 너무 많아 비효율적

**Mark-and-Compact의 장점**:

- 살아있는 객체를 이동(compact)하여 단편화 방지
- 복사본을 만들지 않으므로 메모리 효율적

### Mark-and-Compact 알고리즘

```
Before Compact:
┌─────────────────────────────────────┐
│ [Live A] [Dead] [Live B] [Dead] [Live C] │
└─────────────────────────────────────┘

After Compact:
┌─────────────────────────────────────┐
│ [Live A][Live B][Live C] (여유 공간) │
└─────────────────────────────────────┘

```

### 코드 구현

```java
public void majorGC() {
    System.out.println("╔════════════════════════════════════════╗");
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
            liveOldObjects.add(obj);  // 살아있는 객체만 수집
        } else {
            oldCollected++;  // 죽은 객체 카운트
        }
    }

    oldGen.compact(liveOldObjects);  // 살아있는 객체만 남기고 압축

    System.out.println("\\n[3단계] 압축 완료:");
    System.out.println("  - 구세대에서 수집됨: " + oldCollected + "개 객체");
    System.out.println("  - 구세대에 남음: " + liveOldObjects.size() + "개 객체");

    resetMarks(liveObjects);
    System.out.println("\\n[Success] Major GC 완료\\n");
}

```

**OldGeneration.compact()** 구현:

```java
public void compact(List<JVMObject> liveObjects) {
    objects.clear();
    currentSize = 0;

    for (JVMObject obj : liveObjects) {
        objects.add(obj);
        currentSize += obj.getSize();
    }
}

```
