package gc.jvm;

import gc.jvm.view.MenuOption;
import gc.jvm.view.View;
import gc.jvm.view.handler.HandlerContext;

public class InteractiveJVM {
    private final View view;
    private final YoonJVM jvm;
    private final HandlerContext context;

    public InteractiveJVM() {
        this.view = new View();
        this.jvm = new YoonJVM();
        this.context = new HandlerContext(jvm, view);
        // 초기 메서드 프레임 생성 (main)
        jvm.invokeMethod("main", 30);
    }

    /**
     * 메인 실행 루프
     */
    public void run() {
        view.welcome();

        while (context.getSystemHandler().isRunning()) {
            try {
                int choice = view.selecetMenu();
                MenuOption option = MenuOption.fromOptionNumber(choice);
                option.execute(context);
                pause();
            } catch (NumberFormatException e) {
                view.println("[Fail] 숫자를 입력해 주세요.");
            } catch (IllegalArgumentException e) {
                view.println("[Fail] 유효하지 않은 옵션입니다. 다시 시도해 주세요.");
            } catch (Exception e) {
                view.println("[Fail] 오류 발생: " + e.getMessage());
            }
        }
        close();
    }

    private void close() {
        view.close();
    }

    private void pause() {
        try {
            Thread.sleep(2000);  // 0.5초 대기
        } catch (InterruptedException e) {
        }
    }
    public static void main(String[] args) {
        InteractiveJVM interactive = new InteractiveJVM();
        interactive.run();
    }

}
