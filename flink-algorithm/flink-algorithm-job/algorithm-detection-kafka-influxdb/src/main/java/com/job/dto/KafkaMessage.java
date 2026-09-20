package com.job.dto;

import java.util.List;

public class KafkaMessage {
    public List<Property> property;
    public long time;

    @Override
    public String toString() {
        return "KafkaMessage{" +
                "property=" + property +
                ", time=" + time +
                '}';
    }
}
