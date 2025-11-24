제가 생각한 JVM의 Minor GC는 Mark-and-Copy 알고리즘을 사용합니다.

### 왜 Mark-and-Copy인가?

**Generational Hypothesis** (세대별 가설):

- 대부분의 객체는 **단명(short-lived)**: 생성 직후 곧 가비지가 됨
- 소수의 객체만 **장수(long-lived)**: 오래 살아남음

Young Generation은 대부분의 객체가 죽으므로:

- 살아남은 객체(소수)만 복사 → 비용 절감
- 복사 과정에서 메모리 압축 효과 (단편화 방지)

<aside>

단편화란?

단편화는 **하나의 큰 덩어리(자료나 공간)가 여러 작은 조각으로 나뉘는 현상**을 뜻합니다.

할당되는 객체마다 크기가 다르다. 그리고 언제 해제될 지도 모른다.

실제 메모리 크기가 100byte이고 현재 사용된 메모리의 크기가 50btye라고 가정한다.

만약 한쪽으로 50btye가 사용된 상황이 아니라면, 50btye 크기의 객체는 할당될 수 없다.

</aside>

## 전체 흐름 요약

```
GC Root 수집
  ↓
재귀적 마킹 (Remembered Set 포함)
  ↓
나이 증가 및 복사 (Survivor/Old 분류)
  ↓
메모리 영역 정리 (Survivor 교체, Eden 비우기)
  ↓
마킹 플래그 초기화

```

**Minor GC는 Young Generation(Eden + Survivor)을 정리하는 과정으로, 총 5단계로 구성됩니다.**

### 1단계: GC Root 수집

JVM Stack의 모든 Frame에 있는 지역 변수를 GC Root로 수집합니다.

이 지역 변수들이 참조하는 객체들은 도달 가능한 객체로 간주됩니다.

```java
public Set<JVMObject> collectGCRoots() {
    Set<JVMObject> roots = new HashSet<>();
    for (Frame frame : frames) {
        for (JVMObject obj : frame.getAllLocalVariables().values()) {
            if (obj != null) {
                roots.add(obj);
            }
        }
    }
    return roots;
}

```

### 2단계: Mark Phase (재귀적 마킹)

GC Root부터 시작하여 도달 가능한 모든 객체를 재귀적으로 탐색하고 마킹합니다.

- GC Root가 참조하는 객체부터 시작
    - 각 객체가 참조하는 다른 객체들도 재귀적으로 탐색
- Remembered에 등록된 Old Generation 객체가 참조하는 Young Generation 객체도 함께 마킹

```java
// GarbageCollector.java
for (JVMObject root : roots) {
    markReachable(root, liveObjects);  // 재귀적 마킹
}
markRememberedYoungReferences(liveObjects);  // Old→Young 참조도 마킹
```

```java
// GarbageCollector.java
private void markReachable(JVMObject obj, Set<JVMObject> liveObjects) {
    if (obj == null || liveObjects.contains(obj)) {
        return;  // 이미 방문했거나 null이면 중단
    }

    liveObjects.add(obj);  // 살아있는 객체로 표시
    obj.mark();            // 마킹 플래그 설정

    // 참조하는 모든 객체에 대해 재귀 호출
    for (JVMObject ref : obj.getReferences()) {
        markReachable(ref, liveObjects);
    }
}

```

## 3단계: Copy Phase (복사)

살아있는 객체를 식별하고, 나이에 따라 Survivor 영역 또는 Old Generation으로 복사합니다.

- **나이 증가**: 모든 살아있는 객체의 `age`를 1 증가시킵니다
- **나이 < 3**: Survivor 영역으로 복사
- **나이 >= 3**: Old Generation으로 승격 (Promotion)

```java
obj.incrementAge();  // 나이 증가
if (obj.shouldPromoteToOld(PROMOTION_AGE_THRESHOLD)) {
    toOld.add(obj);  // Old로 승격
} else {
    toSurvivor.add(obj);  // Survivor로 복사
}

```

## 4단계: Cleanup Phase (정리)

```java
// 비활성 Survivor 비우기 및 교체
youngGen.clearInactiveSurvivor();

// 활성 Survivor에 살아있는 객체 추가
for (JVMObject obj : toSurvivor) {
    youngGen.addToActiveSurvivor(obj);
}

// Old Generation으로 승격
int promotedCount = 0;
for (JVMObject obj : toOld) {
    if (oldGen.allocate(obj)) {
        // Eden 또는 Survivor에서 제거
        if (!youngGen.removeFromEden(obj)) {
            youngGen.removeFromSurvivor(obj);
        }
        promotedCount++;
    } else {
        System.out.println("[경고] 구세대 메모리 가득 참, 승격 불가");
    }
}

// Eden 영역 비우기
int edenCollected = edenSnapshot.size() - liveEdenObjects.size();
youngGen.clearEden();

```

**왜 `removeFromEden()` 실패 시 `removeFromSurvivor()`를 호출하나?**

객체는 Eden **또는** Survivor 중 한 곳에만 존재합니다 (상호 배타적).

Eden에서 제거 실패 → 객체는 Survivor에 있다는 뜻!

## 5단계: Mark 초기화

GC가 완료된 후 모든 살아있는 객체의 `marked` 플래그를 초기화합니다. 다음 GC를 위해 마킹 상태를 리셋하는 과정입니다.

