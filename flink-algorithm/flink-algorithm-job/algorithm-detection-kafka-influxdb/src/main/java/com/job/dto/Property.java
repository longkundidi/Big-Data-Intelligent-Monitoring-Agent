package com.job.dto;

public class Property {
    public String id;
    public String key;
    public String value;

    @Override
    public String toString() {
        return "Property{" +
                "id='" + id + '\'' +
                ", key='" + key + '\'' +
                ", value='" + value + '\'' +
                '}';
    }
}
