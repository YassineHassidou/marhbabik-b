package com.marhababik360.gateway.repository;

import com.marhababik360.gateway.model.Route;

import java.util.List;

public class RouteRepository {
    public List<Route> routes() {
        return List.of(route("/auth/", "auth", false), route("/users", "user", true), route("/categories", "category", true),
                route("/services", "catalog", true), route("/messages", "chat", true));
    }

    private Route route(String prefix, String target, boolean requiresAuth) {
        Route route = new Route();
        route.pathPrefix = prefix;
        route.targetBaseUrl = target;
        route.requiresAuth = requiresAuth;
        return route;
    }
}
