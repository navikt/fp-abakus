ALTER TABLE IAY_INNTEKTSPOST ADD COLUMN ETTERBETALING BOOLEAN DEFAULT false;
comment on column IAY_INNTEKTSPOST.ETTERBETALING is 'Forteller om inntekten er en etterbetaling. Brukes av blant annet av Arena til å markere spesialutbetalinger tilbake i tid.';
