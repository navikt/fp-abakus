package no.nav.foreldrepenger.abakus.app.diagnostikk;

import java.util.Objects;

public class DumpOutput {

    private final String relativePath;
    private final String content;

    public DumpOutput(String relativePath, String content) {
        this.relativePath = Objects.requireNonNull(relativePath);
        this.content = Objects.requireNonNull(content);
    }

    public String getPath() {
        return relativePath;
    }

    public String getContent() {
        return content;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "<" + relativePath + ", size=" + (content == null ? "(null)" : content.length()) + ">";
    }
}
