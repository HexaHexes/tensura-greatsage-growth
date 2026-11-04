package org.bitwisemadness.tensuragreatsagegrowth.client;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import org.bitwisemadness.tensuragreatsagegrowth.SageFamily;
import org.bitwisemadness.tensuragreatsagegrowth.net.ClientFamilyUnlocks;

/**
 * Draws the growth strip inside the Sage menu (stack PR C, issue #10): the
 * addon's tree is keyed by its final SageNode enum (layout, glyphs, hubs -
 * impossible to extend), so the mod renders its own row of family entries -
 * same information, same purchase flow, our geometry (see FamilyStripLayout,
 * which holds the pure part for the unit tests).
 */
public final class FamilyStrip {

    private FamilyStrip() {
    }

    /** Draw the strip (client render thread only). */
    public static void render(GuiGraphics gui, Font font, float screenWidth, float screenHeight) {
        float left = FamilyStripLayout.LEFT_PAD;
        float top = FamilyStripLayout.top(screenHeight);
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
    }
}
