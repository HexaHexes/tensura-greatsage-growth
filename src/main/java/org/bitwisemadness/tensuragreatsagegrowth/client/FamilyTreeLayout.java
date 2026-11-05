package org.bitwisemadness.tensuragreatsagegrowth.client;

import org.bitwisemadness.tensuragreatsagegrowth.SageFamily;

/**
 * Where the three family upgrades live in the addon's growth-tree coordinate
 * space (owner rulings on issue #20: proper nodes inside the tree; the branch
 * <b>starts from the Resistance node</b>; name/description/requirements live
 * in a hover tooltip like the base mod's nodes).
 *
 * <p>The addon lays nodes out on a grid of 34-unit steps ({@code SkillTreeView}'s
 * {@code STEP}) and maps them to screen space as
 * {@code origin + raw * (STEP * zoom)} (decompiled from {@code place()}).
 * The rank column - Resistance, Extra, Unique, Ultimate - sits at x=0 with
 * y = -2/-4/-6/-8. Our trio takes the <b>adjacent column at x=1</b> on the
 * <b>same three rows</b> (y = -2/-4/-6), with the connecting thread starting
 * at the Resistance node itself.</p>
 *
 * <p>Deliberately free of Minecraft types so the unit tests can load it.</p>
 */
public final class FamilyTreeLayout {

    /** Matches SkillTreeView's STEP constant (decompiled: `ldc 34.0f`). */
    public static final float STEP = 34.0F;

    /** RANK_RESISTANCE's tree position - where our thread starts. */
    public static final float ANCHOR_X = 0.0F;
    public static final float ANCHOR_Y = -2.0F;

    private FamilyTreeLayout() {
    }

    /** The tree-space x of the family column (all three share it; moved one
     * cell right of the first draft per the playtest - less clutter). */
    public static float rawX(SageFamily family) {
        return 2.0F;
    }

    /** The tree-space y: aligned with RANK_EXTRA (-4), RANK_UNIQUE (-6) and RANK_ULTIMATE
     * (-8), per the playtest ruling. */
    public static float rawY(SageFamily family) {
        return switch (family) {
            case ASPECTUAL -> -4.0F;
            case SPIRITUAL -> -6.0F;
            case SUMMON -> -8.0F;
        };
    }

    /** Screen coordinate for a tree-space value (pure - unit tested). */
    public static float screen(float origin, float raw, float zoom) {
        return origin + (raw * (STEP * zoom));
    }
}
