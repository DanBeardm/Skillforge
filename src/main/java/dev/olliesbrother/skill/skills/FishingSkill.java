package dev.olliesbrother.skill.skills;

import dev.olliesbrother.skill.Skill;
import net.minecraft.util.Identifier;

public final class FishingSkill extends Skill {

    public FishingSkill() {
        super(
                Identifier.of("skillforge", "fishing"),
                "Fishing"
        );
    }
}