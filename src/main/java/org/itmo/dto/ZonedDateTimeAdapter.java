package org.itmo.dto; // Или новый пакет, например, org.itmo.xml;

import jakarta.xml.bind.annotation.adapters.XmlAdapter;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class ZonedDateTimeAdapter extends XmlAdapter<String, ZonedDateTime> {

    // JAXB хорошо работает со стандартным ISO 8601 форматом
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

    // Преобразует ZonedDateTime в String для экспорта (маршалинга)
    @Override
    public String marshal(ZonedDateTime zonedDateTime) throws Exception {
        if (zonedDateTime == null) {
            return null;
        }
        return zonedDateTime.format(FORMATTER);
    }

    // Преобразует String в ZonedDateTime для импорта (демаршалинга)
    @Override
    public ZonedDateTime unmarshal(String string) throws Exception {
        if (string == null || string.trim().isEmpty()) {
            return null;
        }
        // Используем ZonedDateTime.parse для обработки строки ISO 8601
        return ZonedDateTime.parse(string, FORMATTER);
    }
}