package dev.olliesbrother.skill.skills;

import dev.olliesbrother.skill.Skill;
import net.minecraft.util.Identifier;

public final class ExcavationSkill extends Skill {

    public ExcavationSkill() {
        super(
                Identifier.of("skillforge", "excavation"),
                "Excavation"
        );
    }
}