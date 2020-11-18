package no.nav.foreldrepenger.abakus.jetty;

public class JettyDevDbKonfigurasjon {
    private String datasource = "defaultDS";
    private String user = "fpabakus";
    private String url = "jdbc:postgresql://127.0.0.1:5432/fpabakus?reWriteBatchedInserts=true";
    private String password = user;

    JettyDevDbKonfigurasjon() {
    }

    public String getDatasource() {
        return datasource;
    }

    public String getUrl() {
        return url;
    }

    public String getUser() {
        return user;
    }

    public String getPassword() {
        return password;
    }

}


