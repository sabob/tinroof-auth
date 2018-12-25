package za.sabob.tinroof.auth.helper;

import za.sabob.tinroof.auth.bean.HttpMethod;

import javax.servlet.http.HttpServletRequest;

public class AuthUtils {

    /**
     * Return the page resource path from the request. For example:
     * <pre class="codeHtml">
     * <span class="blue">http://www.mycorp.com/banking/secure/login.htm</span>  ->  <span class="red">/secure/performBasicAuthLogin.htm</span> </pre>
     *
     * @param request the page servlet request
     * @return the page resource path from the request
     */
    public static String getResourcePath( HttpServletRequest request ) {
        // Adapted from VelocityViewServlet.handleIncludedRequest() method:

        // If we get here from RequestDispatcher.include(), getServletPath()
        // will return the original (wrong) URI requested.  The following
        // special attribute holds the correct path.  See section 8.3 of the
        // Servlet 2.3 specification.

        String path = ( String )
                request.getAttribute( "javax.servlet.include.servlet_path" );

        // Also take into account the PathInfo stated on
        // SRV.4.4 Request Path Elements.
        String info = ( String )
                request.getAttribute( "javax.servlet.include.path_info" );

        if ( path == null ) {
            path = request.getServletPath();
            info = request.getPathInfo();
        }

        if ( info != null ) {
            path += info;
        }

        //appendIfMissing( path, "/" );
        return path;
    }

    public static String appendIfMissing( String str, String suffix ) {

        if ( str == null || suffix == null || suffix.isEmpty() ) {
            return str;
        }

        if ( str.endsWith( suffix ) ) {
            return str;
        }

        if ( !str.endsWith( suffix ) ) {
            str += suffix;
        }
        return str;
    }

    public static String removeStart( String str, String remove ) {
        if ( isEmpty( str ) || isEmpty( remove ) ) {
            return str;
        }
        if ( str.startsWith( remove ) ) {
            return str.substring( remove.length() );
        }
        return str;

    }

    public static boolean isEmpty( final String str ) {
        return str == null || str.length() == 0;
    }

    public static boolean isBlank( final String str ) {
        return str == null || str.trim().length() == 0;
    }

    public static boolean isPost( HttpServletRequest req ) {
        return req.getMethod().equalsIgnoreCase( HttpMethod.POST.description() );
    }
}
