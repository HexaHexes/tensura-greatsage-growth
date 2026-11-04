package org.bitwisemadness.tensuragreatsagegrowth.net;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import org.bitwisemadness.tensuragreatsagegrowth.FamilyUnlocks;
import org.bitwisemadness.tensuragreatsagegrowth.SageFamily;

/**
 * Payload registration and the server-side send points (stack PR A, issue #10).
 *
 * <p>Registered from the mod entry point with {@code modBus.addListener} - the
 * same annotation-free pattern the command and tick listeners use.</p>
 */
public final class GreatSageGrowthNetwork {

    private GreatSageGrowthNetwork() {
    }

    /** Mod-bus registration: the payload flows server -> client only. */
    public static void onRegisterPayloads(RegisterPayloadHandlersEvent event) {
        event.registrar("1")
                .versioned("1")
                .playToClient(FamilyUnlockPayload.TYPE, FamilyUnlockPayload.STREAM_CODEC,
                        (payload, context) -> context.enqueueWork(() -> ClientFamilyUnlocks.apply(payload)));
    }

    /** Send the player's current unlock flags (always sent - three booleans are free). */
    public static void sendState(ServerPlayer player) {
        // flags read from the player's own NBT through FamilyUnlocks
        boolean aspectual = FamilyUnlocks.isUnlocked(player, SageFamily.ASPECTUAL);
        boolean spiritual = FamilyUnlocks.isUnlocked(player, SageFamily.SPIRITUAL);
        boolean summon = FamilyUnlocks.isUnlocked(player, SageFamily.SUMMON);
        PacketDistributor.sendToPlayer(player,
                new FamilyUnlockPayload(aspectual, spiritual, summon));
    }
}
