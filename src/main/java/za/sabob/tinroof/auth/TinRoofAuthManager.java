package za.sabob.tinroof.auth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import za.sabob.tinroof.auth.bean.MatchResult;
import za.sabob.tinroof.auth.bean.SecurityConstraint;
import za.sabob.tinroof.auth.bean.TinRoofConfig;
import za.sabob.tinroof.auth.bean.UsernameAndPassword;
import za.sabob.tinroof.auth.error.AuthError;
import za.sabob.tinroof.auth.error.ErrorCodes;
import za.sabob.tinroof.auth.helper.AuthUtils;
import za.sabob.tinroof.auth.helper.BasicAuthHelper;
import za.sabob.tinroof.auth.helper.FormAuthHelper;

import javax.servlet.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

public class TinRoofAuthManager {

    protected static Logger LOGGER = LoggerFactory.getLogger( TinRoofAuthManager.class );

    protected TinRoofConfig config;

    public TinRoofAuthManager( TinRoofConfig config ) {
        this.config = config;
    }

    public void handleRequest( HttpServletRequest req, HttpServletResponse resp, FilterChain chain ) {

        ensureBasicAuthUserAndCookieUserSame( req, resp );

        MatchResult matchResult = matchToMostSpecificSecurityConstraint( req );

        if ( matchResult.isExcluded() ) {
            handleExcludedRequest( matchResult, req, resp, chain );
        }

        if ( matchResult.isIncluded() ) {

            handleIncludedRequest( matchResult, chain, req, resp );

        } else {

            handleUnmatchedRequest( matchResult, req, resp, chain );
        }
    }

    public TinRoofConfig getConfig() {
        return config;
    }

    public void setConfig( TinRoofConfig config ) {
        this.config = config;
    }

    protected boolean performBasicAuthLogin( HttpServletRequest req, HttpServletResponse resp, String username, String password ) {

        try {
            if ( isUserAlreadyAuthenticated( req ) ) {
                return true;
            }

            req.login( username, password );
            return true;

        } catch ( Exception ex ) {

            notifyBasicAuthenticationFailed( resp, username );

            return false;
        }
    }

    protected boolean performFormAuthLogin( HttpServletRequest req, HttpServletResponse resp, String username, String password ) {

        try {
            if ( isUserAlreadyAuthenticated( req ) ) {
                return true;
            }

            req.login( username, password );
            return true;

        } catch ( Exception ex ) {

            notifyFormAuthenticationFailed( resp, username );

            return false;
        }
    }


    protected boolean logout( HttpServletRequest req, HttpServletResponse resp ) {

        try {
            req.logout();
            return true;

        } catch ( Exception ex ) {

            AuthError error = new AuthError( ErrorCodes.LOGOUT_ERROR, "User '" + req.getRemoteUser() + "' could not be logged out" );
            writeError( resp, error );
            return false;
        }
    }

    protected void ensureBasicAuthUserAndCookieUserSame( HttpServletRequest req, HttpServletResponse resp ) {

        if ( isUserAlreadyAuthenticated( req ) ) {

            if ( BasicAuthHelper.isBasicAuthRequest( req ) ) {
                UsernameAndPassword usernameAndPassword = BasicAuthHelper.getUsernameAndPassword( req );

                if ( usernameAndPassword == null ) {
                    // Shouldn't happen
                    return;

                } else {
                    if ( !req.getRemoteUser().equals( usernameAndPassword.username ) ) {
                        logout( req, resp );
                    }

                }

            }
        }
    }

    protected boolean isUserAlreadyAuthenticated( HttpServletRequest req ) {

        if ( req.getRemoteUser() != null ) {
            return true;
        }

        return false;

    }

    protected boolean loginWithBasicAuthentication( HttpServletRequest req, HttpServletResponse resp ) {
        UsernameAndPassword usernameAndPassword = BasicAuthHelper.getUsernameAndPassword( req );

        if ( usernameAndPassword == null ) {
            AuthError error = new AuthError( ErrorCodes.BASIC_AUTH_INVALID_USERNAME_AND_PASSWORD_TOKEN, "Basic authentication requires a base64 encoded, readable username:password token" );
            writeError( resp, error );

            return false;
        }

        boolean success = performBasicAuthLogin( req, resp, usernameAndPassword.username, usernameAndPassword.password );

        return success;
    }

    protected boolean loginWithFormAuthentication( HttpServletRequest req, HttpServletResponse resp ) {
        UsernameAndPassword usernameAndPassword = FormAuthHelper.getUsernameAndPassword( req );

        if ( AuthUtils.isBlank( (usernameAndPassword.username) ) ) {

            AuthError error = new AuthError( ErrorCodes.USERNAME_REQUIRED, "username is required" );
            writeError( resp, error );
            return false;
        }

        if ( AuthUtils.isBlank( (usernameAndPassword.password) ) ) {
            AuthError error = new AuthError( ErrorCodes.PASSWORD_REQUIRED, "password is required" );
            writeError( resp, error );
            return false;
        }


        boolean success = performFormAuthLogin( req, resp, usernameAndPassword.username, usernameAndPassword.password );

        return success;
    }

    protected void writeError( HttpServletResponse resp, AuthError error ) {

        try {

            StringBuilder sb = new StringBuilder();
            sb.append( "{" );
            sb.append( "\"message\" : " );
            sb.append( "\"" );
            sb.append( error.getMessage() );
            sb.append( "\"" );
            sb.append( "," );
            sb.append( "\"code\" : " );
            sb.append( "\"" );
            sb.append( error.getCode() );
            sb.append( "\"" );
            sb.append( "}" );

            PrintWriter writer = resp.getWriter();
            writer.write( sb.toString() );

        } catch ( IOException e ) {
            e.printStackTrace();
        }
    }

    protected void handleIncludedRequest( MatchResult matchResult, FilterChain chain, HttpServletRequest req, HttpServletResponse resp ) {

        boolean loginSucceeded = false;

        try {

            if ( BasicAuthHelper.isBasicAuthRequest( req ) ) {

                loginSucceeded = loginWithBasicAuthentication( req, resp );

            } else if ( isFormLoginRequest( req ) ) {

                loginSucceeded = loginWithFormAuthentication( req, resp );

            } else {

                loginSucceeded = isUserAlreadyAuthenticated( req );

                if ( !loginSucceeded ) {
                    notifyAuthRequired( req, resp );
                }

            }

        } catch ( Exception error ) {
            handleLoginException( error, req );
        }

        if ( loginSucceeded ) {

            // authorize before continuing
            boolean authorized = authorize( matchResult, req );

            if ( authorized ) {
                doFilter( req, resp, chain );

            } else {

                String username = req.getRemoteUser();

                logout( req, resp );
                String path = AuthUtils.getResourcePath( req );
                notifyNotAuthorized( req, resp, path, username );
            }
        }
    }

    protected void handleExcludedRequest( MatchResult matchResult, HttpServletRequest req, HttpServletResponse resp, FilterChain chain ) {
        doFilter( req, resp, chain );
        return;
    }

    protected void handleUnmatchedRequest( MatchResult matchResult, HttpServletRequest req, HttpServletResponse resp, FilterChain chain ) {

        // This request is not targeting this auth filter, so if authenticated request goes through and let the JEE container take care of authorization
        boolean alreadyAuthenticated = isUserAlreadyAuthenticated( req );

        if ( alreadyAuthenticated ) {
            doFilter( req, resp, chain );

        } else {

            // Else redirect to index.jsp
            redirectToWelcomePage( req, resp, chain );
        }
    }

    protected void redirectToWelcomePage( HttpServletRequest req, HttpServletResponse resp, FilterChain chain ) {
        sendRedirect( req, resp, getConfig().getWelcomePage() );
    }

    protected MatchResult matchToMostSpecificSecurityConstraint( HttpServletRequest req ) {

        MatchResult finalMatchResult = new MatchResult( false, false, null, null );

        // Loop all constraints, if an exclusion is matched we exit, otherwise continue until we find an exclusion or
        // loop finishes and any of the constraints included the request
        for ( SecurityConstraint sc : getConfig().getSecurityConstraints() ) {

            if ( !sc.doesHttpMethodApply( req ) ) {
                continue;
            }

            MatchResult matchResult = sc.match( req );

            // Exit early if request is excluded
            if ( matchResult.isExcluded() ) {
                return matchResult;
            }

            // If we find a matching include path, the final included value is true
            if ( matchResult.isIncluded() ) {

                // Update the security constraint if it contained a more specific include path than the previous security constraint
                boolean included = true;
                boolean excluded = false;
                finalMatchResult = MatchResult.createNewMatchResultIfMatchedPathIsMoreSpecific( finalMatchResult, included, excluded, sc, matchResult.getMatchedPath() );
            }
        }

        return finalMatchResult;
    }

    protected void sendRedirect( HttpServletRequest req, HttpServletResponse resp, String url ) {

        try {

            String fullUrl = resp.encodeRedirectURL( req.getContextPath() + url );

            resp.sendRedirect( fullUrl );

        } catch ( IOException ex ) {
            throw new RuntimeException( ex );
        }
    }

    protected void notifyAuthRequired( HttpServletRequest req, HttpServletResponse resp ) {

        if ( getConfig().isNotifyBasicAuthRequired() ) {
            resp.setHeader( "WWW-Authenticate", "Basic" );
            resp.setStatus( 401 );
            AuthError error = new AuthError( ErrorCodes.REQUIRES_BASIC_AUTH, "This service requires basic or form authentication" );
            writeError( resp, error );

        } else {
            resp.setStatus( 401 );
            AuthError error = new AuthError( ErrorCodes.REQUIRES_BASIC_AUTH, "This service requires form or basic authentication" );
            writeError( resp, error );

        }
    }

    protected void notifyFormAuthenticationFailed( HttpServletResponse resp, String username ) {
        resp.setStatus( 401 );
        AuthError error = new AuthError( ErrorCodes.REQUIRES_FORM_AUTH, username + " could not be authenticated" );
        writeError( resp, error );
    }

    protected void notifyBasicAuthenticationFailed( HttpServletResponse resp, String username ) {
        resp.setHeader( "WWW-Authenticate", "Basic" );
        resp.setStatus( 401 );
        AuthError error = new AuthError( ErrorCodes.REQUIRES_BASIC_AUTH, username + " could not be authenticated" );
        writeError( resp, error );
    }

    protected void notifyNotAuthorized( HttpServletRequest req, HttpServletResponse resp, String path, String username ) {

        if ( getConfig().isNotifyBasicAuthRequired() ) {
            resp.setHeader( "WWW-Authenticate", "Basic" );
            resp.setStatus( 401 );
            AuthError error = new AuthError( ErrorCodes.REQUIRES_BASIC_AUTH, username + " is not authorized to access the path " + path );
            writeError( resp, error );

        } else {
            resp.setStatus( 401 );
            AuthError error = new AuthError( ErrorCodes.REQUIRES_FORM_AUTH, username + " is not authorized to access the path " + path );
            writeError( resp, error );
        }
    }

    public void doFilter( HttpServletRequest req, HttpServletResponse resp, FilterChain chain ) {

        try {
            chain.doFilter( req, resp );

        } catch ( Exception ex ) {
            throw new RuntimeException( ex );
        }
    }

    protected boolean authorize( MatchResult mr, HttpServletRequest req ) {
        SecurityConstraint sc = mr.getSecurityConstraint();
        boolean authorized = sc.authorize( req );
        return authorized;

    }

    protected String getLoggedInUser( HttpServletRequest req ) {


        if ( req != null ) { // Guard in case null. Shouldn't ever happen though.
            return req.getRemoteUser();
        }


        return "Unkonwn";
    }

    protected void handleLoginException( Exception error, HttpServletRequest req ) {
        LOGGER.debug( error.getMessage(), error );

        UsernameAndPassword usernameAndPassword = BasicAuthHelper.getUsernameAndPassword( req );

//        new EmailBuilder()
//                .user( getLoggedInUser( req ) )
//                .exception( "Error occurred while authenticating user " + usernameAndPassword.username, error )
//                .toJavaDevs()
//                .send();
    }

    protected boolean isFormLoginRequest( HttpServletRequest req ) {

        if ( getConfig().isHandleFormLogin() && BasicAuthHelper.isFormAuthRequest( req, getConfig().getLoginUrl() ) ) {
            return true;
        }

        return false;
    }
}
