package org.bitwisemadness.tensuragreatsagegrowth.client;

import org.bitwisemadness.tensuragreatsagegrowth.FamilyUnlocks;
import org.bitwisemadness.tensuragreatsagegrowth.SageFamily;
import org.bitwisemadness.tensuragreatsagegrowth.net.ClientFamilyUnlocks;

/**
 * The geometry and state of the mod's growth strip (stack PR C, issue #10) -
 * deliberately free of Minecraft types so the pure unit tests can load it
 * (the template's rule: unit tests are pure JVM, game types live in gametests).
 *
 * <p>The strip is anchored to the <b>right</b> edge (issue #20): bottom-left is
 * where the Sage screens print their own hint lines ("Drag to move - Scroll to
 * zoom..."), which the strip used to cover.</p>
 */
public final class FamilyStripLayout {

    public static final float ROW_HEIGHT = 12.0F;
    public static final float SIDE_PAD = 8.0F;
    public static final float BOTTOM_PAD = 8.0F;
    public static final float ROW_WIDTH = 150.0F;

    private FamilyStripLayout() {
    }

    /** One strip entry: family, label, state. */
    public record Row(SageFamily family, String label, Kind kind) {
        public enum Kind {
            OWNED, BUYABLE
        }
    }

    /** The label for a family in its current (client-known) state. */
    public static String label(SageFamily family) {
        if (ClientFamilyUnlocks.isUnlocked(family)) {
            return family.displayName() + " - owned";
        }
        return family.displayName() + " - Lv " + family.minLevel() + " + " + FamilyUnlocks.XP_COST + " XP";
    }

    /** The top edge of the strip in screen coordinates (pure - unit tested). */
    public static float top(float screenHeight) {
        return screenHeight - BOTTOM_PAD - (ROW_HEIGHT * SageFamily.values().length);
    }

    /** The left edge of the right-anchored strip (pure - unit tested). */
    public static float left(float screenWidth) {
        return screenWidth - SIDE_PAD - ROW_WIDTH;
    }

    /** The row under a mouse position, or null (pure - unit tested). */
    public static Row hit(float mouseX, float mouseY, float screenWidth, float screenHeight) {
        float left = left(screenWidth);
        float stripTop = top(screenHeight);
        if (mouseX < left || mouseX > left + ROW_WIDTH || mouseY < stripTop) {
            return null;
        }
        int index = (int) Math.floor((mouseY - stripTop) / ROW_HEIGHT);
        SageFamily[] families = SageFamily.values();
        if (index < 0 || index >= families.length) {
            return null;
        }
        SageFamily family = families[index];
        Row.Kind kind = ClientFamilyUnlocks.isUnlocked(family) ? Row.Kind.OWNED : Row.Kind.BUYABLE;
        return new Row(family, label(family), kind);
    }
}
