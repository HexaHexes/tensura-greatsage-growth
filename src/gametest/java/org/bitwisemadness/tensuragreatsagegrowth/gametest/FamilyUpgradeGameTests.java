package org.bitwisemadness.tensuragreatsagegrowth.gametest;

import dev.dedek.greatsage.server.SageProgress;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import org.bitwisemadness.tensuragreatsagegrowth.FamilyUnlocks;
import org.bitwisemadness.tensuragreatsagegrowth.SageFamily;
import org.bitwisemadness.tensuragreatsagegrowth.TensuraGreatSageGrowth;

/**
 * The Aspectual growth node, end to end: the purchase spends the Sage's XP
 * through the addon's own economy and records the unlock in the addon's own
 * progression NBT - the exact state the analysis gate reads (stack PR 2).
 */
@GameTestHolder(TensuraGreatSageGrowth.MOD_ID)
@PrefixGameTestTemplate(false)
public final class FamilyUpgradeGameTests {

    private FamilyUpgradeGameTests() {
    }

    /** Purchase: enough level and XP buys the node, spends the XP, and is idempotent. */
    @GameTest(template = "empty")
    public static void aspectual_purchase(GameTestHelper helper) {
        ServerPlayer player = MockPlayers.create(helper);
        SageProgress.setXp(player, 1_000_000);
        int before = SageProgress.xp(player);

        helper.assertTrue(!FamilyUnlocks.isUnlocked(player, SageFamily.ASPECTUAL),
                "the node must start locked");

        helper.assertTrue(FamilyUnlocks.purchase(player, SageFamily.ASPECTUAL),
                "the purchase must succeed with level and XP to spare");
        helper.assertTrue(FamilyUnlocks.isUnlocked(player, SageFamily.ASPECTUAL),
                "the unlock must land in the addon's progression NBT");
        helper.assertTrue(SageProgress.xp(player) == before - FamilyUnlocks.XP_COST,
                "the purchase must spend exactly " + FamilyUnlocks.XP_COST + " XP; got "
                        + (before - SageProgress.xp(player)));

        String census = ChatProbe.outboundCensus(player);
        helper.assertTrue(census.contains("Payload"),
                "the purchase must sync the unlock flags to the client; outbound: " + census);
        int afterFirst = SageProgress.xp(player);
        helper.assertTrue(!FamilyUnlocks.purchase(player, SageFamily.ASPECTUAL),
                "a second purchase must refuse as already unlocked");
        helper.assertTrue(SageProgress.xp(player) == afterFirst,
                "the refused purchase must not spend anything");

        helper.succeed();
    }
}
