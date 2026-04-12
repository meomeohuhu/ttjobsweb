package com.ttjobs.backend.repository;

import com.ttjobs.backend.entity.Job;

public interface JobWithSavedCount {
    Job getJob();
    Long getSavedCount();
}
