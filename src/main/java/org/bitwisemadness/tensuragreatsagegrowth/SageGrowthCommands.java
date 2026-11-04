package org.bitwisemadness.tensuragreatsagegrowth;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

/**
 * The acquisition command for the growth nodes: {@code /sagegrowth unlock
 * <family>} spends the Sage's XP and records the unlock in the addon's own
 * progression NBT (ruling on tensura-greatsage-growth#3; the console's node
 * tree cannot host new nodes because its graph is a final enum - see issue #1).
 */
public final class SageGrowthCommands {

    private SageGrowthCommands() {
    }

    /** Registered from the mod entry point (the Forge bus needs no annotation). */
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        dispatcher.register(Commands.literal("sagegrowth")
                .then(Commands.literal("unlock")
                        .then(Commands.argument("family", StringArgumentType.word())
                                .executes(context -> {
                                    ServerPlayer player = context.getSource().getPlayer();
                                    if (player == null) {
                                        context.getSource().sendFailure(
                                                Component.translatable("greatsagegrowth.playersOnly"));
                                        return 0;
                                    }
                                    SageFamily family = SageFamily.fromArgument(
                                            StringArgumentType.getString(context, "family"));
                                    if (family == null) {
                                        context.getSource().sendFailure(
                                                Component.translatable("greatsagegrowth.unknownFamily"));
                                        return 0;
                                    }
                                    return FamilyUnlocks.purchase(player, family) ? 1 : 0;
                                }))));
    }
}
