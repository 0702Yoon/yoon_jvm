## 📌 YoonJVM 클래스 개요

YoonJVM은 전체 시뮬레이터의 오케스트레이터로, 모든 주요 컴포넌트를 통합하고 JVM 기능을 제공합니다.

이 페이지에선 YoonJVM의 기능을 설명하도록 하겠습니다.

**보유 컴포넌트**

```java
private final ClassLoader classLoader;         // 클래스 로딩 및 검증
private final RunTimeMemory runTimeMemory;     // 메모리 영역 관리
private final GarbageCollector gc;             // 가비지 컬렉션
private int objectId;                          // 객체 ID 카운터

```

---

## 기능

### 객체 생성 (Object Creation)

**기능. 클래스를 통한 객체 생성**

`createObject(JVMClass type)` → `JVMObject`

**목적**: 기본 객체 생성 (참조 없음)

**동작**:

1. 객체 ID 증가 (`objectId++`)
2. JVMClass를 JVMObject로 변환
3. Eden에 할당
4. Eden 사용률 체크 → 70% 초과 시 자동 Minor GC

**실패 조건**:

- 자동 GC 실행 후에도 공간 부족

**기능: 존재하는 객체를 참조하는 객체를 생성**

`createObjectWithReference(JVMClass type, String refNames)` → `JVMObject`

**목적**: 참조를 가진 객체 생성

**동작**:

1. 기본 객체 생성 (위와 동일)
2. `refNames`에 지정된 객체들에 대한 참조 추가
3. Old→Young 참조 시 ReferenceTracker 자동 업데이트

**refNames 형식**:

- 쉼표(`,`)로 구분된 변수명 문자열
- 공백 자동 제거 (`strip()`)
- 존재하지 않는 변수명은 무시 (경고 출력)

---

### 메서드

**기능: 새 메서드 호출 시뮬레이션**

`invokeMethod(String methodName, int maxLocals)` → `Frame`

**동작:**

1. Frame 생성 (`new Frame(methodName, maxLocals)`)
2. JVMStack에 푸시
3. 현재 활성 프레임으로 설정

**사용 예시:**

```java
Frame mainFrame = jvm.invokeMethod("main", 30);
// → Frame[method=main, locals=0/30]

Frame methodA = jvm.invokeMethod("methodA", 10);
// → Stack: [methodA, main]

```

**maxLocals**: 이 Frame이 보유할 수 있는 최대 지역 변수 개수

**기능: 현재 프레임을 스택에서 제거**

`returnFromMethod()` → `Frame`

**동작:**

1. JVMStack에서 현재 Frame pop
2. 이전 Frame이 활성 프레임으로 전환

**사용 예시:**

```java
Frame returned = jvm.returnFromMethod();
// → Frame[method=methodA] (제거됨)
// → 현재 활성 Frame: main

```

**실패 조건:**

- 스택이 비어있을 때 → `null` 반환

---

### 지역 변수 관리

**기능: 현재 Frame의 지역 변수에 객체 저장 (GC Root 등록)**

`storeLocal(String name, JVMObject obj)` → `void`

**동작:**

1. 현재 활성 Frame 가져오기
2. Frame의 localVariables Map에 저장
3. **GC Root로 자동 등록됨**

**사용 예시:**

```java
JVMObject obj1 = jvm.createObject(sumClass);
jvm.storeLocal("obj1", obj1);
// → Frame의 localVariables: {"obj1" → Object[id=1]}
// → obj1은 이제 GC Root

```

**중요:**

- Frame에 저장하지 않으면 GC Root가 아니므로 Minor GC 시 회수됨!

**기능: 현재 Frame의 지역 변수에서 객체 가져오기**

`loadLocal(String varName)` → `JVMObject`

**사용 예시:**

```java
JVMObject obj = jvm.loadLocal("obj1");
// → Object[id=1] 반환

```

**반환값:**

- 변수가 존재하면 해당 JVMObject
- 존재하지 않으면 `null`

**기능: 지역 변수를 제거하여 객체를 GC 대상으로 전환**

`removeObjectReference(String name)` → `void`

**동작:**

1. 현재 Frame의 localVariables에서 제거
2. **더 이상 GC Root가 아님**
3. 다음 Minor GC 시 회수 대상

**사용 예시:**

```java
jvm.storeLocal("obj1", obj1);  // GC Root
jvm.removeObjectReference("obj1");  // GC Root 해제
jvm.runMinorGC();  // → obj1 회수됨

```

---

### 가비지 컬렉션 (Garbage Collection)

**기능: Minor GC 실행 (Mark-and-Copy)**

`runMinorGC()` → `void`

**동작:**

1. GC Root 수집 (Stack + Card Table)
2. Mark Phase (재귀적 마킹)
3. Copy Phase:
    - age < 3 → Survivor로 복사
    - age >= 3 → Old로 승격
4. Eden 정리

**자동 트리거:** Eden 사용률 70% 초과 시 minor GC가 실행된다.

**기능: Major GC 실행 (Mark-and-Compact)**

`runMajorGC()` → `void`

**동작:**

1. 전체 힙 Mark Phase
2. Old Generation Compact
3. Minor GC 실행

**자동 트리거:** Old 사용률 80% 초과 시 Major GC가 실행된다.

**기능: Full GC (Young + Old 전체 힙 GC) 실행**

`runFullGC()` → `void`

**동작:**

1. Major GC 실행
2. Minor GC 실행

---

### 사용자 정의 클래스 등록

**기능: 새로운 클래스를 ClassLoader에 등록**

`registerClass(TempJVMClass tempClass)` → `void`

**사용 예시:**

```java
TempJVMClass userClass = new TempJVMClass("Person", JAVA_17, fields);
jvm.registerClass(userClass);
// → ClassLoader에 등록됨

```

---

메서드 분류

### 객체 생명주기

```
생성: createObject(), createObjectWithReference()
저장: storeLocal()
조회: loadLocal()
제거: removeObjectReference()
회수: runMinorGC(), runMajorGC(), runFullGC()

```

### 메서드 호출 스택

```
호출: invokeMethod()
리턴: returnFromMethod()
현재: loadLocal(), storeLocal()

```

### 상태 관리

```
메모리: printMemoryStatus()
GC: printGCStatistics()
전체: printFullStatus()
객체: printCurrentObjects()

```

### 클래스 관리

```
조회: getClassByName()
등록: registerClass()

```

---

### 주의사항!

1. Frame 없이 storeLocal 호출
2. GC Root 등록 누락
3. 자동 GC 트리거
