package no.nav.foreldrepenger.abakus.app.diagnostikk.rapportering;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import jakarta.ws.rs.core.StreamingOutput;

import no.nav.foreldrepenger.abakus.app.diagnostikk.DumpOutput;

class ZipOutput {

    private void addToZip(ZipOutputStream zipOut, DumpOutput dump) {
        var zipEntry = new ZipEntry(dump.getPath());
        try {
            zipOut.putNextEntry(zipEntry);
            zipOut.write(dump.getContent().getBytes(StandardCharsets.UTF_8));
            zipOut.closeEntry();
        } catch (IOException e) {
            throw new IllegalStateException("Kunne ikke zippe dump fra : " + dump, e);
        }
    }

    StreamingOutput dump(List<DumpOutput> outputs) {
        return outputStream -> {
            try (var zipOut = new ZipOutputStream(new BufferedOutputStream(outputStream));) {
                outputs.forEach(dump -> addToZip(zipOut, dump));
            } finally {
                outputStream.flush();
                outputStream.close();
            }
        };
    }
}
