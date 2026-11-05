package org.bitwisemadness.tensuragreatsagegrowth.gametest;

import dev.dedek.greatsage.SageNode;
import dev.dedek.greatsage.server.AnalysisManager;
import dev.dedek.greatsage.server.SageProgress;
import io.github.manasmods.manascore.skill.api.ManasSkill;
import io.github.manasmods.manascore.skill.api.SkillAPI;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import org.bitwisemadness.tensuragreatsagegrowth.FamilyUnlocks;
import org.bitwisemadness.tensuragreatsagegrowth.SageFamily;
import org.bitwisemadness.tensuragreatsagegrowth.TensuraGreatSageGrowth;

/**
 * The Spiritual growth node against the real analysis gate (stack PR 3): with
 * the family unlocked at #3's ruling, {@code AnalysisManager.start} must refuse
 * every spiritual skill before the unlock and let at least one through after
 * it - the mixin gate proven against live Tensura skills, not a stub.
 */
@GameTestHolder(TensuraGreatSageGrowth.MOD_ID)
@PrefixGameTestTemplate(false)
public final class SpiritualUpgradeGameTests {

    private SpiritualUpgradeGameTests() {
    }

    /** The family gate refuses everything before the unlock and opens after it. */
    @GameTest(template = "empty")
    public static void spiritual_gate_opens_with_unlock(GameTestHelper helper) {
        ServerPlayer player = MockPlayers.create(helper);
        // fixture: RANK_UNIQUE's gate (level 20) and the family thread up to
        // Spiritual - Resistance first, then Aspectual. RANK_EXTRA stays
        // locked on purpose: the requirement is the family thread, not the ranks.
        SageProgress.setXp(player, SageProgress.xpForLevel(SageNode.RANK_UNIQUE.minLevel()));
        SageProgress.unlock(player, SageNode.RANK_RESISTANCE.id());
        helper.assertTrue(SageProgress.has(player, SageNode.RANK_RESISTANCE),
                "fixture: the Resistance rank node must unlock");

        // owner ruling: spiritual requires aspectual - with level and points
        // fine but Aspectual locked, the purchase must refuse
        helper.assertTrue(!FamilyUnlocks.purchase(player, SageFamily.SPIRITUAL),
                "spiritual must refuse while aspectual is locked");
        helper.assertTrue(!FamilyUnlocks.isUnlocked(player, SageFamily.SPIRITUAL),
                "the refused purchase must not record the unlock");
        helper.assertTrue(FamilyUnlocks.purchase(player, SageFamily.ASPECTUAL),
                "fixture: aspectual must be purchasable");

        // live spiritual skills; granted to the player, because the addon's own
        // analysis requires ownership (its notOwned gate)
        List<ResourceLocation> candidates = new ArrayList<>();
        for (ManasSkill skill : SkillAPI.getSkillRegistry()) {
            ResourceLocation id = skill.getRegistryName();
            if (id != null && SageFamily.SPIRITUAL.ownsSkill(skill)) {
                candidates.add(id);
            }
        }
        helper.assertTrue(!candidates.isEmpty(), "Tensura must register spiritual skills");
        var storage = SkillAPI.getSkillsFrom(player);
        for (ResourceLocation id : candidates) {
            if (storage.getSkill(id).isEmpty()) {
                ManasSkill skill = SkillAPI.getSkillRegistry().get(id);
                if (skill != null) {
                    storage.learnSkill(skill.createDefaultInstance(), Component.literal("gametest"));
                }
            }
        }

        // pre-unlock: OUR family gate refuses every spiritual skill - whatever
        // the addon's own rank/resistance/magicule gates would say
        for (ResourceLocation id : candidates) {
            AnalysisManager.start(player, id.toString());
        }
        helper.assertTrue(AnalysisManager.progressBySkill(player).isEmpty(),
                "the family gate must refuse every spiritual analysis before the unlock");

        // the purchase clears the mirrored rank gate (level fixture above,
        // points at level 20) and spends exactly RANK_UNIQUE's cost
        int pointsBefore = SageProgress.points(player);
        helper.assertTrue(FamilyUnlocks.purchase(player, SageFamily.SPIRITUAL),
                "the spiritual node must be purchasable once aspectual is unlocked");
        int spent = pointsBefore - SageProgress.points(player);
        helper.assertTrue(spent == SageNode.RANK_UNIQUE.cost(),
                "the purchase must spend exactly RANK_UNIQUE's cost ("
                        + SageNode.RANK_UNIQUE.cost() + " points); got " + spent);

        // post-unlock: at least one spiritual skill must actually start analysing;
        // the refusals are read back from the mock's chat so a failure names the gate
        boolean started = false;
        StringBuilder refusals = new StringBuilder();
        for (ResourceLocation id : candidates) {
            if (AnalysisManager.progressBySkill(player).containsKey(id.toString())) {
                started = true;
                break;
            }
            AnalysisManager.start(player, id.toString());
            if (AnalysisManager.progressBySkill(player).containsKey(id.toString())) {
                started = true;
                break;
            }
            String said = ChatProbe.lastSystemChat(player);
            refusals.append(id).append(" -> ").append(said).append("; ");
        }
        helper.assertTrue(started,
                "after the unlock at least one spiritual analysis must start. refusals: " + refusals);
        helper.succeed();
    }
}
