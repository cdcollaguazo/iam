package com.cdcollaguazo.iam.eventlistener;

import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.EventListenerProviderFactory;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;

public class KeycloakListenerProviderFactory implements EventListenerProviderFactory {
    @Override
    public EventListenerProvider create( KeycloakSession keycloakSession ) {
        return new KeycloakEventListenerProvider( keycloakSession );
    }

    @Override
    public void init( org.keycloak.Config.Scope scope ) {

    }

    @Override
    public void postInit( KeycloakSessionFactory keycloakSessionFactory ) {

    }

    @Override
    public void close() {

    }

    @Override
    public String getId() {
        return "keycloak_event_listener_provider";
    }
}
