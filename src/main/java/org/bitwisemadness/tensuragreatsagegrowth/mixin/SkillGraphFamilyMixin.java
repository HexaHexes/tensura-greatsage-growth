package org.bitwisemadness.tensuragreatsagegrowth.mixin;

import io.github.manasmods.manascore.skill.api.ManasSkill;
import io.github.manasmods.manascore.skill.api.ManasSkillInstance;
import io.github.manasmods.manascore.skill.api.SkillAPI;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import org.bitwisemadness.tensuragreatsagegrowth.SageFamily;
import org.bitwisemadness.tensuragreatsagegrowth.net.ClientFamilyUnlocks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * The analysis screen's family entries (stack PR B, issue #10): the addon's
 * skill browser builds its node graph from the LEARNED skills through an
 * {@code instanceof Skill} filter, so magic-family skills never enter it.
 *
 * <p>This mixin injects just before {@code buildFacets()} - at that point the
 * addon has filled {@code byTier} and {@code tiers} but has not yet derived the
 * facet options or the visible set from them - and appends one bucket per
 * <em>unlocked</em> family, holding that family's learned skills as the addon's
 * own {@code Node} records (built reflectively: the record's constructor is
 * private, and its canonical shape is an implementation detail of the addon's
 * package). The tier key is the family's package fragment ("aspectual",
 * "spiritual", "summon") - lowercase like every other tier, unknown to
 * {@code rankLocked}'s switch (so not rank-locked), and absent from
 * {@code RANK_*} gating, which is exactly the policy from #3: the family node
 * IS the progression gate. {@code Node.analysable()} then reads
 * {@code !mastered} for these buckets, so learned family skills carry the
 * analysable badge automatically.</p>
 *
 * <p>Client-only: declared in the mixins.json {@code client} array, so
 * dedicated servers (including the GameTest harness) never load it.</p>
 */
@Mixin(targets = "dev.dedek.greatsage.client.SkillGraph", remap = false)
public class SkillGraphFamilyMixin {

    @Shadow
    private Map<String, List<Object>> byTier;

    @Shadow
    private List<String> tiers;

    @Inject(
            method = "rebuild(Lnet/minecraft/client/player/LocalPlayer;)V",
            at = @At(value = "INVOKE", target = "Ldev/dedek/greatsage/client/SkillGraph;buildFacets()V", remap = false))
    private void greatsagegrowth$appendFamilyBuckets(LocalPlayer player, CallbackInfo ci) {
        Map<String, List<Object>> staging = new java.util.LinkedHashMap<>();
        for (SageFamily family : SageFamily.values()) {
            if (!ClientFamilyUnlocks.isUnlocked(family)) {
                continue;
            }
            List<Object> nodes = new ArrayList<>();
            for (ManasSkillInstance instance : SkillAPI.getSkillsFrom(player).getLearnedSkills()) {
                ManasSkill skill = instance.getSkill();
                if (skill == null || !family.ownsSkillClassName(skill.getClass().getName())) {
                    continue;
                }
                ResourceLocation id = skill.getRegistryName();
                if (id == null) {
                    continue;
                }
                nodes.add(newFamilyNode(id, skill.getName(), family.tensuraPackage(), instance.isMastered(player)));
            }
            if (!nodes.isEmpty()) {
                staging.put(family.tensuraPackage(), nodes);
            }
        }
        for (Map.Entry<String, List<Object>> entry : staging.entrySet()) {
            if (!byTier.containsKey(entry.getKey())) {
                byTier.put(entry.getKey(), entry.getValue());
                if (!tiers.contains(entry.getKey())) {
                    tiers.add(entry.getKey());
                }
            }
        }
    }

    /**
     * The addon's Node record has a private constructor - reached reflectively
     * from this mixin's package (classpath classes are not module-encapsulated,
     * so setAccessible is stable in dev and in the production mod jar alike).
     * Returns Object: the record type itself is package-private.
     */
    private static Object newFamilyNode(ResourceLocation id, net.minecraft.network.chat.Component name,
            String tier, boolean mastered) {
        try {
            Class<?> nodeClass = Class.forName("dev.dedek.greatsage.client.SkillGraph$Node");
            Constructor<?> ctor = nodeClass.getDeclaredConstructor(
                    ResourceLocation.class, net.minecraft.network.chat.Component.class, String.class, boolean.class);
            ctor.setAccessible(true);
            return ctor.newInstance(id, name, tier, mastered);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("cannot build the family node for the analysis screen", e);
        }
    }
}
