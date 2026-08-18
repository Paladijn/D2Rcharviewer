/**
 * Character viewer for Diablo II: Resurrected
 */
module d2rcharviewer {
    requires io.github.paladijn.d2rsavegameparser;
    requires jakarta.ws.rs;
    requires org.slf4j;
    requires io.vertx.web;
    requires quarkus.reactive.routes;
    requires io.smallrye.mutiny.vertx.web;
    requires com.fasterxml.jackson.annotation;
    requires com.fasterxml.jackson.databind;
    requires java.net.http;
    requires jakarta.cdi;
    requires io.quarkus.core;
    requires org.eclipse.microprofile.config;

    exports io.github.paladijn.d2rcharviewer.model.diablorun to com.fasterxml.jackson.databind;
    exports io.github.paladijn.d2rcharviewer.model.translation to com.fasterxml.jackson.databind;
}
