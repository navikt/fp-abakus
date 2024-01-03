package no.nav.foreldrepenger.abakus.registerdata.inntekt.sigrun.klient;

class SigrunRestConfig {
    // felles config
    static final String CONSUMER_ID = "x-consumer-id";
    static final String X_CALL_ID = "x-call-id";
    static final String NYE_HEADER_CALL_ID = "no.nav.callid";
    static final String NYE_HEADER_CONSUMER_ID = "no.nav.consumer.id";


    //api/beregnetskatt
    static final String FILTER = "BeregnetSkattPensjonsgivendeInntekt";
    static final String PATH_BS = "/api/beregnetskatt";
    static final String X_AKTØRID = "x-aktoerid";
    static final String X_FILTER = "x-filter";
    static final String X_INNTEKTSÅR = "x-inntektsaar";

    //api/v1/summertskattegrunnlag
    static final String INNTEKTSAAR = "inntektsaar";
    static final String INNTEKTSFILTER = "inntektsfilter";
    static final String PATH_SSG = "/api/v1/summertskattegrunnlag";
    static final String FILTER_SSG = "SummertSkattegrunnlagForeldrepenger";

    //api/v1/pensjonsgivendeinntektforfolketrygden
    static final String PATH_PGI_FT = "/api/v1/pensjonsgivendeinntektforfolketrygden";
}
