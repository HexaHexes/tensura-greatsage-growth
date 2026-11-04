package org.bitwisemadness.tensuragreatsagegrowth.net;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;
import org.bitwisemadness.tensuragreatsagegrowth.SageFamily;

/**
 * The client-side mirror of the unlock flags (stack PR A, issue #10): the
 * payload handler writes here from the network thread via enqueueWork, and the
 * screens read here (stack PRs B and C). Empty until the first payload - the
 * screens treat that as "nothing unlocked yet", which is the same answer the
 * server would give before the first sync.
 */
public final class ClientFamilyUnlocks {

    private static volatile Set<SageFamily> unlocked = Collections.emptySet();

    private ClientFamilyUnlocks() {
    }

    /** Apply a payload snapshot (network thread entry - handler enqueues). */
    public static void apply(FamilyUnlockPayload payload) {
        unlocked = payload.families();
    }

    /** Whether the local client believes the family is unlocked. */
    public static boolean isUnlocked(SageFamily family) {
        return unlocked.contains(family);
    }

    /** The full current snapshot (tests and screen code) - a copy, never the field. */
    public static Set<SageFamily> snapshot() {
        return Set.copyOf(unlocked);
    }

    /** Test seam: replace the snapshot directly. */
    public static void setForTesting(EnumSet<SageFamily> families) {
        unlocked = Set.copyOf(families);
    }
}
