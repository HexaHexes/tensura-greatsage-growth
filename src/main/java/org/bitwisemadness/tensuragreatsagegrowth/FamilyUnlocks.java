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

    /** The addon's persisted-data bucket this mod keeps its own unlocks in. */
    private static final String GROWTH_ROOT = "greatsagegrowth";

    /**
     * Whether the player's Sage has the family unlock.
     *
     * <p>The addon's own unlocked-set is enum-backed ({@code SageNode.byId}
     * drops unknown keys - see #1), so the three growth nodes live in this
     * mod's own bucket of the same persisted data. The analysis gate and the
     * purchase flow both read here.</p>
     */
    public static boolean isUnlocked(ServerPlayer player, SageFamily family) {
        return player.getPersistentData().getCompound(GROWTH_ROOT).getBoolean(family.unlockKey());
    }

    /**
     * Speak the unlock through the Great Sage addon's own voice API, so the
     * node lands with the same fanfare as the addon's own tree unlocks. The
     * voice line is the addon's native "acquired" line; our mod ships no
     * voice assets of its own (see the PR notes on stack 5).
     */
    private static void announce(ServerPlayer player, SageFamily family) {
        var server = player.getServer();
        if (server == null) {
            return;
        }
        dev.dedek.greatsage.api.WorldVoice.announce(
                server,
                "voice.shutoku_zumi",
                Component.translatable("greatsagegrowth.unlocked", family.displayName())
                        .withStyle(ChatFormatting.GOLD),
                Component.empty(),
                player.getUUID());
    }
    private static void record(ServerPlayer player, SageFamily family) {
        net.minecraft.nbt.CompoundTag root = player.getPersistentData().getCompound(GROWTH_ROOT);
        root.putBoolean(family.unlockKey(), true);
        player.getPersistentData().put(GROWTH_ROOT, root);
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
        if (SageProgress.level(player) < family.minLevel()) {
            player.displayClientMessage(
                    Component.translatable("greatsagegrowth.needLevel", family.minLevel(), family.displayName())
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
        record(player, family);
        player.displayClientMessage(
                Component.translatable("greatsagegrowth.unlocked", family.displayName())
                        .withStyle(ChatFormatting.GOLD), false);
        announce(player, family);
        return true;
    }
}
