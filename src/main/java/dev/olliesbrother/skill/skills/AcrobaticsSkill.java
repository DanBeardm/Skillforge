package dev.olliesbrother.skill.skills;

import dev.olliesbrother.skill.Skill;
import net.minecraft.util.Identifier;

public final class AcrobaticsSkill extends Skill {

    public AcrobaticsSkill() {
        super(
                Identifier.of("skillforge", "acrobatics"),
                "Acrobatics"
        );
    }
}