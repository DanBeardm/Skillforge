package dev.olliesbrother.skill.skills;

import dev.olliesbrother.skill.Skill;
import net.minecraft.util.Identifier;

public final class SwordsSkill extends Skill {

    public SwordsSkill() {
        super(
                Identifier.of("skillforge", "swords"),
                "Swords"
        );
    }
}