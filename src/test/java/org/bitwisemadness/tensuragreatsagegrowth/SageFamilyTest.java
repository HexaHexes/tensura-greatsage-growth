package org.bitwisemadness.tensuragreatsagegrowth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

/** The family table: unlock keys, package fragments and argument parsing. */
class SageFamilyTest {

    @Test
    void unlockKeysAreUnique() {
        Set<String> keys = new HashSet<>();
        for (SageFamily family : SageFamily.values()) {
            assertTrue(keys.add(family.unlockKey()), "duplicate unlock key: " + family.unlockKey());
        }
        assertEquals(3, keys.size());
    }

    @Test
    void argumentsResolveByPackageFragment() {
        for (SageFamily family : SageFamily.values()) {
            assertEquals(family, SageFamily.fromArgument(family.tensuraPackage()));
            assertEquals(family, SageFamily.fromArgument(family.tensuraPackage().toUpperCase(java.util.Locale.ROOT)));
        }
        assertEquals(SageFamily.ASPECTUAL, SageFamily.fromArgument(" aspectual "));
    }

    @Test
    void unknownAndNullArgumentsResolveToNothing() {
        assertNull(SageFamily.fromArgument(null));
        assertNull(SageFamily.fromArgument(""));
        assertNull(SageFamily.fromArgument("elemental"));
    }

    @Test
    void familiesCarryDistinctPackages() {
        assertNotEquals(SageFamily.ASPECTUAL.tensuraPackage(), SageFamily.SPIRITUAL.tensuraPackage());
        assertNotEquals(SageFamily.SPIRITUAL.tensuraPackage(), SageFamily.SUMMON.tensuraPackage());
        assertNotEquals(SageFamily.ASPECTUAL.tensuraPackage(), SageFamily.SUMMON.tensuraPackage());
    }

    @Test
    void familyRecognitionIsPackageBased() {
        String prefix = "io.github.manasmods.tensura.ability.magic.";
        assertTrue(SageFamily.ASPECTUAL.ownsSkillClassName(prefix + "aspectual.fire.FireMagic"));
        assertTrue(SageFamily.SPIRITUAL.ownsSkillClassName(prefix + "spiritual.light.LightMagic"));
        assertTrue(SageFamily.SUMMON.ownsSkillClassName(prefix + "summon.SummonBasiliskMagic"));
        // a trailing dot keeps "summon" from matching a "summoning" package
        assertFalse(SageFamily.SUMMON.ownsSkillClassName(prefix + "summoning.SummonMagic"));
        // non-magic skills and nulls belong to no family
        assertFalse(SageFamily.ASPECTUAL.ownsSkillClassName("io.github.manasmods.tensura.ability.skill.SwordSkill"));
        assertFalse(SageFamily.ASPECTUAL.ownsSkillClassName(null));
        assertFalse(SageFamily.ASPECTUAL.ownsSkill(null));
    }
}
