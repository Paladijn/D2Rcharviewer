/**
 * Character viewer for Diablo II: Resurrected
 */
module d2rcharviewer {
    requires io.github.paladijn.d2rsavegameparser;
    requires jakarta.cdi;
    requires jakarta.inject;
    requires jakarta.ws.rs;
    requires microprofile.config.api;
    requires org.slf4j;
    requires quarkus.core;
    requires io.vertx.web;
    requires quarkus.reactive.routes;
    requires io.smallrye.mutiny.vertx.web;
    requires com.fasterxml.jackson.annotation;
}
