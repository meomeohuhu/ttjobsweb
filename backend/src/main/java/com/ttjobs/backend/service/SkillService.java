package com.ttjobs.backend.service;

import com.ttjobs.backend.dto.CreateSkillRequest;
import com.ttjobs.backend.dto.SkillDTO;
import com.ttjobs.backend.entity.Skill;
import com.ttjobs.backend.repository.SkillRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@Service
public class SkillService {

    @Autowired
    private SkillRepository skillRepository;

    public List<SkillDTO> searchSkills(String query) {
        String normalized = query == null ? "" : query.trim();
        List<Skill> skills = normalized.isBlank()
                ? skillRepository.findAll().stream()
                        .sorted(Comparator.comparing(Skill::getName, String.CASE_INSENSITIVE_ORDER))
                        .limit(20)
                        .toList()
                : skillRepository.findTop20ByNameContainingIgnoreCaseOrderByNameAsc(normalized);
        return skills.stream().map(this::toDto).toList();
    }

    @Transactional
    public SkillDTO createSkill(CreateSkillRequest request) {
        String name = toTitleCase(request.getName());
        Skill skill = skillRepository.findByNameIgnoreCase(name)
                .orElseGet(() -> {
                    Skill newSkill = new Skill();
                    newSkill.setName(name);
                    return skillRepository.save(newSkill);
                });
        return toDto(skill);
    }

    private SkillDTO toDto(Skill skill) {
        SkillDTO dto = new SkillDTO();
        dto.setId(skill.getId());
        dto.setName(skill.getName());
        return dto;
    }

    private String toTitleCase(String input) {
        String[] words = input.trim().toLowerCase(Locale.ROOT).split("\\s+");
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < words.length; i++) {
            if (words[i].isBlank()) {
                continue;
            }
            builder.append(Character.toUpperCase(words[i].charAt(0)));
            if (words[i].length() > 1) {
                builder.append(words[i].substring(1));
            }
            if (i < words.length - 1) {
                builder.append(' ');
            }
        }
        return builder.toString();
    }
}
