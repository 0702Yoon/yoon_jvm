package gc.jvm.classs.type;

/**
 * 상수풀 엔트리의 타입을 나타내는 태그 JVM 스펙에 정의된 상수풀 엔트리 타입들과 각 타입의 구조 정보
 */
public enum ConstantPoolTag {
    // tag, description, structure (각 필드명과 타입)
    CONSTANT_Utf8(1, "UTF-8 인코딩된 문자열",
        "tag:u1, length:u2, bytes:u1[length]"),

    CONSTANT_Integer(3, "정수 리터럴",
        "tag:u1, bytes:u4"),

    CONSTANT_Float(4, "부동소수점 리터럴",
        "tag:u1, bytes:u4"),

    CONSTANT_Long(5, "long 리터럴",
        "tag:u1, high_bytes:u4, low_bytes:u4"),

    CONSTANT_Double(6, "double 리터럴",
        "tag:u1, high_bytes:u4, low_bytes:u4"),

    CONSTANT_Class(7, "클래스 또는 인터페이스의 심벌 참조",
        "tag:u1, name_index:u2"),

    CONSTANT_String(8, "String 리터럴",
        "tag:u1, string_index:u2"),

    CONSTANT_Fieldref(9, "필드의 심벌 참조",
        "tag:u1, class_index:u2, name_and_type_index:u2"),

    CONSTANT_Methodref(10, "메서드의 심벌 참조",
        "tag:u1, class_index:u2, name_and_type_index:u2"),

    CONSTANT_InterfaceMethodref(11, "인터페이스 메서드의 심벌 참조",
        "tag:u1, class_index:u2, name_and_type_index:u2"),

    CONSTANT_NameAndType(12, "필드 또는 메서드의 이름과 서술자",
        "tag:u1, name_index:u2, descriptor_index:u2"),

    CONSTANT_MethodHandle(15, "메서드 핸들",
        "tag:u1, reference_kind:u1, reference_index:u2"),

    CONSTANT_MethodType(16, "메서드 타입",
        "tag:u1, descriptor_index:u2"),

    CONSTANT_Dynamic(17, "동적으로 계산되는 상수",
        "tag:u1, bootstrap_method_attr_index:u2, name_and_type_index:u2"),

    CONSTANT_InvokeDynamic(18, "동적으로 계산되는 호출 사이트",
        "tag:u1, bootstrap_method_attr_index:u2, name_and_type_index:u2"),

    CONSTANT_Module(19, "모듈",
        "tag:u1, name_index:u2"),

    CONSTANT_Package(20, "패키지",
        "tag:u1, name_index:u2");

    private final int tag;
    private final String description;
    private final String structure;

    ConstantPoolTag(int tag, String description, String structure) {
        this.tag = tag;
        this.description = description;
        this.structure = structure;
    }
}
