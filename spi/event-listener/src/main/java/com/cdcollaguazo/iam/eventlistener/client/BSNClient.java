package com.cdcollaguazo.iam.eventlistener.client;

import com.cdcollaguazo.iam.eventlistener.model.UserRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.keycloak.models.UserModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class BSNClient {

    private static final String BSN_USERS_API_URL = System.getenv( "BSN_USERS_API_URL" );
    private static final HttpClient client = HttpClient.newHttpClient();
    private static final Logger log = LoggerFactory.getLogger( BSNClient.class );
    private static final ObjectMapper mapper = new ObjectMapper();

    public static void saveUser( UserModel user ) {

        if ( BSN_USERS_API_URL == null ) {
            log.error( "No bsn users api url set" );
            return;
        }

        UserRequest userRequest = new UserRequest(
                user.getId(), user.getEmail(), user.getFirstName(), user.getLastName()
        );

        try {
            String jsonUser = mapper.writeValueAsString( userRequest );
            HttpRequest request = HttpRequest.newBuilder()
                    .uri( URI.create( BSN_USERS_API_URL ) )
                    .header( "Content-Type", "application/json" )
                    .POST( HttpRequest.BodyPublishers.ofString( jsonUser ) )
                    .build();

            HttpResponse<String> response = client.send( request, HttpResponse.BodyHandlers.ofString() );

            if ( response.statusCode() != 201 ) {
                log.warn( "Failed to send user. Status code {}, Body {}", response.statusCode(), response.body() );
                return;
            }

            log.info( "Successfully send user" );
        } catch ( Exception e ) {
                log.error( "An error occurred while sending the user request", e );
        }
    }

}
