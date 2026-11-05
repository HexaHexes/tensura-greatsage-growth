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
 * The Summoning growth node against the real analysis gate (stack PR 4): the
 * deepest family node - every live summon-family skill refused before the
 * unlock, at least one analysing after it.
 */
@GameTestHolder(TensuraGreatSageGrowth.MOD_ID)
@PrefixGameTestTemplate(false)
public final class SummonUpgradeGameTests {

    private SummonUpgradeGameTests() {
    }

    /** The family gate refuses every summon skill before the unlock and opens after it. */
    @GameTest(template = "empty")
    public static void summon_gate_opens_with_unlock(GameTestHelper helper) {
        ServerPlayer player = MockPlayers.create(helper);
        // fixture: RANK_ULTIMATE's gate (level 38) and the whole family
        // thread - Resistance, Aspectual, Spiritual. RANK_EXTRA/RANK_UNIQUE
        // stay locked on purpose: summoning requires spiritual, not unique.
        SageProgress.setXp(player, SageProgress.xpForLevel(SageNode.RANK_ULTIMATE.minLevel()));
        SageProgress.unlock(player, SageNode.RANK_RESISTANCE.id());
        helper.assertTrue(FamilyUnlocks.purchase(player, SageFamily.ASPECTUAL),
                "fixture: aspectual must be purchasable");
        helper.assertTrue(FamilyUnlocks.purchase(player, SageFamily.SPIRITUAL),
                "fixture: spiritual must be purchasable once aspectual is unlocked");

        List<ResourceLocation> candidates = new ArrayList<>();
        for (ManasSkill skill : SkillAPI.getSkillRegistry()) {
            ResourceLocation id = skill.getRegistryName();
            if (id != null && SageFamily.SUMMON.ownsSkill(skill)) {
                candidates.add(id);
            }
        }
        helper.assertTrue(!candidates.isEmpty(), "Tensura must register summon skills");
        var storage = SkillAPI.getSkillsFrom(player);
        for (ResourceLocation id : candidates) {
            if (storage.getSkill(id).isEmpty()) {
                ManasSkill skill = SkillAPI.getSkillRegistry().get(id);
                if (skill != null) {
                    storage.learnSkill(skill.createDefaultInstance(), Component.literal("gametest"));
                }
            }
        }

        for (ResourceLocation id : candidates) {
            AnalysisManager.start(player, id.toString());
        }
        helper.assertTrue(AnalysisManager.progressBySkill(player).isEmpty(),
                "the family gate must refuse every summon analysis before the unlock");

        // the purchase clears the mirrored rank gate (level fixture above,
        // points at level 38) and spends exactly RANK_ULTIMATE's cost
        int pointsBefore = SageProgress.points(player);
        helper.assertTrue(FamilyUnlocks.purchase(player, SageFamily.SUMMON),
                "the summon node must be purchasable once spiritual is unlocked");
        int spent = pointsBefore - SageProgress.points(player);
        helper.assertTrue(spent == SageNode.RANK_ULTIMATE.cost(),
                "the purchase must spend exactly RANK_ULTIMATE's cost ("
                        + SageNode.RANK_ULTIMATE.cost() + " points); got " + spent);

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
                "after the unlock at least one summon analysis must start. refusals: " + refusals);
        helper.succeed();
    }
}
