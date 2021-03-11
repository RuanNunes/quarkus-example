package org.acme;

import io.smallrye.mutiny.Multi;
import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.ext.web.client.WebClient;
import org.jsoup.Jsoup;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import java.util.Map;

@Path("/")
public class GreetingResource {

    @Inject
    Vertx vertx;

    private WebClient client;

    @PostConstruct
    void initialize() {
        this.client = WebClient.create(vertx);
    }

    @Produces
    @Path("/check")
    @GET
    public Multi<Map> check(@QueryParam("url") String url) {
           return client.getAbs(url)
                   .send()
                   .map(response -> response.bodyAsString())
                   .toMulti()
                   .flatMap(
                        html -> Multi.createFrom().iterable(
                                Jsoup.parse(html).select("a[href]")
                                .eachAttr("href"))
                   ).flatMap(
                           link -> client.headAbs(link)
                                   .send()
                                   .map(response -> Map.of("url", link, "status", response.statusCode()))
                                   .toMulti()
                   );
    }
}