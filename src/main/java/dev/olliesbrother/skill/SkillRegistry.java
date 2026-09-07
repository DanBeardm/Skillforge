package dev.olliesbrother.skill;

import net.minecraft.util.Identifier;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

public final class SkillRegistry {

    private static final Map<Identifier, Skill> SKILLS = new LinkedHashMap<>();

    private SkillRegistry() {
    }

    public static <T extends Skill> T register(T skill) {
        if (SKILLS.containsKey(skill.getId())) {
            throw new IllegalArgumentException(
                    "A skill with ID " + skill.getId() + " is already registered."
            );
        }

        SKILLS.put(skill.getId(), skill);
        return skill;
    }

    public static Skill get(Identifier id) {
        return SKILLS.get(id);
    }

    public static Collection<Skill> getAll() {
        return SKILLS.values();
    }
}
