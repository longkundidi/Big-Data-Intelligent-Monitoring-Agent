package com.job.utils;

import com.algorithm.common.model.SensorOriginalDTO;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import org.apache.flink.api.connector.sink.SinkWriter;
import org.apache.flink.streaming.connectors.influxdb.sink.writer.InfluxDBSchemaSerializer;

public class SensorOriginalDTOSerializer implements InfluxDBSchemaSerializer<SensorOriginalDTO> {

    @Override
    public Point serialize(SensorOriginalDTO sensorOriginalDTO, SinkWriter.Context context) {
        final Point dataPoint = new Point("sensor_data");
        dataPoint.time(sensorOriginalDTO.getMpTime(), WritePrecision.MS);
        dataPoint.addTag("monitor_point_id", sensorOriginalDTO.getMonitorPointId());
        switch (sensorOriginalDTO.getMpType()) {
            case "long":
                dataPoint.addField("mp_data_i", Long.parseLong(sensorOriginalDTO.getMpData()));
                break;
            case "float":
            case "double":
                dataPoint.addField("mp_data_f", Float.parseFloat(sensorOriginalDTO.getMpData()));
                break;
            case "boolean":
                dataPoint.addField("mp_data_b", "0".equals(sensorOriginalDTO.getMpData()));
                break;
            case "string":
                dataPoint.addField("mp_data_s", sensorOriginalDTO.getMpData());
                break;
            default:
                dataPoint.addField("mp_data_s", sensorOriginalDTO.getMpData());
                break;
        }

        return dataPoint;
    }
}
