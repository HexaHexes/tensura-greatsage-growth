package org.bitwisemadness.tensuragreatsagegrowth;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * The completion diff, deliberately free of Minecraft types so unit tests can
 * load it without the game classpath: the ids present in current but not in
 * tracked are the analyses that completed since the last observation.
 */
public final class CompletionDiff {

    private CompletionDiff() {
    }

    public static List<String> newCompletions(Set<String> tracked, Set<String> current) {
        List<String> fresh = new ArrayList<>();
        for (String id : current) {
            if (!tracked.contains(id)) {
                fresh.add(id);
            }
        }
        return fresh;
    }
}
