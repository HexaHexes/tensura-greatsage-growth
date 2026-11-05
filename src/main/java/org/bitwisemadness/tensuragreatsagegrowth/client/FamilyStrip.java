package org.bitwisemadness.tensuragreatsagegrowth.client;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import org.bitwisemadness.tensuragreatsagegrowth.SageFamily;
import org.bitwisemadness.tensuragreatsagegrowth.TensuraGreatSageGrowth;
import org.bitwisemadness.tensuragreatsagegrowth.net.ClientFamilyUnlocks;

/**
 * Draws the growth strip inside the Sage screens (stack PR C, issue #10): the
 * addon's node tree cannot be extended (its layout maps are keyed by the final
 * {@code SageNode} enum), so the mod renders its own row of family entries -
 * same information, same purchase flow, our geometry (see FamilyStripLayout,
 * which holds the pure part for the unit tests).
 *
 * <p>Visibility pass (issue #20, from the alpha.10 playtest screenshots):
 * anchored to the bottom-<b>right</b> edge so it never covers the screens'
 * bottom-left hint lines, and the palette reads on any backdrop - dark chips
 * with bright text (the old owned style drew gold text on a gold fill), with a
 * gold accent bar marking owned rows.</p>
 *
 * <p>Runtime diagnostics (issue #17): the first rendered frame logs screen
 * dims, the computed rect and every label as INFO, so an invisible strip can be
 * told apart from a strip that never draws.</p>
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
        float left = FamilyStripLayout.left(screenWidth);
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
            int bg = owned ? 0xD01C1405 : 0xC017171D;
            int fg = owned ? 0xFFFFD700 : 0xFFE8E8E8;
            gui.fill((int) left, (int) y, (int) (left + FamilyStripLayout.ROW_WIDTH),
                    (int) (y + FamilyStripLayout.ROW_HEIGHT - 2), bg);
            if (owned) {
                gui.fill((int) left, (int) y, (int) left + 2,
                        (int) (y + FamilyStripLayout.ROW_HEIGHT - 2), 0xFFFFD700);
            }
            gui.drawString(font, Component.literal(FamilyStripLayout.label(family)),
                    (int) left + 6, (int) y + 2, fg, false);
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
