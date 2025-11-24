package gc.jvm.view.handler;

import gc.jvm.YoonJVM;
import gc.jvm.view.View;

/**
 * 모든 핸들러를 담는 컨테이너
 * MenuOption에서 필요한 핸들러에 접근할 수 있도록 제공
 */
public class HandlerContext {
    private final GCHandler gcHandler;
    private final ObjectHandler objectHandler;
    private final MemoryHandler memoryHandler;
    private final AdvancedHandler advancedHandler;
    private final ClassHandler classHandler;
    private final SystemHandler systemHandler;

    public HandlerContext(YoonJVM jvm, View view) {
        this.gcHandler = new GCHandler(jvm);
        this.objectHandler = new ObjectHandler(jvm, view);
        this.memoryHandler = new MemoryHandler(jvm);
        this.advancedHandler = new AdvancedHandler(jvm, view);
        this.classHandler = new ClassHandler(jvm, view);
        this.systemHandler = new SystemHandler();
    }

    public GCHandler getGcHandler() {
        return gcHandler;
    }

    public ObjectHandler getObjectHandler() {
        return objectHandler;
    }

    public MemoryHandler getMemoryHandler() {
        return memoryHandler;
    }

    public AdvancedHandler getAdvancedHandler() {
        return advancedHandler;
    }

    public ClassHandler getClassHandler() {
        return classHandler;
    }

    public SystemHandler getSystemHandler() {
        return systemHandler;
    }
}
