package org.bitwisemadness.tensuragreatsagegrowth;

import dev.dedek.greatsage.SageNode;
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
 * {@code unlocked(player)} are public).</p>
 *
 * <p><b>Points economy (owner ruling).</b> A family unlock costs Great Sage
 * growth points - {@code points = level * 8 - spent} - and never XP. The addon
 * derives {@code spent} from the costs of the payable nodes in its unlocked
 * set; family keys can never enter that set ({@code SageNode.byId} resolves
 * enum ids only), so this mod extends the derivation: {@link #spent} sums the
 * unlocked families and the {@code SageProgressSpentMixin} adds it inside
 * {@code SageProgress.spent}, the one choke point every points reading goes
 * through. Recording an unlock therefore lowers {@code points} everywhere the
 * addon reads them, and {@code SageProgress.sync} republishes the new total to
 * the client - no spend API, no reflection, no rank granted as a side effect.</p>
 *
 * <p><b>Mirrors and thread (owner rulings).</b> Cost and level mirror the rank
 * nodes exactly - Aspectual = Extra skills, Spiritual = Unique, Summoning =
 * Ultimate ({@link #cost}/{@link #minLevel} read {@link #rankNode} live; the
 * pure {@link SageFamily} table's copy is pinned by the gametest parity
 * assertion because unit tests run without the addon jar). The requirement
 * chain follows this family thread instead: Resistances - Aspectual -
 * Spiritual - Summoning, i.e. spiritual requires aspectual and summoning
 * requires spiritual, <em>not</em> the ranks' Extra and Unique.</p>
 */
public final class FamilyUnlocks {

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
     * The rank node whose cost and level this family mirrors (owner ruling:
     * Aspectual magic costs and gates like Extra skills, Spiritual like
     * Unique, Summoning like Ultimate).
     */
    public static SageNode rankNode(SageFamily family) {
        return switch (family) {
            case ASPECTUAL -> SageNode.RANK_EXTRA;
            case SPIRITUAL -> SageNode.RANK_UNIQUE;
            case SUMMON -> SageNode.RANK_ULTIMATE;
        };
    }

    /** The growth points the unlock costs: the mirrored rank node's cost, live. */
    public static int cost(SageFamily family) {
        return rankNode(family).cost();
    }

    /** The Sage level the unlock needs: the mirrored rank node's level, live. */
    public static int minLevel(SageFamily family) {
        return rankNode(family).minLevel();
    }

    /**
     * Growth points this mod's unlocked families have spent. The addon's
     * derived ledger cannot see them (family keys are not SageNode ids), so
     * the spent-mixin adds this sum inside {@code SageProgress.spent}.
     */
    public static int spent(ServerPlayer player) {
        int total = 0;
        for (SageFamily family : SageFamily.values()) {
            if (isUnlocked(player, family)) {
                total += cost(family);
            }
        }
        return total;
    }

    /**
     * Whether the requirement node on this family's thread is owned: the
     * Resistance rank node where the thread starts (Aspectual), the previous
     * family for the rest - owner ruling: the chain follows the family thread,
     * not the ranks' Extra and Unique.
     */
    public static boolean requirementOwned(ServerPlayer player, SageFamily family) {
        return switch (family) {
            case ASPECTUAL -> SageProgress.has(player, SageNode.RANK_RESISTANCE);
            case SPIRITUAL -> isUnlocked(player, SageFamily.ASPECTUAL);
            case SUMMON -> isUnlocked(player, SageFamily.SPIRITUAL);
        };
    }

    /**
     * Display name of the family's requirement node: the addon's own label for
     * Resistances ({@code greatsage.tree.rank_resistance}), the previous
     * family's display name for the rest.
     */
    public static Component requirementName(SageFamily family) {
        return switch (family) {
            case ASPECTUAL -> Component.translatable(SageNode.RANK_RESISTANCE.labelKey());
            case SPIRITUAL -> Component.literal(SageFamily.ASPECTUAL.displayName());
            case SUMMON -> Component.literal(SageFamily.SPIRITUAL.displayName());
        };
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
     * Buy the family unlock: the mirrored rank node's level and growth-point
     * cost plus the family thread's requirement gate it. Recording spends the
     * points (the spent-mixin counts the new unlock) and the sync below
     * republishes them to the client. Returns false (with a chat reason) when
     * the unlock is already owned or a gate fails.
     */
    public static boolean purchase(ServerPlayer player, SageFamily family) {
        if (isUnlocked(player, family)) {
            player.displayClientMessage(
                    Component.translatable("greatsagegrowth.alreadyUnlocked", family.displayName())
                            .withStyle(ChatFormatting.GRAY), true);
            return false;
        }
        if (SageProgress.level(player) < minLevel(family)) {
            player.displayClientMessage(
                    Component.translatable("greatsagegrowth.needLevel", minLevel(family), family.displayName())
                            .withStyle(ChatFormatting.GRAY), true);
            return false;
        }
        if (!requirementOwned(player, family)) {
            player.displayClientMessage(
                    Component.translatable("greatsagegrowth.needPrev",
                            requirementName(family), family.displayName())
                            .withStyle(ChatFormatting.GRAY), true);
            return false;
        }
        int points = SageProgress.points(player);
        if (points < cost(family)) {
            player.displayClientMessage(
                    Component.translatable("greatsagegrowth.needPoints", cost(family), family.displayName())
                            .withStyle(ChatFormatting.GRAY), true);
            return false;
        }
        record(player, family);
        // the spent-mixin now counts this family: points drop by cost(family)
        SageProgress.sync(player, "");
        player.displayClientMessage(
                Component.translatable("greatsagegrowth.unlocked", family.displayName())
                        .withStyle(ChatFormatting.GOLD), false);
        announce(player, family);
        org.bitwisemadness.tensuragreatsagegrowth.net.GreatSageGrowthNetwork.sendState(player);
        return true;
    }
}
