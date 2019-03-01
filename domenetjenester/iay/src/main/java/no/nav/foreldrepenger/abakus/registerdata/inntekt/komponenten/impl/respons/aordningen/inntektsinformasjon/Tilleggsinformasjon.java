package no.nav.foreldrepenger.abakus.registerdata.inntekt.komponenten.impl.respons.aordningen.inntektsinformasjon;


import no.nav.foreldrepenger.abakus.registerdata.inntekt.komponenten.impl.respons.aordningen.inntektsinformasjon.tilleggsinformasjondetaljer.TilleggsinformasjonDetaljer;

public class Tilleggsinformasjon {
    private String kategori;
    private TilleggsinformasjonDetaljer tilleggsinformasjonDetaljer;


    public Tilleggsinformasjon() {
    }

    public Tilleggsinformasjon(String kategori, TilleggsinformasjonDetaljer tilleggsinformasjonDetaljer) {
        this.kategori = kategori;
        this.tilleggsinformasjonDetaljer = tilleggsinformasjonDetaljer;
    }

    public String getKategori() {
        return this.kategori;
    }

    public void setKategori(String kategori) {
        this.kategori = kategori;
    }

    public TilleggsinformasjonDetaljer getTilleggsinformasjonDetaljer() {
        return this.tilleggsinformasjonDetaljer;
    }

    public void setTilleggsinformasjonDetaljer(TilleggsinformasjonDetaljer tilleggsinformasjonDetaljer) {
        this.tilleggsinformasjonDetaljer = tilleggsinformasjonDetaljer;
    }

}
