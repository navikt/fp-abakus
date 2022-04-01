package no.nav.foreldrepenger.abakus.app.jackson;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import no.nav.abakus.iaygrunnlag.kodeverk.Kodeverdi;

/**
 * Enkel serialisering av KodeverkTabell klasser, uten at disse trenger @JsonIgnore eller lignende. Deserialisering går
 * av seg selv normalt (får null for andre felter).
 */
public class KodelisteSerializer extends StdSerializer<Kodeverdi> {


    public KodelisteSerializer() {
        super(Kodeverdi.class);
    }

    @Override
    public void serialize(Kodeverdi value, JsonGenerator jgen, SerializerProvider provider) throws IOException {

        jgen.writeStartObject();

        jgen.writeStringField("kode", value.getKode());

        jgen.writeStringField("kodeverk", value.getKodeverk());

        jgen.writeEndObject();
    }

}
