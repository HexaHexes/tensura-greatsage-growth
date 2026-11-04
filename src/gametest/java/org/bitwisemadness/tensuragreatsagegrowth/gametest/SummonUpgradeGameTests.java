package org.bitwisemadness.tensuragreatsagegrowth.gametest;

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
        SageProgress.setXp(player, 10_000_000);

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

        helper.assertTrue(FamilyUnlocks.purchase(player, SageFamily.SUMMON),
                "the summon node must be purchasable with level and XP to spare");

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
