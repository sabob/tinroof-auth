package za.sabob.tinroof.auth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import za.sabob.tinroof.auth.bean.SecurityConstraint;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Protects the performBasicAuthLogin and authentication mechanism for REOS API. The filter processes all incoming requests but only handle requests which path' are
 * included. Note: excluded paths will override included paths.
 */
//@WebFilter( filterName = "tinRoofAuthFilter", urlPatterns = "/*" )
public abstract class AuthFilter implements Filter {

    protected ServletContext servletContext;

    protected TinRoofAuthManager tinRoofAuthManager;

    public abstract TinRoofAuthManager config( FilterConfig config );

    public void init( FilterConfig config ) throws ServletException {
        servletContext = config.getServletContext();
        tinRoofAuthManager = config( config );

        if ( tinRoofAuthManager == null ) {
            throw new IllegalStateException( "TunRoofAuthManager cannot be null" );
        }
    }

    public void destroy() {
    }

    public void doFilter( ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain ) throws IOException, ServletException {

        HttpServletRequest req = ( HttpServletRequest ) servletRequest;
        HttpServletResponse resp = ( HttpServletResponse ) servletResponse;

        tinRoofAuthManager.handleRequest( req, resp, chain );

    }


}

