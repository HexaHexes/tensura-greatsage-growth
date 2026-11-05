package org.bitwisemadness.tensuragreatsagegrowth.gametest;

import dev.dedek.greatsage.SageNode;
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
 * The Aspectual growth node, end to end (owner ruling: growth points, never
 * XP): the purchase spends Great Sage points - exactly the mirrored rank
 * node's cost - and records the unlock in the addon's own progression NBT,
 * the exact state the analysis gate reads (stack PR 2).
 */
@GameTestHolder(TensuraGreatSageGrowth.MOD_ID)
@PrefixGameTestTemplate(false)
public final class FamilyUpgradeGameTests {

    private FamilyUpgradeGameTests() {
    }

    /** Purchase: level + points + the Resistance requirement buy it for the rank cost. */
    @GameTest(template = "empty")
    public static void aspectual_purchase(GameTestHelper helper) {
        ServerPlayer player = MockPlayers.create(helper);
        // fixture: the mirrored rank gate (RANK_EXTRA: level 9) and the
        // family thread's start - the Resistance rank node
        SageProgress.setXp(player, SageProgress.xpForLevel(SageNode.RANK_EXTRA.minLevel()));
        SageProgress.unlock(player, SageNode.RANK_RESISTANCE.id());
        helper.assertTrue(SageProgress.has(player, SageNode.RANK_RESISTANCE),
                "fixture: the Resistance rank node must unlock");

        // owner ruling: cost and level mirror the rank nodes - the pure table's
        // copy must not drift from the addon's live values (unit tests run
        // without the addon jar, hence the pin lives here)
        helper.assertTrue(SageFamily.ASPECTUAL.minLevel() == SageNode.RANK_EXTRA.minLevel(),
                "aspectual's level gate must mirror RANK_EXTRA's " + SageNode.RANK_EXTRA.minLevel());
        helper.assertTrue(SageFamily.SPIRITUAL.minLevel() == SageNode.RANK_UNIQUE.minLevel(),
                "spiritual's level gate must mirror RANK_UNIQUE's " + SageNode.RANK_UNIQUE.minLevel());
        helper.assertTrue(SageFamily.SUMMON.minLevel() == SageNode.RANK_ULTIMATE.minLevel(),
                "summoning's level gate must mirror RANK_ULTIMATE's " + SageNode.RANK_ULTIMATE.minLevel());
        helper.assertTrue(FamilyUnlocks.cost(SageFamily.ASPECTUAL) == SageNode.RANK_EXTRA.cost(),
                "aspectual's cost must mirror RANK_EXTRA's " + SageNode.RANK_EXTRA.cost());

        int xpBefore = SageProgress.xp(player);
        int pointsBefore = SageProgress.points(player);
        helper.assertTrue(!FamilyUnlocks.isUnlocked(player, SageFamily.ASPECTUAL),
                "the node must start locked");

        helper.assertTrue(FamilyUnlocks.purchase(player, SageFamily.ASPECTUAL),
                "the purchase must succeed with level, points and the Resistance requirement");
        helper.assertTrue(FamilyUnlocks.isUnlocked(player, SageFamily.ASPECTUAL),
                "the unlock must land in the addon's progression NBT");
        helper.assertTrue(SageProgress.xp(player) == xpBefore,
                "the points economy must not touch XP; " + xpBefore + " -> " + SageProgress.xp(player));
        int spent = pointsBefore - SageProgress.points(player);
        helper.assertTrue(spent == SageNode.RANK_EXTRA.cost(),
                "the purchase must spend exactly the mirrored rank cost ("
                        + SageNode.RANK_EXTRA.cost() + " points); got " + spent);

        String census = ChatProbe.outboundCensus(player);
        helper.assertTrue(census.contains("Payload"),
                "the purchase must sync the unlock flags to the client; outbound: " + census);
        int pointsAfterFirst = SageProgress.points(player);
        helper.assertTrue(!FamilyUnlocks.purchase(player, SageFamily.ASPECTUAL),
                "a second purchase must refuse as already unlocked");
        helper.assertTrue(SageProgress.points(player) == pointsAfterFirst,
                "the refused purchase must not spend anything");

        helper.succeed();
    }
}
