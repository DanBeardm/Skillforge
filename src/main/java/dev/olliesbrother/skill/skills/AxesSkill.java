package dev.olliesbrother.skill.skills;

import dev.olliesbrother.skill.Skill;
import net.minecraft.util.Identifier;

public final class AxesSkill extends Skill {

    public AxesSkill() {
        super(
                Identifier.of("skillforge", "axes"),
                "Axes"
        );
    }
}