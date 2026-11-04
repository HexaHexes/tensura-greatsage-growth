package org.bitwisemadness.tensuragreatsagegrowth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

/** The completion diff: pure logic that turns the addon's analysis set growth into events. */
class CompletionDiffTest {

    @Test
    void newIdsComeOutAsCompletions() {
        Set<String> tracked = new LinkedHashSet<>(Set.of("tensura:old_one", "tensura:old_two"));
        Set<String> current = new LinkedHashSet<>(Set.of("tensura:old_one", "tensura:old_two", "tensura:aspectual_fire"));
        List<String> fresh = CompletionDiff.newCompletions(tracked, current);
        assertEquals(List.of("tensura:aspectual_fire"), fresh);
    }

    @Test
    void unchangedSetsProduceNothing() {
        Set<String> tracked = Set.of("tensura:a", "tensura:b");
        Set<String> current = Set.of("tensura:a", "tensura:b");
        assertTrue(CompletionDiff.newCompletions(tracked, current).isEmpty());
    }

    @Test
    void removalsAreNotCompletions() {
        Set<String> tracked = new LinkedHashSet<>(Set.of("tensura:a", "tensura:b"));
        Set<String> current = new LinkedHashSet<>(Set.of("tensura:a"));
        assertTrue(CompletionDiff.newCompletions(tracked, current).isEmpty());
    }

    @Test
    void firstObservationTracksEverythingWithoutEvents() {
        Set<String> current = Set.of("tensura:x", "tensura:y");
        // the baseline case: a fresh SEEN set built FROM current diffed against current
        assertTrue(CompletionDiff.newCompletions(new LinkedHashSet<>(current), current).isEmpty());
    }
}
