package dev.olliesbrother.skill.skills;

import dev.olliesbrother.skill.Skill;
import net.minecraft.util.Identifier;

public final class HerbalismSkill extends Skill {

    public HerbalismSkill() {
        super(
                Identifier.of("skillforge", "herbalism"),
                "Herbalism"
        );
    }
}