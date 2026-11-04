package org.bitwisemadness.tensuragreatsagegrowth.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.bitwisemadness.tensuragreatsagegrowth.SageFamily;
import org.junit.jupiter.api.Test;

/** The strip's geometry and hit-testing - pure JVM by construction. */
class FamilyStripLayoutTest {

    private static final float SCREEN_W = 800.0F;
    private static final float SCREEN_H = 600.0F;

    @Test
    void topPlacesAllThreeRowsInsideTheScreen() {
        float top = FamilyStripLayout.top(SCREEN_H);
        float bottom = top + (FamilyStripLayout.ROW_HEIGHT * 3);
        assertTrue(top > 0 && bottom <= SCREEN_H, "the strip must fit on screen; top=" + top);
    }

    @Test
    void hitsLandOnTheRightRows() {
        float top = FamilyStripLayout.top(SCREEN_H);
        FamilyStripLayout.Row first = FamilyStripLayout.hit(
                FamilyStripLayout.LEFT_PAD + 4, top + 1, SCREEN_W, SCREEN_H);
        assertEquals(SageFamily.ASPECTUAL, first.family(), "the first row is aspectual");
        FamilyStripLayout.Row third = FamilyStripLayout.hit(
                FamilyStripLayout.LEFT_PAD + 4, top + (FamilyStripLayout.ROW_HEIGHT * 2) + 1, SCREEN_W, SCREEN_H);
        assertEquals(SageFamily.SUMMON, third.family(), "the third row is summon");
    }

    @Test
    void outsideTheStripThereIsNoHit() {
        assertNull(FamilyStripLayout.hit(0, 0, SCREEN_W, SCREEN_H), "top-left is outside");
        assertNull(FamilyStripLayout.hit(FamilyStripLayout.LEFT_PAD - 1, 500, SCREEN_W, SCREEN_H),
                "left of the pad is outside");
        float below = FamilyStripLayout.top(SCREEN_H) + (FamilyStripLayout.ROW_HEIGHT * 3);
        assertNull(FamilyStripLayout.hit(FamilyStripLayout.LEFT_PAD + 4, below + 1, SCREEN_W, SCREEN_H),
                "below the strip is outside");
    }
}
