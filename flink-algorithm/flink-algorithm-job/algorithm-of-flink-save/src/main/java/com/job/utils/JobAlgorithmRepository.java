package com.job.utils;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface JobAlgorithmRepository extends JpaRepository<JobAlgorithm, Long> {
    Optional<JobAlgorithm> findByJobName(String jobName);

    @Query(value =
            "SELECT creator FROM al_data_clean WHERE al_short_name = ?1 " +
            "UNION " +
            "SELECT creator FROM al_knowledge_extraction WHERE al_short_name = ?1 " +
            "UNION " +
            "SELECT creator FROM al_state_evaluation WHERE model_short_name = ?1 " +
            "UNION " +
            "SELECT creator FROM al_fault_diagnosis WHERE model_short_name = ?1 " +
            "LIMIT 1",
            nativeQuery = true)
    Optional<String> findCreatorByName(String shortName);
}