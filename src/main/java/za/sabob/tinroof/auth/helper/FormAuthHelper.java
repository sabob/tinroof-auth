package za.sabob.tinroof.auth.helper;

import za.sabob.tinroof.auth.bean.TinRoofConfig;
import za.sabob.tinroof.auth.bean.UsernameAndPassword;

import javax.servlet.http.HttpServletRequest;
import java.util.logging.Logger;

public class FormAuthHelper {

    private final static Logger LOGGER = Logger.getLogger( FormAuthHelper.class.getName() );

    private final static String PARAM_USERNAME = "username";

    private final static String PARAM_PASSWORD = "password";

    public static UsernameAndPassword getUsernameAndPassword( HttpServletRequest req ) {
        String username = req.getParameter( PARAM_USERNAME );
        String password = req.getParameter( PARAM_PASSWORD );
        UsernameAndPassword result = new UsernameAndPassword( username, password );
        return result;
    }

    public static boolean isFormAuthRequest( HttpServletRequest req, TinRoofConfig config ) {

        if ( config.isHandleFormLogin() ) {

            String loginUrl = config.getLoginUrl();

            String path = AuthUtils.getResourcePath( req );
            if ( path.equals( loginUrl ) ) {
                return true;
            }
        }

        return false;
    }
}
