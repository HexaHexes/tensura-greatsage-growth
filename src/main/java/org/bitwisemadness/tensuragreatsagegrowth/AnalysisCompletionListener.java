package org.bitwisemadness.tensuragreatsagegrowth;

import io.github.manasmods.manascore.skill.api.ManasSkill;
import io.github.manasmods.manascore.skill.api.SkillAPI;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/**
 * Family analysis reporting: when an analysis of a family skill completes, the
 * Sage reports it with its family context (the "aspect reporting" piece of the
 * approved stack - issue #2).
 *
 * <p>Completions are observed as growth of the addon's own
 * {@code analysedSkills} set on a server player tick; the first observation of
 * a player only establishes the baseline, so completions that predate this mod
 * never re-announce. The diff itself is a pure function so unit tests do not
 * need a server.</p>
 */
public final class AnalysisCompletionListener {

    /** The player's per-session baseline of already-analysed skill ids. */
    private static final Map<UUID, Set<String>> SEEN = new ConcurrentHashMap<>();

    /** Throttle: one observation per second of game time. */
    private static final int OBSERVE_INTERVAL = 20;

    private AnalysisCompletionListener() {
    }

    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        if (player.tickCount % OBSERVE_INTERVAL != 0) {
            return;
        }
        Set<String> current = dev.dedek.greatsage.server.AnalysisManager.analysedSkills(player);
        Set<String> seen = SEEN.computeIfAbsent(player.getUUID(), id -> new HashSet<>(current));
        for (String skillId : CompletionDiff.newCompletions(seen, current)) {
            seen.add(skillId);
            SageFamily family = FamilySkills.familyOf(skillId);
            if (family == null || !FamilyUnlocks.isUnlocked(player, family)) {
                continue;
            }
            player.displayClientMessage(
                    Component.translatable("greatsagegrowth.analysisReport", family.displayName(), skillName(skillId))
                            .withStyle(ChatFormatting.AQUA), false);
        }
    }


    /** The skill's display name from the registry, falling back to the raw id. */
    private static String skillName(String skillId) {
        var id = net.minecraft.resources.ResourceLocation.tryParse(skillId);
        if (id == null) {
            return skillId;
        }
        var registry = SkillAPI.getSkillRegistry();
        if (registry == null) {
            return skillId;
        }
        ManasSkill skill = registry.get(id);
        if (skill == null) {
            return skillId;
        }
        var name = skill.getName();
        return name == null ? skillId : name.getString();
    }
}
