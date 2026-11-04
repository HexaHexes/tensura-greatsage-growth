package org.bitwisemadness.tensuragreatsagegrowth.gametest;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import org.bitwisemadness.tensuragreatsagegrowth.SageFamily;
import org.bitwisemadness.tensuragreatsagegrowth.TensuraGreatSageGrowth;
import org.bitwisemadness.tensuragreatsagegrowth.net.FamilyUnlockPayload;

/**
 * The unlock payload's flag mapping (stack PR A, issue #10) - it crosses MC
 * types (CustomPacketPayload), so it lives here rather than in the pure unit
 * tests, which run without the game classpath.
 */
@GameTestHolder(TensuraGreatSageGrowth.MOD_ID)
@PrefixGameTestTemplate(false)
public final class FamilyUnlockPayloadGameTests {

    private FamilyUnlockPayloadGameTests() {
    }

    /** Flags map both directions for none, all and partial unlocks. */
    @GameTest(template = "empty")
    public static void unlock_flags_map(GameTestHelper helper) {
        FamilyUnlockPayload none = FamilyUnlockPayload.of(List.of());
        helper.assertTrue(!none.aspectual() && !none.spiritual() && !none.summon(),
                "no keys must map to no flags");

        FamilyUnlockPayload all = FamilyUnlockPayload.of(List.of(
                SageFamily.ASPECTUAL.unlockKey(),
                SageFamily.SPIRITUAL.unlockKey(),
                SageFamily.SUMMON.unlockKey()));
        helper.assertTrue(all.aspectual() && all.spiritual() && all.summon(),
                "all keys must map to all flags");
        helper.assertTrue(all.families().equals(EnumSet.allOf(SageFamily.class)),
                "all flags must read back as all families");

        FamilyUnlockPayload one = FamilyUnlockPayload.of(Set.of(SageFamily.SPIRITUAL.unlockKey()));
        helper.assertTrue(!one.aspectual() && one.spiritual() && !one.summon(),
                "a partial unlock must map only that family");
        helper.assertTrue(one.families().equals(EnumSet.of(SageFamily.SPIRITUAL)),
                "the partial payload must read back as exactly one family");

        helper.succeed();
    }
}
