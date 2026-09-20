package com.job.utils;

import com.job.dto.DetectionRms;
import com.job.dto.DetectionRmsDTO;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import org.apache.flink.api.connector.sink.SinkWriter;
import org.apache.flink.streaming.connectors.influxdb.sink.writer.InfluxDBSchemaSerializer;

public class DetectionRmsDTOSerializer implements InfluxDBSchemaSerializer<DetectionRmsDTO> {

    @Override
    public Point serialize(DetectionRmsDTO rmsDTO, SinkWriter.Context context) {
        final Point dataPoint = new Point("algorithm_detection_rms");
        dataPoint.time(rmsDTO.getMpTime(), WritePrecision.MS);
        dataPoint.addTag("monitor_point_id", rmsDTO.getMonitorPointId());
        dataPoint.addTag("server_id", rmsDTO.getServerId());
        DetectionRms detectionRms = rmsDTO.getMpData();
        dataPoint.addField("rms_hi", detectionRms.getRmsHi());
        dataPoint.addField("threshold", detectionRms.getThreshold());
        dataPoint.addField("anomaly_flag", detectionRms.getThreshold() == 0);
        return dataPoint;
    }
}
