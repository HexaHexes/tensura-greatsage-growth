package org.bitwisemadness.tensuragreatsagegrowth.client;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.bitwisemadness.tensuragreatsagegrowth.SageFamily;
import org.junit.jupiter.api.Test;

/** Tree-space placement of the family nodes (pure JVM by construction). */
class FamilyTreeLayoutTest {

    @Test
    void screenTransformMatchesTheAddonsPlaceFormula() {
        // place(): screen = origin + raw * (STEP * zoom); STEP = 34
        assertEquals(100.0F + (2.0F * (34.0F * 1.5F)),
                FamilyTreeLayout.screen(100.0F, 2.0F, 1.5F), 0.001F,
                "origin + raw * (34 * zoom)");
        assertEquals(50.0F, FamilyTreeLayout.screen(50.0F, 0.0F, 3.0F), 0.001F,
                "zero-raw sits at the origin");
    }

    @Test
    void trioFormsAColumnParallelToTheRankRows() {
        // RANK_RESISTANCE/EXTRA/UNIQUE occupy y = -2/-4/-6 at x = 0 - our rows match.
        assertEquals(-2.0F, FamilyTreeLayout.rawY(SageFamily.ASPECTUAL), 0.0F);
        assertEquals(-4.0F, FamilyTreeLayout.rawY(SageFamily.SPIRITUAL), 0.0F);
        assertEquals(-6.0F, FamilyTreeLayout.rawY(SageFamily.SUMMON), 0.0F);
        for (SageFamily family : SageFamily.values()) {
            assertEquals(2.0F, FamilyTreeLayout.rawX(family), 0.0F,
                    "one shared column, clear of x=0 (ranks) and x=3 (deep read)");
        }
    }

    @Test
    void rowsAreTwoUnitsApartLikeTheRankColumn() {
        float spiritual = FamilyTreeLayout.rawY(SageFamily.SPIRITUAL);
        float aspectual = FamilyTreeLayout.rawY(SageFamily.ASPECTUAL);
        assertEquals(2.0F, Math.abs(spiritual - aspectual), 0.0F,
                "two grid steps between rows, matching the addon's vertical rhythm");
    }
}
