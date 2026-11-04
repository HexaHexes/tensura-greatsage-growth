package org.bitwisemadness.tensuragreatsagegrowth;

import dev.dedek.greatsage.server.SageProgress;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

/**
 * Reading and spending the Great Sage addon's own progression state.
 *
 * <p>The addon stores unlocked nodes as plain strings in the player's persisted
 * data, so this mod's growth nodes live in the same set (see issue #1: the
 * research found {@code SageProgress.unlock(player, String)} /
 * {@code unlocked(player)} are public). Acquisition spends raw XP through the
 * addon's public {@code setXp} - the addon's own points economy is internal to
 * its screens, so XP is the honest currency this mod can spend.</p>
 */
public final class FamilyUnlocks {

    /** The XP cost of one family unlock. */
    public static final int XP_COST = 1000;

    private FamilyUnlocks() {
    }

    /** Whether the player's Sage has the family unlock. */
    public static boolean isUnlocked(ServerPlayer player, SageFamily family) {
        return SageProgress.unlocked(player).contains(family.unlockKey());
    }

    /**
     * Buy the family unlock: requires {@link #XP_COST} XP, deducts it through the
     * addon's public XP setter and records the unlock in the addon's own
     * progression NBT. Returns false (with a chat reason) when the unlock is
     * already owned or the player cannot afford it.
     */
    public static boolean purchase(ServerPlayer player, SageFamily family) {
        if (isUnlocked(player, family)) {
            player.displayClientMessage(
                    Component.translatable("greatsagegrowth.alreadyUnlocked", family.displayName())
                            .withStyle(ChatFormatting.GRAY), true);
            return false;
        }
        int xp = SageProgress.xp(player);
        if (xp < XP_COST) {
            player.displayClientMessage(
                    Component.translatable("greatsagegrowth.needXp", XP_COST, family.displayName())
                            .withStyle(ChatFormatting.GRAY), true);
            return false;
        }
        SageProgress.setXp(player, xp - XP_COST);
        SageProgress.unlock(player, family.unlockKey());
        player.displayClientMessage(
                Component.translatable("greatsagegrowth.unlocked", family.displayName())
                        .withStyle(ChatFormatting.GOLD), false);
        return true;
    }
}
