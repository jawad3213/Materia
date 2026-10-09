package com.materia.backend.common.infrastructure.web;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Builds RFC 4180 CSV: fields with a comma, quote or line break are quoted, quotes doubled. Fields that a spreadsheet
 * would run as a formula (starting with = + - @) are prefixed with an apostrophe. The download starts with a UTF-8
 * byte-order mark so Excel shows accents correctly.
 */
public final class CsvWriter {

    private static final String BOM = "﻿";
    private final StringBuilder out = new StringBuilder(BOM);

    public CsvWriter row(Object... fields) {
        out.append(Arrays.stream(fields).map(CsvWriter::field).collect(Collectors.joining(","))).append("\r\n");
        return this;
    }

    /** An empty line, between sections. */
    public CsvWriter blank() {
        out.append("\r\n");
        return this;
    }

    public String content() {
        return out.toString();
    }

    public ResponseEntity<byte[]> download(String fileName) {
        return ResponseEntity.ok()
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename(fileName).build().toString())
                .body(content().getBytes(StandardCharsets.UTF_8));
    }

    static String field(Object value) {
        if (value == null) return "";
        String text = value.toString();
        if (!text.isEmpty() && "=+-@".indexOf(text.charAt(0)) >= 0 && !looksNumeric(text)) {
            text = "'" + text;
        }
        if (text.contains(",") || text.contains("\"") || text.contains("\n") || text.contains("\r")) {
            return "\"" + text.replace("\"", "\"\"") + "\"";
        }
        return text;
    }

    private static boolean looksNumeric(String text) {
        return text.matches("^[-+]?\\d+(\\.\\d+)?$");
    }
}
