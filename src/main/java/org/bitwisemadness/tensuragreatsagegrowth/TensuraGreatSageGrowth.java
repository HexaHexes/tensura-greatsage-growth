package org.bitwisemadness.tensuragreatsagegrowth;

import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;

/**
 * Template mod entry point.
 *
 * <p>The {@link #MOD_ID} matches {@code mod_id} in
 * {@code gradle.properties}.</p>
 */
@Mod(TensuraGreatSageGrowth.MOD_ID)
public final class TensuraGreatSageGrowth {

    public static final String MOD_ID = "tensura_greatsage_growth";
    public static final Logger LOGGER = LogUtils.getLogger();

    public TensuraGreatSageGrowth(IEventBus modBus) {
        LOGGER.info("Tensura: Great Sage - Growth initialized");
        NeoForge.EVENT_BUS.addListener(SageGrowthCommands::onRegisterCommands);
        NeoForge.EVENT_BUS.addListener(AnalysisCompletionListener::onPlayerTick);
        NeoForge.EVENT_BUS.addListener(TensuraGreatSageGrowth::onPlayerLoggedIn);
        modBus.addListener(org.bitwisemadness.tensuragreatsagegrowth.net.GreatSageGrowthNetwork::onRegisterPayloads);
    }

    /** On join: hand the client its unlock flags so the screens can render. */
    private static void onPlayerLoggedIn(net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof net.minecraft.server.level.ServerPlayer player) {
            org.bitwisemadness.tensuragreatsagegrowth.net.GreatSageGrowthNetwork.sendState(player);
        }
    }
}
