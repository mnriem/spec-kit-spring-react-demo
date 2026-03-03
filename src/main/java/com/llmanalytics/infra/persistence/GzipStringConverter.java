package com.llmanalytics.infra.persistence;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

@Converter
public class GzipStringConverter implements AttributeConverter<String, byte[]> {

    @Override
    public byte[] convertToDatabaseColumn(String attribute) {
        if (attribute == null) {
            return null;
        }
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             GZIPOutputStream gzip = new GZIPOutputStream(baos)) {
            gzip.write(attribute.getBytes(StandardCharsets.UTF_8));
            gzip.finish();
            return baos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Failed to gzip-compress string", e);
        }
    }

    @Override
    public String convertToEntityAttribute(byte[] dbData) {
        if (dbData == null) {
            return null;
        }
        try (GZIPInputStream gzip = new GZIPInputStream(new ByteArrayInputStream(dbData))) {
            return new String(gzip.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("Failed to gzip-decompress bytes", e);
        }
    }
}
