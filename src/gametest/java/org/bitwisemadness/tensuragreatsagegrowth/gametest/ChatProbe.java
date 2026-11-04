package org.bitwisemadness.tensuragreatsagegrowth.gametest;

import java.lang.reflect.Field;
import java.util.Queue;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;
import net.minecraft.server.level.ServerPlayer;

/**
 * Reads back what the Sage said to a gametest mock: MockPlayers' connection is
 * an EmbeddedChannel, so every displayClientMessage lands in its outbound queue
 * as a system-chat packet. Diagnostic aid for gate assertions; the fields are
 * protected/private, hence reflection - permitted here because the dev runtime
 * loads NeoForge from the classpath (the same technique MockPlayers uses).
 */
public final class ChatProbe {

    private ChatProbe() {
    }

    /** The newest system-chat text sent to this player, or a queue census if none. */
    public static String lastSystemChat(ServerPlayer player) {
        try {
            Field connField = null;
            for (Class<?> type = player.connection.getClass(); type != null; type = type.getSuperclass()) {
                try {
                    connField = type.getDeclaredField("connection");
                    break;
                } catch (NoSuchFieldException ignored) {
                    // keep walking up: the connection field lives on a superclass
                }
            }
            if (connField == null) {
                return "no-connection-field";
            }
            connField.setAccessible(true);
            Connection connection = (Connection) connField.get(player.connection);
            Field channelField = Connection.class.getDeclaredField("channel");
            channelField.setAccessible(true);
            Object channel = channelField.get(connection);
            if (!(channel instanceof io.netty.channel.embedded.EmbeddedChannel embedded)) {
                return "not-embedded: " + channel;
            }
            String newest = null;
            StringBuilder kinds = new StringBuilder();
            Queue<Object> outbound = embedded.outboundMessages();
            for (Object msg : outbound) {
                kinds.append(msg.getClass().getSimpleName()).append(',');
                if (msg instanceof ClientboundSystemChatPacket chat) {
                    newest = chat.content().getString();
                }
            }
            return newest != null ? newest : ("queue=" + kinds);
        } catch (ReflectiveOperationException | RuntimeException e) {
            return "probe-failed: " + e;
        }
    }
}
