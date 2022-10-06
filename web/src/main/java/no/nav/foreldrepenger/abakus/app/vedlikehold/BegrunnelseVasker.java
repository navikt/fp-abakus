package no.nav.foreldrepenger.abakus.app.vedlikehold;

class BegrunnelseVasker {

    public static String vask(String uvasket) {
        return uvasket.replace("\n", "").replace("\r", "")
            .replace("\u00a0"," ");
    }

}
