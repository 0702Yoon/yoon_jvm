package gc.jvm.view;

import gc.jvm.classs.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

public class View {
    private final MenuPrinter menuPrinter;
    private final Scanner sc;

    public View() {
        this.menuPrinter = new MenuPrinter();
        this.sc = new Scanner(System.in);
    }

    public void welcome() {
        menuPrinter.printWelcome();
    }

    public int selecetMenu() {
        menuPrinter.displayMenu();
        String input = sc.nextLine().trim();
        if (input.isEmpty()) {
            return -1;  // 빈 입력은 무효 처리 (0은 EXIT)
        }
        return Integer.parseInt(input);
    }

    public String readLine() {
        return sc.nextLine().strip();
    }

    public String readLine(String prompt) {
        System.out.print(prompt);
        System.out.flush();
        return sc.nextLine().trim();
    }

    public int readInt(String prompt) {
        System.out.print(prompt);
        System.out.flush();
        return Integer.parseInt(sc.nextLine().trim());
    }

    public void println(String message) {
        System.out.println(message);
    }

    public void close() {
        System.out.println("\n[Success] Yoon JVM 시뮬레이터를 종료합니다. 안녕히 가세요!");
        sc.close();
    }

    public String selectClass(List<String> loadClasses) {
        printCurrentClass(loadClasses);
        return sc.nextLine().strip();
    }

    public void printCurrentClass(List<String> loadClasses) {
        System.out.println("현재 존재하는 클래스 목록입니다.");
        System.out.println(String.join(", ", loadClasses));
        System.out.println();
        System.out.print("생성할 객체의 클래스 명을 입력하세요: ");
        System.out.flush();  // 버퍼 강제 flush
    }

    public TempClassDto createTempClass() {
        System.out.print("클래스의 이름을 작성해주세요: ");
        System.out.flush();
        String name = readLine();
        if (name.isEmpty()) {
            System.out.println("[Fail] 이름은 비워둘 수 없습니다.");
            throw new IllegalArgumentException();
        }
        System.out.print("\n필드를 추가하시겠습니까? (y/n): ");
        System.out.flush();
        String answer = readLine();
        if (answer.equalsIgnoreCase("n")) {
            return new TempClassDto(name, Collections.emptyList());
        } else if (answer.equalsIgnoreCase("y")) {
            return new TempClassDto(name, addFieldsToClass());
        }
        throw new IllegalArgumentException();
    }

    private List<Field> addFieldsToClass() {
        System.out.println("\n필드 추가 (종료하려면 빈 문자 입력):");
        List<Field> fields = new ArrayList<>();
        while (true) {
            System.out.print("  필드 타입 (ex: String): ");
            System.out.flush();
            String fieldType = readLine();
            if (fieldType.isEmpty()) {
                break;
            }
            System.out.print("  필드 이름 (ex: id): ");
            System.out.flush();
            String fieldName = readLine();
            fields.add(new Field(fieldType, fieldName));
            System.out.println("  [Success] 필드 추가됨: " + fieldType + " " + fieldName);
        }
        return fields;
    }


    public String selectReferenceObj() {
        System.out.print("\n참조할 객체의 변수명을 입력하세요: ");
        System.out.flush();
        return readLine();
    }
}
