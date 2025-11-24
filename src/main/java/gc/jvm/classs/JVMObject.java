package gc.jvm.classs;

import gc.jvm.memory.ReferenceTracker;
import java.util.ArrayList;
import java.util.List;

/**
 * JVM에서 관리되는 객체를 표현 Mark-and-Copy GC를 위한 필드들 포함
 */
public class JVMObject {
    public static final int OBJECT_HEADER_SIZE = 16;


    private final int id;                    // 객체 고유 ID
    private final JVMClass type;               // 객체 타입 (클래스명)
    private int size;                        // 객체 크기 (바이트)
    private boolean marked;                  // GC 마킹 플래그
    private int age;                         // 객체의 나이 (GC 생존 횟수)
    private List<JVMObject> references;      // 참조하는 다른 객체들
    private List<Field> fields;                     // 실제 데이터 (간단 표현용)

    public JVMObject(int objectId,JVMClass type, int size) {
        this.id = objectId;
        this.type = type;
        this.size = size;
        this.marked = false;
        this.age = 0;
        this.references = new ArrayList<>();
        this.fields = null;
    }

    public JVMObject(int objectId,JVMClass type, int size, List<Field> fields) {
        this(objectId,type, size);
        this.fields = fields;
    }

    /**
     * 다른 객체에 대한 참조 추가
     */
    public void addReference(JVMObject obj) {
        if (obj != null && !references.contains(obj)) {
            references.add(obj);
            ReferenceTracker.onReferenceAdded(this, obj);
        }
    }

    /**
     * 참조 제거
     */
    public void removeReference(JVMObject obj) {
        references.remove(obj);
    }

    public void mark() {
        this.marked = true;
    }

    public void unmark() {
        this.marked = false;
    }

    /**
     * 나이 증가 (GC 생존 시)
     */
    public void incrementAge() {
        this.age++;
    }

    /**
     * Old Generation으로 승격될 조건 확인
     */
    public boolean shouldPromoteToOld(int threshold) {
        return this.age >= threshold;
    }

    /**
     * 복사본 생성 (Mark-and-Copy용)
     */
    public JVMObject copy() {
        JVMObject copied = new JVMObject(this.id, this.type, this.size, this.fields);
        copied.age = this.age;
        copied.marked = false;
        copied.references = new ArrayList<>(this.references);
        return copied;
    }

    /**
     * 객체 정보 출력
     */
    @Override
    public String toString() {
        return String.format("Object[id=%d, type=%s, size=%dB, age=%d, refs=%d, marked=%b]",
            id, type, size, age, references.size(), marked);
    }

    public String getClassName() {
        return type.getName();
    }

    public String toDetailString() {
        StringBuilder sb = new StringBuilder();
        sb.append(this);
        if (fields != null) {
            sb.append(" data=").append(fields);
        }
        if (!references.isEmpty()) {
            sb.append(" -> [");
            for (int i = 0; i < references.size(); i++) {
                if (i > 0) {
                    sb.append(", ");
                }
                sb.append(references.get(i).getId());
            }
            sb.append("]");
        }
        return sb.toString();
    }

    public int getId() {
        return id;
    }

    public int getSize() {
        return size;
    }

    public List<JVMObject> getReferences() {
        return references;
    }

    public int getAge() {
        return age;
    }
}
