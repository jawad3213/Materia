package com.materia.backend.common.infrastructure.web;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/** CSV fields: quoted when needed, and never run as a spreadsheet formula. */
class CsvWriterTest {

    @Test
    @DisplayName("csv: commas, quotes and line breaks are quoted; empty values stay empty")
    void quoting() {
        assertThat(CsvWriter.field("plain")).isEqualTo("plain");
        assertThat(CsvWriter.field("a, b")).isEqualTo("\"a, b\"");
        assertThat(CsvWriter.field("say \"hi\"")).isEqualTo("\"say \"\"hi\"\"\"");
        assertThat(CsvWriter.field("line\nbreak")).isEqualTo("\"line\nbreak\"");
        assertThat(CsvWriter.field(null)).isEmpty();
    }

    @Test
    @DisplayName("csv: text starting like a formula is neutralised, numbers are not")
    void formulaInjection() {
        assertThat(CsvWriter.field("=HYPERLINK(\"x\")")).startsWith("\"'=");
        assertThat(CsvWriter.field("@cmd")).isEqualTo("'@cmd");
        assertThat(CsvWriter.field("-12.50")).isEqualTo("-12.50");
        assertThat(CsvWriter.field("+212 522")).isEqualTo("'+212 522");
    }

    @Test
    @DisplayName("csv: rows end with CRLF and the content starts with a UTF-8 byte-order mark")
    void rows() {
        String csv = new CsvWriter().row("a", 1).blank().row("é", null).content();
        assertThat(csv).isEqualTo("﻿a,1\r\n\r\né,\r\n");
    }
}
