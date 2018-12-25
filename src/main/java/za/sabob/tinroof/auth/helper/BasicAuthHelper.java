package za.sabob.tinroof.auth.helper;

import za.sabob.tinroof.auth.bean.HttpMethod;
import za.sabob.tinroof.auth.bean.UsernameAndPassword;

import javax.servlet.http.HttpServletRequest;
import java.util.Base64;

public class BasicAuthHelper {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BASIC_PREFIX = "Basic ";

    public static boolean isBasicAuthRequest( HttpServletRequest req ) {

        // get username and password from the Authorization header
        String authHeader = req.getHeader( AUTHORIZATION_HEADER );

        if ( authHeader != null && authHeader.startsWith( BASIC_PREFIX ) ) {
            return true;
        }
        return false;
    }

    public static boolean isFormAuthRequest( HttpServletRequest req, String loginUrl ) {

        // Only consider POST requests as Form authentication
        if ( AuthUtils.isPost( req ) ) {

            String path = AuthUtils.getResourcePath( req );
            if ( path.equals( loginUrl ) ) {
                return true;
            }

        }
        return false;
    }

    public static UsernameAndPassword getUsernameAndPassword( HttpServletRequest req ) {

        String authHeader = req.getHeader( AUTHORIZATION_HEADER );

        String base64UsernameAndPassword = authHeader.substring( BASIC_PREFIX.length() );
        byte[] decodedValue = Base64.getDecoder().decode( base64UsernameAndPassword );
        String usernameAndPassword = new String( decodedValue );

        if ( !usernameAndPassword.contains( ":" ) ) {
            return null;
        }

        String username = usernameAndPassword.substring( 0, usernameAndPassword.indexOf( ':' ) );
        String password = usernameAndPassword.substring( usernameAndPassword.indexOf( ':' ) + 1 );

        UsernameAndPassword result = new UsernameAndPassword( username, password );
        return result;
    }
}
