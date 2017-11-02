package com.nhl.link.rest.encoder.legacy;

import com.fasterxml.jackson.core.JsonGenerator;
import com.nhl.link.rest.encoder.AbstractEncoder;
import com.nhl.link.rest.encoder.Encoder;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class ISOLocalDateTimeEncoder extends AbstractEncoder {

    private static final Encoder instance = new ISOLocalDateTimeEncoder();

    public static Encoder encoder() {
        return instance;
    }

    private ISOLocalDateTimeEncoder() {
    }

    @Override
    protected boolean encodeNonNullObject(Object object, JsonGenerator out) throws IOException {
        LocalDateTime dateTime = (LocalDateTime) object;
        String formatted = dateTime.truncatedTo(ChronoUnit.SECONDS).toString();
        out.writeObject(formatted);
        return true;
    }

}

