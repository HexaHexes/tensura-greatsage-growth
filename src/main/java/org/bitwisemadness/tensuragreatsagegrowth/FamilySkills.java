package org.bitwisemadness.tensuragreatsagegrowth;

import io.github.manasmods.manascore.skill.api.SkillAPI;
import net.minecraft.resources.ResourceLocation;

/**
 * Resolves a Tensura skill id to its magic family.
 *
 * <p>The skill id the Great Sage's analysis screen submits is a
 * {@link ResourceLocation}; the registry lookup yields the skill instance, and
 * the family is recognised by Tensura's family base classes
 * ({@code AspectualMagic} / {@code SpiritualMagic} / {@code SummoningMagic}).
 * Ids that do not resolve (or resolve to a non-magic skill) are not this mod's
 * business and return null.</p>
 */
public final class FamilySkills {

    private FamilySkills() {
    }

    /** The family of a resolved skill id, or null when it is not a family skill. */
    public static SageFamily familyOf(ResourceLocation skillId) {
        if (skillId == null) {
            return null;
        }
        Object skill = SkillAPI.getSkillRegistry().get(skillId);
        if (skill == null) {
            return null;
        }
        for (SageFamily family : SageFamily.values()) {
            if (family.ownsSkill(skill)) {
                return family;
            }
        }
        return null;
    }

    /** The family of a raw skill id string, or null when it does not parse or resolve. */
    public static SageFamily familyOf(String rawSkillId) {
        if (rawSkillId == null) {
            return null;
        }
        ResourceLocation id = ResourceLocation.tryParse(rawSkillId.trim());
        return id == null ? null : familyOf(id);
    }
}
