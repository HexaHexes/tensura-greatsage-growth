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
    }
}
