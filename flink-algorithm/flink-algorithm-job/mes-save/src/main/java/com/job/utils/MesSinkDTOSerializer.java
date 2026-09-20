package com.job.utils;

import com.algorithm.common.model.MesSinkDTO;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import org.apache.flink.api.connector.sink.SinkWriter;
import org.apache.flink.streaming.connectors.influxdb.sink.writer.InfluxDBSchemaSerializer;

public class MesSinkDTOSerializer implements InfluxDBSchemaSerializer<MesSinkDTO> {

    @Override
    public Point serialize(MesSinkDTO mesSinkDTO, SinkWriter.Context context) {
        final Point dataPoint = new Point("mes");
        dataPoint.time(mesSinkDTO.getTime(), WritePrecision.MS);
        dataPoint.addTag("id", mesSinkDTO.getId());
        dataPoint.addField(mesSinkDTO.getKey(), mesSinkDTO.getValue());
        return dataPoint;
    }
}
