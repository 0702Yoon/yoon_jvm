### 왜 필요한가?

**문제 상황**:

```
구세대 영역에 있는 객체만이 신세대 영역에 있는 객체를 참조하는 경우
```

Minor GC는 Young Generation만 정리합니다. 하지만 Old Generation의 객체가 Young의 객체를 참조한다면?

→ 실제로 사용되는 객체이지만, GC 처리가 되버린다.!

**해결책**:

- Minor GC할 때 구세대 영역도 탐색한다. (X) → 구세대 영역은 넓고, 이런 경우는 자주 존재하지 않는 다.
- Remembered Set으로 Old → Young 참조를 추적
    - Minor GC할 때 Remembered만 확인하면 되니까 효율적으로 확인할 수 있다.

### ReferenceTracker 구조

```java
public final class ReferenceTracker {
    private static YoungGeneration youngGen;
    private static OldGeneration oldGen;
    private static final Set<JVMObject> rememberedSet = new HashSet<>();

    // Write Barrier: 참조 추가 시 호출
    public static void onReferenceAdded(JVMObject from, JVMObject to) {
        if (oldGen.contains(from) && youngGen.contains(to)) {
            rememberedSet.add(from);  // Old 객체를 기록
        }
    }

    // GC 시 사용
    public static Set<JVMObject> getRememberedSetSnapshot() {
        Set<JVMObject> snapshot = new HashSet<>();
        for (JVMObject obj : rememberedSet) {
            if (oldGen.contains(obj)) {  // 여전히 Old에 있는지 확인
                snapshot.add(obj);
            }
        }
        return snapshot;
    }

    // Major GC 후 정리
    public static void pruneStaleEntries() {
        rememberedSet.removeIf(obj -> !oldGen.contains(obj));
    }
}

```

### Write Barrier 시뮬레이션

실제 JVM은 객체에 참조를 할당할 때 자동으로 Write Barrier를 실행합니다. YoonJVM에서는 `JVMObject.addReference()`에서 명시적으로 호출합니다.

```java
// JVMObject.java
public void addReference(JVMObject obj) {
    if (obj != null && !references.contains(obj)) {
        references.add(obj);
        ReferenceTracker.onReferenceAdded(this, obj);  // Write Barrier!
    }
}

```

```java
// ReferenceTracker.java
public static void onReferenceAdded(JVMObject from, JVMObject to) {
    if (youngGen == null || oldGen == null) {
        return;
    }
    if (oldGen.contains(from) && youngGen.contains(to)) {
        rememberedSet.add(from);
    }
    // 자신은 구세대에 있고 참조하는 객체가 신세대일 경우
}
```

### 동작 예시

```java
// Old의 객체 A가 Young의 객체 B를 참조
JVMObject oldObj = ...; // Old Generation에 위치
JVMObject youngObj = ...; // Young Generation에 위치

oldObj.addReference(youngObj);
// → ReferenceTracker.onReferenceAdded(oldObj, youngObj) 호출
// → rememberedSet.add(oldObj) 실행

```

**Minor GC 시**:

```java
// GC Root 외에 Remembered Set의 Old 객체도 스캔
for (JVMObject oldObj : ReferenceTracker.getRememberedSetSnapshot()) {
    for (JVMObject ref : oldObj.getReferences()) {
        if (youngGen.contains(ref)) {
            markReachable(ref, liveObjects);  // Young 객체를 살림
        }
    }
}

```
