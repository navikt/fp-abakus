package no.nav.foreldrepenger.abakus.app.diagnostikk;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.StreamingOutput;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.abakus.iaygrunnlag.kodeverk.YtelseType;
import no.nav.foreldrepenger.abakus.kobling.kontroll.YtelseTypeRef;
import no.nav.foreldrepenger.abakus.typer.Saksnummer;

@ApplicationScoped
public class DebugDumpsters {

    private static final Logger LOG = LoggerFactory.getLogger(DebugDumpsters.class);

    private @Any Instance<DebugDump> dumpere;

    protected DebugDumpsters() {
        //
    }

    @Inject
    public DebugDumpsters(@Any Instance<DebugDump> dumpere) {
        this.dumpere = dumpere;
    }

    public StreamingOutput dumper(DumpKontekst kobling) {
        var ytelseType = kobling.getYtelseType();
        var saksnummer = kobling.getSaksnummer();
        return outputStream -> {
            try (var zipOut = new ZipOutputStream(new BufferedOutputStream(outputStream));) {
                var dumpsters = findDumpsters(ytelseType);
                var allDumps = dumpOutput(kobling, dumpsters);
                allDumps.forEach(dump -> addToZip(saksnummer, zipOut, dump));
            } finally {
                outputStream.flush();
                outputStream.close();
            }
        };

    }

    private List<Instance<DebugDump>> findDumpsters(YtelseType ytelseType) {
        return YtelseTypeRef.Lookup.list(DebugDump.class, dumpere, ytelseType);
    }

    private void addToZip(Saksnummer saksnummer, ZipOutputStream zipOut, DumpOutput dump) {
        var zipEntry = new ZipEntry(saksnummer.getVerdi() + "/" + dump.getPath());
        try {
            zipOut.putNextEntry(zipEntry);
            zipOut.write(dump.getContent().getBytes(StandardCharsets.UTF_8));
            zipOut.closeEntry();
        } catch (IOException e) {
            throw new IllegalStateException("Kunne ikke zippe dump fra : " + dump, e);
        }
    }

    private List<DumpOutput> dumpOutput(DumpKontekst kobling, List<Instance<DebugDump>> dumpsters) {
        var dumpers = dumpsters.stream().flatMap(Instance::stream).toList();
        var dumperNames = dumpers.stream().map(d -> d.getClass().getName()).toList();
        LOG.info("Dumper fra: {}", dumperNames);

        return dumpers.stream().flatMap(ddp -> {
            try {
                return ddp.dump(kobling).stream();
            } catch (Exception e) {
                var sw = new StringWriter();
                var pw = new PrintWriter(sw);
                e.printStackTrace(pw);
                return Stream.of(new DumpOutput(ddp.getClass().getSimpleName() + "-ERROR.txt", sw.toString()));
            }
        }).toList();
    }

}
