package no.nav.foreldrepenger.abakus.felles.diff;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import no.nav.abakus.iaygrunnlag.kodeverk.IndexKey;
import no.nav.foreldrepenger.abakus.felles.jpa.IntervallEntitet;

/**
 * Hjelpemetoder for å raskere sette sammen en IndexKey fra flere deler.
 * Må være String, CharSequence, Number eller implementere IndexKey for hver key part.
 */
public final class IndexKeyComposer {

    private IndexKeyComposer() {
        // hidden
    }

    /**
     * Hjelpe metode for å effektivt generere keys.
     */
    public static String createKey(Object... keyParts) {
        StringBuilder sb = new StringBuilder(keyParts.length * 10);
        int max = keyParts.length;
        for (int i = 0; i < max; i++) {
            String part = toString(keyParts[i], i);
            sb.append(part);
            if (i < (max - 1)) {
                sb.append("::");
            }
        }
        return sb.toString();

    }

    private static String toString(Object obj, int i) {
        if (obj == null) {
            return "-";
        }
        var objClass = obj.getClass();
        if (CharSequence.class.isAssignableFrom(objClass)) {
            return (String) obj;
        } else if (Number.class.isAssignableFrom(objClass)) {
            return (obj).toString();
        } else if (IndexKey.class.isAssignableFrom(objClass)) {
            return ((IndexKey) obj).getIndexKey();
        } else if (IntervallEntitet.class.isAssignableFrom(objClass)) {
            IntervallEntitet periode = (IntervallEntitet) obj;
            return "[" + periode.getFomDato().format(DateTimeFormatter.ISO_DATE) +
                "," + periode.getTomDato().format(DateTimeFormatter.ISO_DATE) + "]";
        } else if (LocalDate.class.isAssignableFrom(objClass)) {
            LocalDate dt = (LocalDate) obj;
            return dt.format(DateTimeFormatter.ISO_DATE);
        } else if (LocalDateTime.class.isAssignableFrom(objClass)) {
            LocalDateTime ldt = (LocalDateTime) obj;
            return ldt.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        } else {
            throw new IllegalArgumentException("Støtter ikke å lage IndexKey for " + objClass.getName() + "[index=" + i + "], " + obj);
        }
    }
}
