package dev.olliesbrother.skill.skills;

import dev.olliesbrother.skill.Skill;
import net.minecraft.util.Identifier;

public final class WoodcuttingSkill extends Skill {

    public WoodcuttingSkill() {
        super(
                Identifier.of("skillforge", "woodcutting"),
                "Woodcutting"
        );
    }
}