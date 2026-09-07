package dev.olliesbrother.skill.skills;

import dev.olliesbrother.skill.Skill;
import net.minecraft.util.Identifier;

public final class UnarmedSkill extends Skill {

    public UnarmedSkill() {
        super(
                Identifier.of("skillforge", "unarmed"),
                "Unarmed"
        );
    }
}