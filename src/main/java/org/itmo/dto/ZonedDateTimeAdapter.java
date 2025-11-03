package org.itmo.dto; 

import jakarta.xml.bind.annotation.adapters.XmlAdapter;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class ZonedDateTimeAdapter extends XmlAdapter<String, ZonedDateTime> {

    
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

    
    @Override
    public String marshal(ZonedDateTime zonedDateTime) throws Exception {
        if (zonedDateTime == null) {
            return null;
        }
        return zonedDateTime.format(FORMATTER);
    }

    
    @Override
    public ZonedDateTime unmarshal(String string) throws Exception {
        if (string == null || string.trim().isEmpty()) {
            return null;
        }
        
        return ZonedDateTime.parse(string, FORMATTER);
    }
}