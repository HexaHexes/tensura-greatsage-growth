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
    void stripIsAnchoredToTheRightEdge() {
        float left = FamilyStripLayout.left(SCREEN_W);
        assertEquals(SCREEN_W - FamilyStripLayout.SIDE_PAD - FamilyStripLayout.ROW_WIDTH, left,
                "right-anchored: pad from the right edge (issue #20 - bottom-left holds the screens' hints)");
        assertTrue(left + FamilyStripLayout.ROW_WIDTH <= SCREEN_W - FamilyStripLayout.SIDE_PAD + 0.01F,
                "strip never crosses the right pad");
    }

    @Test
    void hitsLandOnTheRightRows() {
        float top = FamilyStripLayout.top(SCREEN_H);
        float left = FamilyStripLayout.left(SCREEN_W);
        FamilyStripLayout.Row first = FamilyStripLayout.hit(
                left + 4, top + 1, SCREEN_W, SCREEN_H);
        assertEquals(SageFamily.ASPECTUAL, first.family(), "the first row is aspectual");
        FamilyStripLayout.Row third = FamilyStripLayout.hit(
                left + 4, top + (FamilyStripLayout.ROW_HEIGHT * 2) + 1, SCREEN_W, SCREEN_H);
        assertEquals(SageFamily.SUMMON, third.family(), "the third row is summon");
    }

    @Test
    void outsideTheStripThereIsNoHit() {
        assertNull(FamilyStripLayout.hit(0, 0, SCREEN_W, SCREEN_H), "top-left is outside");
        assertNull(FamilyStripLayout.hit(SCREEN_W / 2, 500, SCREEN_W, SCREEN_H),
                "bottom-centre is outside the right-anchored strip");
        float below = FamilyStripLayout.top(SCREEN_H) + (FamilyStripLayout.ROW_HEIGHT * 3);
        assertNull(FamilyStripLayout.hit(FamilyStripLayout.left(SCREEN_W) + 4, below + 1, SCREEN_W, SCREEN_H),
                "below the strip is outside");
        assertNull(FamilyStripLayout.hit(SCREEN_W - 2, 500, SCREEN_W, SCREEN_H),
                "inside the right pad is outside");
    }
}
