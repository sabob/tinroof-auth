package za.sabob.tinroof.auth.bean;

import java.util.Optional;

public enum HttpMethod {

    GET( "GET" ),
    POST( "POST" ),
    PUT( "PUT" ),
    DELETE( "DELETE" ),
    PATCH( "PATCH" ),
    OPTIONS( "OPTIONS " ),
    HEAD( "HEAD" ),
    TRACE( "TRACE " );

    private String description;

    HttpMethod( String description ) {
        this.description = description;
    }

    public String description() {
        return description;
    }

    public static Optional<HttpMethod> lookup( String value ) {

        try {
            HttpMethod method = valueOf( value );
            return Optional.ofNullable( method );

        } catch ( Exception e ) {
            return Optional.ofNullable( null );
        }
    }
}
