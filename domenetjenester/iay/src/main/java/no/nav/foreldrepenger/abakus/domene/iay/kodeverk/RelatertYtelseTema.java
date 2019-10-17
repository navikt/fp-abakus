package no.nav.foreldrepenger.abakus.domene.iay.kodeverk;

public enum RelatertYtelseTema {

    FORELDREPENGER_TEMA("FA"),
    ENSLIG_FORSORGER_TEMA("EF"),
    SYKEPENGER_TEMA("SP"),
    PÅRØRENDE_SYKDOM_TEMA("BS"); //$NON-NLS-1$

    private String kode;

    RelatertYtelseTema(String kode) {
        this.kode = kode;
    }

    public String getKode() {
        return kode;
    }
}
