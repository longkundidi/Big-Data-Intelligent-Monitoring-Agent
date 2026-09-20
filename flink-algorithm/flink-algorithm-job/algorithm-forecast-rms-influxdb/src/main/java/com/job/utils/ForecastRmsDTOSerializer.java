package com.job.utils;

import com.job.dto.ForecastRms;
import com.job.dto.ForecastRmsDTO;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import org.apache.flink.api.connector.sink.SinkWriter;
import org.apache.flink.streaming.connectors.influxdb.sink.writer.InfluxDBSchemaSerializer;

public class ForecastRmsDTOSerializer implements InfluxDBSchemaSerializer<ForecastRmsDTO> {

    @Override
    public Point serialize(ForecastRmsDTO rmsDTO, SinkWriter.Context context) {
        final Point dataPoint = new Point("algorithm_forecast_rms");
        dataPoint.time(rmsDTO.getMpTime(), WritePrecision.MS);
        dataPoint.addTag("monitor_point_id", rmsDTO.getMonitorPointId());
        dataPoint.addTag("server_id", rmsDTO.getServerId());
        ForecastRms forecastRms = rmsDTO.getMpData();
        dataPoint.addField("rms_hi", forecastRms.getRmsHi());
        dataPoint.addField("threshold", forecastRms.getThreshold());
        dataPoint.addField("anomaly_flag", forecastRms.getThreshold() == 0);
        return dataPoint;
    }
}
