package gc.jvm.classs.type;

/**
 * 클래스(또는 인터페이스)의 접근 정보를 식별하는 접근 플래그 2바이트로 표현되며, 최대 16개의 플래그를 사용할 수 있음
 */
public enum AccessFlags {
    ACC_PUBLIC(0x0001, "public 타입"),
    ACC_FINAL(0x0010, "final로 선언됨"),
    ACC_SUPER(0x0020, "JDK 1.0.2 이후 컴파일된 클래스"),
    ACC_INTERFACE(0x0200, "인터페이스"),
    ACC_ABSTRACT(0x0400, "추상 클래스"),
    ACC_SYNTHETIC(0x1000, "컴파일러가 생성한 클래스"),
    ACC_ANNOTATION(0x2000, "어노테이션 타입"),
    ACC_ENUM(0x4000, "열거형 타입"),
    ACC_MODULE(0x8000, "모듈");

    private final int value;
    private final String description;

    AccessFlags(int value, String description) {
        this.value = value;
        this.description = description;
    }
}
