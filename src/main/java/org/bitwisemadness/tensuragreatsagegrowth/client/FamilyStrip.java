package org.bitwisemadness.tensuragreatsagegrowth.client;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import org.bitwisemadness.tensuragreatsagegrowth.SageFamily;
import org.bitwisemadness.tensuragreatsagegrowth.TensuraGreatSageGrowth;
import org.bitwisemadness.tensuragreatsagegrowth.net.ClientFamilyUnlocks;

/**
 * Draws the growth strip inside the Sage menu (stack PR C, issue #10): the
 * addon's tree is keyed by its final SageNode enum (layout, glyphs and hubs -
 * impossible to extend), so the mod renders its own row of family entries -
 * same information, same purchase flow, our geometry (see FamilyStripLayout,
 * which holds the pure part for the unit tests).
 *
 * <p>Runtime diagnostics (issue #17): the first rendered frame logs screen
 * dims, the computed rect and every label as INFO, so an invisible strip can be
 * told apart from a strip that never draws. {@code -Dgreatsagegrowth.stripDebug=true}
 * additionally outlines the rect in magenta with a corner marker - a visible
 * proof of where (or whether) the strip paints.</p>
 */
public final class FamilyStrip {

    /** One-shot first-frame log guard (issue #17). */
    private static volatile boolean firstFrameLogged;

    /** Set from {@code -Dgreatsagegrowth.stripDebug=true} for the debug marker. */
    public static final boolean DEBUG = Boolean.getBoolean("greatsagegrowth.stripDebug");

    private FamilyStrip() {
    }

    /** Draw the strip (client render thread only). */
    public static void render(GuiGraphics gui, Font font, float screenWidth, float screenHeight) {
        float left = FamilyStripLayout.LEFT_PAD;
        float top = FamilyStripLayout.top(screenHeight);
        float bottom = top + (FamilyStripLayout.ROW_HEIGHT * SageFamily.values().length);
        if (!firstFrameLogged) {
            firstFrameLogged = true;
            StringBuilder labels = new StringBuilder();
            for (SageFamily family : SageFamily.values()) {
                if (labels.length() > 0) {
                    labels.append(" | ");
                }
                labels.append('[').append(FamilyStripLayout.label(family)).append(']');
            }
            net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
            TensuraGreatSageGrowth.LOGGER.info(
                    "growth strip first frame: screen {}x{} window-gui {}x{} rect=({},{})..({},{}) labels={}",
                    (int) screenWidth, (int) screenHeight,
                    mc.getWindow().getGuiScaledWidth(), mc.getWindow().getGuiScaledHeight(),
                    (int) left, (int) top, (int) (left + FamilyStripLayout.ROW_WIDTH), (int) bottom,
                    labels);
        }
        int row = 0;
        for (SageFamily family : SageFamily.values()) {
            float y = top + (FamilyStripLayout.ROW_HEIGHT * row);
            boolean owned = ClientFamilyUnlocks.isUnlocked(family);
            int bg = owned ? 0x66B8860D : 0x66101018;
            int fg = owned ? 0xFFFFD700 : 0xFFDDDDDD;
            gui.fill((int) left, (int) y, (int) (left + FamilyStripLayout.ROW_WIDTH),
                    (int) (y + FamilyStripLayout.ROW_HEIGHT - 2), bg);
            gui.drawString(font, Component.literal(FamilyStripLayout.label(family)),
                    (int) left + 4, (int) y + 2, fg, false);
            row++;
        }
        if (DEBUG) {
            drawDebugMarker(gui, (int) left, (int) top,
                    (int) (left + FamilyStripLayout.ROW_WIDTH), (int) bottom);
        }
    }

    /** Magenta outline + corner square: proof of where the strip paints (issue #17). */
    private static void drawDebugMarker(GuiGraphics gui, int x1, int y1, int x2, int y2) {
        int magenta = 0xFFFF00FF;
        gui.fill(x1 - 2, y1 - 2, x2 + 2, y1, magenta);
        gui.fill(x1 - 2, y2, x2 + 2, y2 + 2, magenta);
        gui.fill(x1 - 2, y1, x1, y2, magenta);
        gui.fill(x2, y1, x2 + 2, y2, magenta);
        gui.fill(x1, y1, x1 + 8, y1 + 8, magenta);
    }
}
