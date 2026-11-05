package org.bitwisemadness.tensuragreatsagegrowth.client;

import org.bitwisemadness.tensuragreatsagegrowth.SageFamily;

/**
 * Where the three family upgrades live in the addon's growth-tree coordinate
 * space (owner ruling on issue #20: proper nodes inside the tree, parallel to
 * the rank column).
 *
 * <p>The addon lays nodes out on a grid of 34-unit steps ({@code SkillTreeView}'s
 * {@code STEP}) and maps them to screen space as
 * {@code origin + raw * (STEP * zoom)} (decompiled from {@code place()}).
 * Its rank column - Resistance, Extra, Unique, Ultimate - sits at x=0 with
 * y = -2/-4/-6/-8. Our trio takes the <b>adjacent column at x=2</b> with the
 * <b>same three rows</b> (y = -2/-4/-6): literally parallel to those rank
 * nodes, clear of the labelled hub nodes at x=-4/0/+4 and of the x=3 column.</p>
 *
 * <p>Deliberately free of Minecraft types so the unit tests can load it.</p>
 */
public final class FamilyTreeLayout {

    /** Matches SkillTreeView's STEP constant (decompiled: `ldc 34.0f`). */
    public static final float STEP = 34.0F;

    private FamilyTreeLayout() {
    }

    /** The tree-space x of a family column (all three share the column). */
    public static float rawX(SageFamily family) {
        return 2.0F;
    }

    /** The tree-space y: the same rows as RANK_RESISTANCE/EXTRA/UNIQUE. */
    public static float rawY(SageFamily family) {
        return switch (family) {
            case ASPECTUAL -> -2.0F;
            case SPIRITUAL -> -4.0F;
            case SUMMON -> -6.0F;
        };
    }

    /** Screen coordinate for a tree-space value (pure - unit tested). */
    public static float screen(float origin, float raw, float zoom) {
        return origin + (raw * (STEP * zoom));
    }
}
