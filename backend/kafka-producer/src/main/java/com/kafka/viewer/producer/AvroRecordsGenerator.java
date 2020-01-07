package com.kafka.viewer.producer;

import org.apache.avro.Schema;
import org.apache.avro.SchemaNormalization;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

/**
 */
public class AvroRecordsGenerator<T extends SpecificRecordBase> {
    private final Class<T> clazz;

    public AvroRecordsGenerator(Class<T> clazz) {
        this.clazz = clazz;
    }

    Stream<ProducerRecord<Long, T>> recordStream(Stream<T> dataStream, final String topicName)
            throws NoSuchMethodException, InvocationTargetException,
            IllegalAccessException, NoSuchAlgorithmException {

        final Method getClassSchemaMethod = clazz.getMethod("getClassSchema");
        final Schema schema = (Schema) getClassSchemaMethod.invoke(null);
        AtomicLong index = new AtomicLong();

        final byte[] fingerprint = SchemaNormalization
                .parsingFingerprint("CRC-64-AVRO", schema);

        return dataStream
                .map(record -> new ProducerRecord<>(topicName, index.getAndIncrement(), record))
                .peek(record -> record.headers().add("AVRO-SCHEMA-HASH", fingerprint));
    }
}
