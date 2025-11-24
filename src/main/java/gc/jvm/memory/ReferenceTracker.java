package gc.jvm.memory;

import gc.jvm.classs.JVMObject;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Old Generation -> Young Generation 참조를 추적하기 위한 간단한 Remembered Set
 */
public final class ReferenceTracker {
    private static YoungGeneration youngGen;
    private static OldGeneration oldGen;
    private static final Set<JVMObject> rememberedSet = new HashSet<>();

    private ReferenceTracker() {
    }

    public static void initialize(YoungGeneration youngGeneration, OldGeneration oldGeneration) {
        youngGen = youngGeneration;
        oldGen = oldGeneration;
        rememberedSet.clear();
    }

    public static void onReferenceAdded(JVMObject from, JVMObject to) {
        if (youngGen == null || oldGen == null) {
            return;
        }

        if (oldGen.contains(from) && youngGen.contains(to)) {
            rememberedSet.add(from);
        }
    }

    public static Set<JVMObject> getRememberedSetSnapshot() {
        if (rememberedSet.isEmpty() || oldGen == null) {
            return Collections.emptySet();
        }

        Set<JVMObject> snapshot = new HashSet<>();
        for (JVMObject obj : rememberedSet) {
            if (oldGen.contains(obj)) {
                snapshot.add(obj);
            }
        }
        return snapshot;
    }


    public static void pruneStaleEntries() {
        if (oldGen == null || rememberedSet.isEmpty()) {
            return;
        }
        rememberedSet.removeIf(obj -> !oldGen.contains(obj));
    }
}

