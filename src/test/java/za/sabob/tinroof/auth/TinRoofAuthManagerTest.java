package za.sabob.tinroof.auth;

import com.sun.security.auth.UserPrincipal;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import za.sabob.tinroof.auth.bean.HttpMethod;
import za.sabob.tinroof.auth.bean.SecurityConstraint;
import za.sabob.tinroof.auth.bean.TinRoofConfig;
import za.sabob.tinroof.auth.builder.AuthBuilder;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

public class TinRoofAuthManagerTest {

    protected final static String LOGIN_PAGE = "/performBasicAuthLogin.jsp";

    protected final static String LOGIN_URL = "/j_security_check";

    protected final static String LOGOUT_URL = "/ibm_security_logout";

    @Test
    public void testIncludedPath() throws Exception {
        TinRoofAuthManager tinroof = createTinRoofAuthManager();
        HttpServletRequest req = createMockRequest();
        HttpServletResponse resp = createMockResponse();
        FilterChain chain = createFilterChain();
        tinroof.handleRequest( req, resp, chain );

        Assert.assertEquals( "bob", req.getRemoteUser() );
    }

    protected HttpServletResponse createMockResponse() {
        MockHttpServletResponse resp = new MockHttpServletResponse();
        return resp;
    }

    protected FilterChain createFilterChain() {
        MockFilterChain chain = new MockFilterChain();
        return chain;
    }

    protected HttpServletRequest createMockRequest() {

        MockHttpServletRequest req = new MockHttpServletRequest() {

            @Override
            public void login( String username, String password ) throws ServletException {
                setRemoteUser( username );
                setUserPrincipal( new UserPrincipal( username ) );
                setAuthType( FORM_AUTH );
            }
        };

        req.setContextPath( "/test" );
        req.setServletPath( "/rest/login" );
        req.addUserRole( "Assessor" );
        req.setMethod( HttpMethod.POST.description() );
        req.setParameter( "username", "bob" );
        req.setParameter( "password", "test" );
        return req;
    }

    protected TinRoofAuthManager createTinRoofAuthManager() {


        List<SecurityConstraint> sc = new AuthBuilder()

                .excludeByDefault( LOGIN_PAGE, LOGIN_URL, LOGOUT_URL )

                .addSecurityConstraint()
                .include( "/rest/*" )
                //.include( "/login" )
                .roles(
                        "Assessor",
                        "Administrator",
                        "Reinsurer" )

                .addSecurityConstraint()
                .include( "/rest/admin/*" )
                .roles( "SysAdmin" )

                .addSecurityConstraint()
                .include( "/rest/integration/*" )
                .httpMethodOmission( HttpMethod.DELETE )
                .roles( "SysAdmin" )

                .addSecurityConstraint()
                .include( "/rest/integration/*" )
                .httpMethod( HttpMethod.DELETE )
                .roles( "SysAdminWithDelete" )

                .build();

        //setSecurityConstraints( sc );

        TinRoofConfig config = new TinRoofConfig();
        config.setSecurityConstraints( sc );
        config.setLoginUrl( "/rest/login" );
        return new TinRoofAuthManager( config );
    }
}