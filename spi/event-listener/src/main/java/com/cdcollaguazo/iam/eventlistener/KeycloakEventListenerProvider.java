package com.cdcollaguazo.iam.eventlistener;

import com.cdcollaguazo.iam.eventlistener.client.BSNClient;
import org.keycloak.events.Event;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.EventType;
import org.keycloak.events.admin.AdminEvent;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.UserModel;

public class KeycloakEventListenerProvider implements EventListenerProvider {

    private final KeycloakSession session;

    public KeycloakEventListenerProvider( KeycloakSession session ) {
        this.session = session;
    }

    @Override
    public void onEvent( Event event ) {
        if ( event.getType() == EventType.REGISTER || event.getType() == EventType.UPDATE_PROFILE ) {
            UserModel user = session.users().getUserById( session.getContext().getRealm(), event.getUserId() );
            BSNClient.saveUser( user );
        }
    }

    @Override
    public void onEvent( AdminEvent adminEvent, boolean b ) {
    }

    @Override
    public void close() {
    }

}
