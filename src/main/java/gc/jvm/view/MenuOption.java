package gc.jvm.view;

import gc.jvm.view.handler.HandlerContext;
import java.util.Arrays;
import java.util.function.Consumer;

public enum MenuOption {
    OBJECT_CREATE_YOUNG(1, "객체 생성 (Young Generation에 할당)", "[객체 관리]",
        ctx -> ctx.getObjectHandler().createObject()),

    CLASS_CREATE_NEW(2, "새로운 클래스 생성", "[클래스 관리]",
        ctx -> ctx.getClassHandler().createClass()),

    OBJECT_CREATE_WITH_REF(3, "참조를 가진 객체 생성", "[객체 관리]",
        ctx -> ctx.getObjectHandler().createObjectWithReference()),
    OBJECT_REMOVE_REF(4, "객체 참조 제거 (Unreachable 상태로 만듦)", "[객체 관리]",
        ctx -> ctx.getObjectHandler().removeObjectReference()),
    LIST_ALL_OBJECTS(5, "이름 지정된 모든 객체 목록 보기", "[객체 관리]",
        ctx -> ctx.getObjectHandler().listNamedObjects()),

    MINOR_GC(6, "Minor GC 실행 (Young Generation)", "[가비지 컬렉션]",
        ctx -> ctx.getGcHandler().runMinorGC()),
    MAJOR_GC(7, "Major GC 실행 (Old Generation)", "[가비지 컬렉션]",
        ctx -> ctx.getGcHandler().runMajorGC()),
    FULL_GC(14, "Full GC 실행 (Young + Old 전체)", "[가비지 컬렉션]",
        ctx -> ctx.getGcHandler().runFullGC()),

    VIEW_MEMORY_STATUS(8, "메모리 상태 보기", "[메모리 및 통계]",
        ctx -> ctx.getMemoryHandler().printMemoryStatus()),
    VIEW_GC_STATS(9, "GC 통계 보기", "[메모리 및 통계]",
        ctx -> ctx.getMemoryHandler().printGCStatistics()),
    VIEW_ALL_STATUS(10, "전체 상태 보기 (메모리 + GC 통계)", "[메모리 및 통계]",
        ctx -> ctx.getMemoryHandler().printFullStatus()),

    STRESS_TEST(11, "여러 객체 생성 (스트레스 테스트)", "[고급 기능]",
        ctx -> ctx.getAdvancedHandler().createMultipleObjects()),
    PUSH_FRAME(12, "메서드 호출 (새 프레임 푸시)", "[고급 기능]",
        ctx -> ctx.getAdvancedHandler().invokeMethod()),
    POP_FRAME(13, "메서드 복귀 (프레임 팝)", "[고급 기능]",
        ctx -> ctx.getAdvancedHandler().returnFromMethod()),

    EXIT(0, "종료", null,
        ctx -> ctx.getSystemHandler().exit());

    private final int optionNumber;
    private final String description;
    private final String category;
    private final Consumer<HandlerContext> action;

    MenuOption(int optionNumber, String description, String category,
               Consumer<HandlerContext> action) {
        this.optionNumber = optionNumber;
        this.description = description;
        this.category = category;
        this.action = action;
    }

    public static MenuOption fromOptionNumber(int choice) {
        return Arrays.stream(MenuOption.values())
            .filter(e -> e.optionNumber == choice)
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("없는 메뉴입니다."));
    }

    public int getOptionNumber() {
        return optionNumber;
    }

    public String getDescription() {
        return description;
    }

    public String getCategory() {
        return category;
    }


    public void execute(HandlerContext context) {
        action.accept(context);
    }
}
