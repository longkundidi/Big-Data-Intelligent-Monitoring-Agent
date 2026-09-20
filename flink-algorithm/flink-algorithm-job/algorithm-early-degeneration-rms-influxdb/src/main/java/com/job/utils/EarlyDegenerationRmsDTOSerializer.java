package com.job.utils;

import com.job.dto.EarlyDegenerationRms;
import com.job.dto.EarlyDegenerationRmsDTO;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import org.apache.flink.api.connector.sink.SinkWriter;
import org.apache.flink.streaming.connectors.influxdb.sink.writer.InfluxDBSchemaSerializer;

public class EarlyDegenerationRmsDTOSerializer implements InfluxDBSchemaSerializer<EarlyDegenerationRmsDTO> {

    @Override
    public Point serialize(EarlyDegenerationRmsDTO rmsDTO, SinkWriter.Context context) {
        final Point dataPoint = new Point("algorithm_early_degeneration_rms");
        dataPoint.time(rmsDTO.getMpTime(), WritePrecision.MS);
        dataPoint.addTag("monitor_point_id", rmsDTO.getMonitorPointId());
        dataPoint.addTag("server_id", rmsDTO.getServerId());
        EarlyDegenerationRms earlyDegenerationRms = rmsDTO.getMpData();
        dataPoint.addField("rms_hi", earlyDegenerationRms.getRmsHi());
        dataPoint.addField("threshold", earlyDegenerationRms.getThreshold());
        dataPoint.addField("anomaly_flag", earlyDegenerationRms.getThreshold() == 0);
        return dataPoint;
    }
}
