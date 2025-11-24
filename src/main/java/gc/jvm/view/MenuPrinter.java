package gc.jvm.view;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MenuPrinter {
    private final StringBuilder sb;
    private static final String SEPARATOR = "-".repeat(60);

    public MenuPrinter() {
        this.sb = new StringBuilder();
    }

    public void printWelcome() {
        sb.append("Welcome to the JVM printer\n");
        sb.append("\n");
        sb.append("\n");
        sb.append(SEPARATOR);
        sb.append("\n");
        sb.append("""
            Yoon JVM 대화형 시뮬레이터에 오신 것을 환영합니다!
            - 런타임 메모리 영역 구현"
            - Mark-and-Copy 가비지 컬렉션
            """);
        sb.append(SEPARATOR);
        sb.append("\n");

        System.out.println(sb);
        reset();
    }

    public void displayMenu() {
        // 1. 헤더 생성
        sb.append(createHeader());

        // 2. 그룹별 메뉴 생성
        sb.append(createGroupedOptions());

        // 3. 종료 및 프롬프트 생성
        sb.append(createFooterAndPrompt());

        System.out.print(sb);

        reset();
    }

    /**
     * 헤더 출력 전용 메서드 분리
     */
    private String createHeader() {
        return "\n" + SEPARATOR + "\n"
            + "                          메뉴\n"
            + SEPARATOR + "\n";
    }

    private String createGroupedOptions() {
        StringBuilder groupBuilder = new StringBuilder();
        List<MenuOption> options = Arrays.asList(MenuOption.values());

        // 1. 카테고리별로 메뉴 옵션을 그룹화
        Map<String, List<MenuOption>> groupedMenu = options.stream()
            .filter(opt -> opt.getCategory() != null)
            .collect(Collectors.groupingBy(MenuOption::getCategory));

        // 2. 그룹화된 데이터를 순회하며 형식에 맞게 출력 (for-each가 스트림보다 직관적)
        for (Map.Entry<String, List<MenuOption>> entry : groupedMenu.entrySet()) {
            String category = entry.getKey();
            List<MenuOption> menuList = entry.getValue();

            groupBuilder.append("  ").append(category).append("\n"); // 카테고리 제목

            for (MenuOption option : menuList) {
                groupBuilder.append(formatOption(option)); // 메뉴 항목 형식화 메서드 호출
            }
            groupBuilder.append("\n"); // 그룹 간 여백
        }

        return groupBuilder.toString();
    }

    private String formatOption(MenuOption option) {
        return String.format("    %2d. %s\n",
            option.getOptionNumber(),
            option.getDescription());
    }

    private String createFooterAndPrompt() {
        MenuOption exitOption = MenuOption.EXIT;

        return formatOption(exitOption) // 종료 옵션도 동일한 형식으로 출력
            + SEPARATOR + "\n"
            + "옵션을 선택하세요: ";
    }

    private void reset() {
        this.sb.setLength(0);
    }
}
