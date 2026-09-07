package dev.olliesbrother.skill.skills;

import dev.olliesbrother.skill.Skill;
import net.minecraft.util.Identifier;

public final class ArcherySkill extends Skill {

    public ArcherySkill() {
        super(
                Identifier.of("skillforge", "archery"),
                "Archery"
        );
    }
}