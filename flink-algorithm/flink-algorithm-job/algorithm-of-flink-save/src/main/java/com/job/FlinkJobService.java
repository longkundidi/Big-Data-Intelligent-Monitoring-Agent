package com.job;

import com.google.gson.Gson;
import com.job.utils.JobAlgorithm;
import com.job.utils.JobAlgorithmRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Optional;
/*
Spring Boot定时任务：定期从 Flink 的 REST API 获取作业信息，并将作业的相关数据保存到数据库中。
该服务通过定时任务每隔1分钟调用 Flink API，获取当前作业的状态、开始时间、持续时间等信息，并将这些信息保存到数据库。
 */
@Service
public class FlinkJobService {

    @Autowired
    private JobAlgorithmRepository jobAlgorithmRepository;

    @Value("${flink.rest.api.url}")
    private String flinkRestApiUrl;

    private final RestTemplate restTemplate = new RestTemplate(new HttpComponentsClientHttpRequestFactory());

    /**
     * * 定时任务，每隔1分钟执行一次
     * */
    @Scheduled(fixedDelay = 60_000)
    public void saveFlinkJobsToDatabase() {
        try {
            String response = restTemplate.getForObject(flinkRestApiUrl + "/jobs/overview", String.class);
            Map<String, Object> rootResponse = new Gson().fromJson(response, Map.class);

            List<Map<String, Object>> jobs = (List<Map<String, Object>>) rootResponse.get("jobs");

            for (Map<String, Object> job : jobs) {
                String name = (String) job.get("name");
                if (name.startsWith("algorithm")) {
                    Optional<JobAlgorithm> optionalJob = jobAlgorithmRepository.findByJobName(name);

                    JobAlgorithm jobAlgorithm = optionalJob.orElseGet(JobAlgorithm::new);
                    jobAlgorithm.setJobId((String) job.get("jid"));
                    jobAlgorithm.setJobName(name);

                    //job开始时间
                    Number startTimeNum = (Number) job.get("start-time");
                    LocalDateTime localDateTime = LocalDateTime.ofInstant(
                            Instant.ofEpochMilli(startTimeNum.longValue()),
                            ZoneId.systemDefault()
                    );
                    jobAlgorithm.setStartTime(localDateTime);

                    //job持续时间
                    Number durationNum = (Number) job.get("duration");
                    Duration duration = Duration.ofMillis(durationNum.longValue());
                    jobAlgorithm.setDuration(formatDuration(duration));

                    jobAlgorithm.setStatus((String) job.get("state"));

                    String al = extractAlgorithmName(name);
                    System.out.println(al);
                    jobAlgorithm.setCreator(jobAlgorithmRepository.findCreatorByName(al).orElse(null));
                    System.out.println(jobAlgorithmRepository.findCreatorByName(al).orElse(null));
                    System.out.println(jobAlgorithm.getCreator());
                    if (!optionalJob.isPresent()) {
                        if (al.equals(name)) {
                            jobAlgorithm.setInputTopic("暂无");
                            jobAlgorithm.setOutputTopic("暂无");
                        } else {
                            jobAlgorithm.setInputTopic("dc_algorithm_" + al);
                            jobAlgorithm.setOutputTopic("dc_algorithm_sink_" + al);
                        }
                        jobAlgorithmRepository.save(jobAlgorithm);
                    } else {
                        jobAlgorithmRepository.saveAndFlush(jobAlgorithm); // 更新
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String formatDuration(Duration duration) {
        long days = duration.toDays();
        long hours = duration.minusDays(days).toHours();
        long minutes = duration.minusDays(days).minusHours(hours).toMinutes();
        long seconds = duration.minusDays(days).minusHours(hours).minusMinutes(minutes).getSeconds();

        StringBuilder sb = new StringBuilder();

        if (days > 0) {
            sb.append(days).append("d ");
        }
        if (hours > 0 || sb.length() > 0) {
            sb.append(hours).append("h ");
        }
        if (minutes > 0 || sb.length() > 0) {
            sb.append(minutes).append("m ");
        }
        sb.append(seconds).append("s");

        return sb.toString().trim();
    }

    private static String extractAlgorithmName(String name) {
        int index = name.indexOf("algorithm_");
        if (index >= 0) {
            // 获取 "algorithm" 后面的内容
            return name.substring(index + "algorithm_".length()).trim();
        }else {
            return name;
        }
    }
}
