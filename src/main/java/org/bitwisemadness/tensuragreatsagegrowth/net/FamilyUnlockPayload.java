package org.bitwisemadness.tensuragreatsagegrowth.net;

import java.util.Collection;
import java.util.EnumSet;
import java.util.Set;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.bitwisemadness.tensuragreatsagegrowth.SageFamily;
import org.bitwisemadness.tensuragreatsagegrowth.TensuraGreatSageGrowth;

/**
 * Server -> client sync of the three family unlock flags (stack PR A, issue
 * #10): the screens live on the client, but the unlock state is server NBT, so
 * the flags ride their own payload on login and right after every purchase.
 *
 * <p>Three booleans rather than raw strings: the client only ever needs the
 * flags, and the family enum is this mod's own constant set on both sides.</p>
 */
public record FamilyUnlockPayload(boolean aspectual, boolean spiritual, boolean summon)
        implements CustomPacketPayload {

    public static final Type<FamilyUnlockPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(TensuraGreatSageGrowth.MOD_ID, "family_unlocks"));

    public static final StreamCodec<net.minecraft.network.FriendlyByteBuf, FamilyUnlockPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.BOOL, FamilyUnlockPayload::aspectual,
                    ByteBufCodecs.BOOL, FamilyUnlockPayload::spiritual,
                    ByteBufCodecs.BOOL, FamilyUnlockPayload::summon,
                    FamilyUnlockPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    /** The payload mirroring the given unlock keys (pure - unit tested). */
    public static FamilyUnlockPayload of(Collection<String> unlockedKeys) {
        Set<String> keys = unlockedKeys instanceof Set<String> set ? set : Set.copyOf(unlockedKeys);
        return new FamilyUnlockPayload(
                keys.contains(SageFamily.ASPECTUAL.unlockKey()),
                keys.contains(SageFamily.SPIRITUAL.unlockKey()),
                keys.contains(SageFamily.SUMMON.unlockKey()));
    }

    /** Which of the three families this payload marks as unlocked (pure - unit tested). */
    public EnumSet<SageFamily> families() {
        EnumSet<SageFamily> set = EnumSet.noneOf(SageFamily.class);
        if (aspectual) {
            set.add(SageFamily.ASPECTUAL);
        }
        if (spiritual) {
            set.add(SageFamily.SPIRITUAL);
        }
        if (summon) {
            set.add(SageFamily.SUMMON);
        }
        return set;
    }
}
