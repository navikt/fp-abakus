package no.nav.foreldrepenger.abakus.registerdata.inntekt.sigrun.klient.summertskattegrunnlag;

import java.util.List;

public record SSGResponse(List<SSGGrunnlag> grunnlag,  List<SSGGrunnlag> svalbardGrunnlag, String skatteoppgjoersdato) {
}
