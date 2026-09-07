package dev.olliesbrother.player;

import dev.olliesbrother.skill.Skill;
import dev.olliesbrother.skill.SkillInstance;
import dev.olliesbrother.skill.SkillRegistry;

import java.util.HashMap;
import java.util.Map;

public class PlayerSkillData {

    private final Map<Skill, SkillInstance> skills = new HashMap<>();

    public PlayerSkillData() {
        for (Skill skill : SkillRegistry.getAll()) {
            skills.put(skill, new SkillInstance());
        }
    }

    public SkillInstance getSkill(Skill skill) {
        return skills.computeIfAbsent(skill, ignored -> new SkillInstance());
    }

    public void setSkill(Skill skill, SkillInstance instance) {
        skills.put(skill, instance);
    }

    public Map<Skill, SkillInstance> getSkills() {
        return skills;
    }
}