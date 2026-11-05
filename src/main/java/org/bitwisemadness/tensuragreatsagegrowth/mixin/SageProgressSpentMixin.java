package org.bitwisemadness.tensuragreatsagegrowth.mixin;

import dev.dedek.greatsage.server.SageProgress;
import net.minecraft.server.level.ServerPlayer;
import org.bitwisemadness.tensuragreatsagegrowth.FamilyUnlocks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * The family unlocks join the addon's derived points ledger (owner ruling:
 * the unlock cost is Great Sage growth points).
 *
 * <p>{@code points = level * 8 - spent}, and the addon derives {@code spent}
 * by summing the costs of the payable nodes in the player's unlocked set -
 * enum-backed, so this mod's family keys can never appear there
 * ({@code SageNode.byId} resolves SageNode ids only). This injection extends
 * that derivation with {@link FamilyUnlocks#spent}: every unlocked family adds
 * its mirrored rank node's cost, so a family purchase lowers {@code points}
 * everywhere the addon reads them - the HUD, the client's synced field (via
 * {@code SageProgress.sync}), and the addon's own purchase gates - without a
 * public spend API, reflection, or granting any rank node as a side effect.</p>
 */
@Mixin(SageProgress.class)
public class SageProgressSpentMixin {

    @Inject(
            method = "spent(Lnet/minecraft/server/level/ServerPlayer;)I",
            at = @At("RETURN"),
            cancellable = true)
    private static void greatsagegrowth$includeFamilySpends(ServerPlayer player, CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(cir.getReturnValueI() + FamilyUnlocks.spent(player));
    }
}
