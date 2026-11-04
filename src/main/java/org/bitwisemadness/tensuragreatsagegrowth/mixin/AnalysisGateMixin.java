package org.bitwisemadness.tensuragreatsagegrowth.mixin;

import dev.dedek.greatsage.server.AnalysisManager;
import dev.dedek.greatsage.server.SageProgress;
import io.github.manasmods.manascore.skill.api.ManasSkillInstance;
import io.github.manasmods.manascore.skill.api.SkillAPI;
import io.github.manasmods.tensura.storage.TensuraStorages;
import io.github.manasmods.tensura.storage.ep.IExistence;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.bitwisemadness.tensuragreatsagegrowth.FamilySkills;
import org.bitwisemadness.tensuragreatsagegrowth.FamilyUnlocks;
import org.bitwisemadness.tensuragreatsagegrowth.SageFamily;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * The family analysis entry on the Great Sage console's analysis screen
 * (ruling on tensura-greatsage-growth#3).
 *
 * <p><b>Why a take-over instead of patching the addon's chain.</b> The addon's
 * {@code start()} was built for Tensura's {@code Skill} branch only: it rejects
 * anything that is not {@code instanceof Skill} as "unknown", and the whole
 * {@code Magic} branch hangs off {@code TensuraSkill} as a <em>sibling</em> of
 * {@code Skill} - no magic skill can enter that chain (verified live: 40/40
 * refused), and this Mixin version cannot even target the {@code instanceof}.
 * So for an approved family skill this mixin cancels the addon's entry and
 * runs the equivalent flow itself, through the addon's own private machinery
 * (shadowed {@code refuse}, {@code duration}, {@code slots}, {@code even},
 * {@code sync}): native refusal messages and voice lines, the config-derived
 * duration ({@code duration()} is instance-guarded and returns
 * {@code ANALYSIS_TICKS} for non-Skill instances), and the identical slot NBT
 * contract the addon's own {@code tick()} consumes - which needs no patching at
 * all (it only touches {@code ManasSkillInstance}).</p>
 *
 * <p><b>Kept from the base flow:</b> busy check, thread (slot) capacity,
 * ownership, temporary-skill refusal, duration &gt; 0, and the magicule cost
 * gate with deduction. <b>Deliberately replaced by the family node:</b> the
 * rank gate (the node IS the progression gate for magic) and the resistance /
 * latent path (family skills are never of the resistance type). The mastery
 * gate is not applied to family skills in v1 - documented in the PRs.</p>
 */
@Mixin(AnalysisManager.class)
public class AnalysisGateMixin {

    /** The magicule cost of one family analysis (small, positive). */
    private static final double FAMILY_ANALYSIS_COST = 5.0D;

    @Shadow
    private static void refuse(ServerPlayer player, Component message, String voiceLine) {
        throw new AssertionError("shadow");
    }

    @Shadow
    private static int duration(ManasSkillInstance instance) {
        throw new AssertionError("shadow");
    }

    @Shadow
    private static net.minecraft.nbt.ListTag slots(ServerPlayer player) {
        throw new AssertionError("shadow");
    }

    @Shadow
    private static void even(ServerPlayer player) {
        throw new AssertionError("shadow");
    }

    @Shadow
    private static void sync(ServerPlayer player, String voiceLine) {
        throw new AssertionError("shadow");
    }

    @Inject(
            method = "start(Lnet/minecraft/server/level/ServerPlayer;Ljava/lang/String;)V",
            at = @At("HEAD"),
            cancellable = true)
    private static void greatsagegrowth$startFamilyAnalysis(ServerPlayer player, String rawSkillId, CallbackInfo ci) {
        SageFamily family = FamilySkills.familyOf(rawSkillId);
        if (family == null) {
            return; // not a family skill (or unparseable) - the addon's own flow
        }
        if (!FamilyUnlocks.isUnlocked(player, family)) {
            refuse(player,
                    Component.translatable("greatsagegrowth.familyLocked", family.displayName()),
                    "voice.kinshi");
            ci.cancel();
            return;
        }
        ci.cancel();

        ResourceLocation id = ResourceLocation.tryParse(rawSkillId.trim());
        if (id == null) {
            refuse(player, Component.translatable("greatsage.analysis.unknown"), "voice.kaiseki_fuka");
            return;
        }
        if (AnalysisManager.progressBySkill(player).containsKey(id.toString())) {
            refuse(player, Component.translatable("greatsage.analysis.busy"), "voice.kaiseki_chuu");
            return;
        }
        int threads = SageProgress.slots(player);
        if (AnalysisManager.analysedSkills(player).size() >= threads) {
            refuse(player,
                    Component.translatable("greatsage.analysis.noSlot", threads),
                    "voice.heiretsu_jougen");
            return;
        }
        ManasSkillInstance instance = SkillAPI.getSkillsFrom(player).getSkill(id).orElse(null);
        if (instance == null) {
            refuse(player, Component.translatable("greatsage.analysis.notOwned"), "voice.hoyuu_nashi");
            return;
        }
        if (instance.isTemporarySkill() || instance.shouldRemove()) {
            refuse(player, Component.translatable("greatsage.analysis.temporary"), "voice.ichijiteki");
            return;
        }
        int total = duration(instance);
        if (total <= 0) {
            refuse(player, Component.translatable("greatsage.analysis.forbidden"), "voice.kinshi");
            return;
        }
        IExistence existence = TensuraStorages.getExistenceFrom(player);
        if (existence.getMagicule() < FAMILY_ANALYSIS_COST) {
            refuse(player,
                    Component.translatable("greatsage.analysis.noMagicule",
                            Math.round(FAMILY_ANALYSIS_COST)),
                    "voice.maso_fusoku");
            return;
        }
        existence.setMagicule(existence.getMagicule() - FAMILY_ANALYSIS_COST);

        // The identical slot contract the addon's own tick() consumes.
        CompoundTag entry = new CompoundTag();
        entry.putString("skill", id.toString());
        entry.putInt("left", total);
        entry.putInt("total", total);
        entry.putInt("share", 0);
        entry.putInt("residue", 0);
        entry.putDouble("cost", Math.max(0.0D, FAMILY_ANALYSIS_COST));
        slots(player).add(entry);
        even(player);
        sync(player, "voice.kaiseki_kaishi");
    }
}
