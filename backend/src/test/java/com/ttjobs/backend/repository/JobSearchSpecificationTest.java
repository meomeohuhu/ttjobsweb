package com.ttjobs.backend.repository;

import com.ttjobs.backend.entity.Company;
import com.ttjobs.backend.entity.Job;
import com.ttjobs.backend.entity.Skill;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
@TestPropertySource(properties = {
        "spring.flyway.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class JobSearchSpecificationTest {

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private SkillRepository skillRepository;

    @Test
    void keywordFilter_shouldMatchTitleAndDescription() {
        Company company = createCompany("Alpha");
        Job javaJob = createJob(company, "Java Dev", "Spring Boot developer", BigDecimal.valueOf(1000), BigDecimal.valueOf(2000));
        createJob(company, "Designer", "UI/UX", BigDecimal.valueOf(500), BigDecimal.valueOf(800));

        Specification<Job> spec = Specification.where(JobSpecifications.activeJobs())
                .and(JobSpecifications.keywordLike("java"));

        List<Job> results = jobRepository.findAll(spec, PageRequest.of(0, 10)).getContent();
        assertEquals(1, results.size());
        assertEquals(javaJob.getId(), results.get(0).getId());
    }

    @Test
    void salaryRangeFilter_shouldMatchRange() {
        Company company = createCompany("Beta");
        createJob(company, "Junior", "Entry", BigDecimal.valueOf(300), BigDecimal.valueOf(600));
        Job midJob = createJob(company, "Mid", "Mid level", BigDecimal.valueOf(1000), BigDecimal.valueOf(1500));

        Specification<Job> spec = Specification.where(JobSpecifications.activeJobs())
                .and(JobSpecifications.salaryMinGte(BigDecimal.valueOf(900)))
                .and(JobSpecifications.salaryMaxLte(BigDecimal.valueOf(1600)));

        List<Job> results = jobRepository.findAll(spec, PageRequest.of(0, 10)).getContent();
        assertEquals(1, results.size());
        assertEquals(midJob.getId(), results.get(0).getId());
    }

    @Test
    void skillsFilter_shouldMatchAnySkill() {
        Company company = createCompany("Gamma");
        Skill spring = createSkill("Spring");
        Skill react = createSkill("React");

        Job backend = createJob(company, "Backend", "Java", BigDecimal.valueOf(1000), BigDecimal.valueOf(2000));
        backend.setSkills(new ArrayList<>(List.of(spring)));
        jobRepository.save(backend);

        Job frontend = createJob(company, "Frontend", "JS", BigDecimal.valueOf(900), BigDecimal.valueOf(1500));
        frontend.setSkills(new ArrayList<>(List.of(react)));
        jobRepository.save(frontend);

        Specification<Job> spec = Specification.where(JobSpecifications.activeJobs())
                .and(JobSpecifications.hasAnySkill(List.of("spring")));

        List<Job> results = jobRepository.findAll(spec, PageRequest.of(0, 10)).getContent();
        assertEquals(1, results.size());
        assertEquals(backend.getId(), results.get(0).getId());
    }

    private Company createCompany(String name) {
        Company company = new Company();
        company.setName(name);
        return companyRepository.save(company);
    }

    private Skill createSkill(String name) {
        Skill skill = new Skill();
        skill.setName(name);
        return skillRepository.save(skill);
    }

    private Job createJob(Company company, String title, String description, BigDecimal min, BigDecimal max) {
        Job job = new Job();
        job.setCompany(company);
        job.setTitle(title);
        job.setDescription(description);
        job.setSalaryMin(min);
        job.setSalaryMax(max);
        job.setCurrency("VND");
        job.setStatus("open");
        job.setPostedDate(LocalDateTime.now());
        return jobRepository.save(job);
    }
}
