package gc.jvm.view.handler;

/**
 * 시스템 관련 작업을 처리하는 핸들러
 */
public class SystemHandler {
    private boolean running;

    public SystemHandler() {
        this.running = true;
    }

    /**
     * 프로그램 종료
     */
    public void exit() {
        running = false;
    }

    /**
     * 실행 중인지 확인
     */
    public boolean isRunning() {
        return running;
    }
}
