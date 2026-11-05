package org.bitwisemadness.tensuragreatsagegrowth;

/**
 * The three magic families this mod's growth nodes unlock in the Great Sage
 * console's analysis screen (ruling on tensura-greatsage-growth#3).
 *
 * <p>Each family carries the unlock key stored in the Great Sage addon's own
 * progression NBT (via {@code SageProgress.unlock}) and the Tensura skill
 * package its skills live in. Families are recognised by package prefix - the
 * Tensura package <em>is</em> the family - so no skill classes need to load
 * for this table to exist (unit tests run without the mod classpath).</p>
 */
public enum SageFamily {
    ASPECTUAL("FAMILY_ASPECTUAL", "aspectual", "Aspectual magic", 5),
    SPIRITUAL("FAMILY_SPIRITUAL", "spiritual", "Spiritual magic", 8),
    SUMMON("FAMILY_SUMMON", "summon", "Summoning magic", 12);

    private static final String SKILL_PACKAGE_PREFIX = "io.github.manasmods.tensura.ability.magic.";

    private final String unlockKey;
    private final String tensuraPackage;
    private final String displayName;
    private final int minLevel;

    SageFamily(String unlockKey, String tensuraPackage, String displayName, int minLevel) {
        this.unlockKey = unlockKey;
        this.tensuraPackage = tensuraPackage;
        this.displayName = displayName;
        this.minLevel = minLevel;
    }

    /** The Sage level this node requires, mirroring the addon's SageNode convention. */
    public int minLevel() {
        return minLevel;
    }

    /** The key stored in the addon's unlocked-set NBT for this family. */
    public String unlockKey() {
        return unlockKey;
    }

    /** The Tensura skill package fragment this family owns ({@code ability/magic/<package>}). */
    public String tensuraPackage() {
        return tensuraPackage;
    }

    /** Human-readable family name for messages. */
    public String displayName() {
        return displayName;
    }

    /** Whether a resolved skill instance belongs to this family. */
    public boolean ownsSkill(Object skill) {
        return skill != null && ownsSkillClassName(skill.getClass().getName());
    }

    /** Whether a fully-qualified skill class name belongs to this family's package. */
    public boolean ownsSkillClassName(String className) {
        if (className == null) {
            return false;
        }
        // Trailing dot after the package fragment keeps "summon" from matching
        // a hypothetical "summoning" package: ...magic.summon.X yes,
        // ...magic.summoning.X no.
        return className.startsWith(SKILL_PACKAGE_PREFIX + tensuraPackage + ".");
    }

    /** Resolve a family from a command argument (package fragment or display name). */
    public static SageFamily fromArgument(String arg) {
        if (arg == null) {
            return null;
        }
        String needle = arg.trim().toLowerCase(java.util.Locale.ROOT);
        for (SageFamily family : values()) {
            if (family.tensuraPackage.equals(needle)) {
                return family;
            }
            if (family.displayName.toLowerCase(java.util.Locale.ROOT).replace(' ', '_').equals(needle)) {
                return family;
            }
        }
        return null;
    }
}
