package io.github.paladijn.d2rcharviewer.resource;

import io.quarkus.vertx.web.Route;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.FileSystemAccess;
import io.vertx.ext.web.handler.StaticHandler;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class StaticFileRouter {

    @Route(path = "/images/*", methods = Route.HttpMethod.GET)
    void staticImages(RoutingContext rc) {
        StaticHandler.create(FileSystemAccess.RELATIVE, "templates/images/").handle(rc);
    }

    @Route(path = "/css/*", methods = Route.HttpMethod.GET)
    void staticCSS(RoutingContext rc) {
        StaticHandler.create(FileSystemAccess.RELATIVE, "templates/css/").handle(rc);
    }

    @Route(path = "/js/*", methods = Route.HttpMethod.GET)
    void staticJavaScript(RoutingContext rc) {
        StaticHandler.create(FileSystemAccess.RELATIVE, "templates/js/").handle(rc);
    }
}
