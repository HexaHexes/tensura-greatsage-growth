package org.bitwisemadness.tensuragreatsagegrowth.mixin;

import dev.dedek.greatsage.server.AnalysisManager;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import org.bitwisemadness.tensuragreatsagegrowth.FamilySkills;
import org.bitwisemadness.tensuragreatsagegrowth.FamilyUnlocks;
import org.bitwisemadness.tensuragreatsagegrowth.SageFamily;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * The family gate on the Great Sage console's analysis screen (ruling on
 * tensura-greatsage-growth#3): a skill from one of the three magic families can
 * only start an analysis once the player's Sage holds the matching growth node.
 *
 * <p>The check runs at the head of the addon's own {@code start} entry point,
 * before any of its slot and rank machinery, so a locked family behaves exactly
 * like one of the addon's own refusals - a red message and no analysis. When
 * the unlock is present the head check passes and the addon's flow proceeds
 * untouched (its rank, resistance and ownership gates still apply).</p>
 */
@Mixin(AnalysisManager.class)
public class AnalysisGateMixin {

    @Inject(
            method = "start(Lnet/minecraft/server/level/ServerPlayer;Ljava/lang/String;)V",
            at = @At("HEAD"),
            cancellable = true)
    private static void greatsagegrowth$gateFamily(ServerPlayer player, String rawSkillId, CallbackInfo ci) {
        SageFamily family = FamilySkills.familyOf(rawSkillId);
        if (family != null && !FamilyUnlocks.isUnlocked(player, family)) {
            player.displayClientMessage(
                    Component.translatable("greatsagegrowth.familyLocked", family.displayName())
                            .withStyle(ChatFormatting.RED), false);
            ci.cancel();
        }
    }
}
