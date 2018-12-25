package za.sabob.tinroof.auth.bean;

import java.util.ArrayList;
import java.util.List;

public class TinRoofConfig {

    public static final String DEFAULT_FORM_LOGIN_PAGE = "/login.jsp";

    public static final String DEFAULT_WELCOME_PAGE = "/index.jsp";

    public static final String DEFAULT_LOGIN_URL = "/login";

    public static final String DEFAULT_LOGOUT_URL = "/logout";

    //protected String formLoginPage = DEFAULT_FORM_LOGIN_PAGE;

    protected String welcomePage = DEFAULT_WELCOME_PAGE;

    protected String loginUrl = DEFAULT_LOGIN_URL;

    protected String logoutUrl = DEFAULT_LOGOUT_URL;

    protected boolean handleFormLogin = true;

    protected boolean handleFormLogout = true;

    protected boolean notifyBasicAuthRequired = true;

    protected List<SecurityConstraint> securityConstraints = new ArrayList<>();

//    public String getFormLoginPage() {
//        return formLoginPage;
//    }
//
//    public void setFormLoginPage( String formLoginPage ) {
//        this.formLoginPage = formLoginPage;
//    }

    public String getWelcomePage() {
        return welcomePage;
    }

    public void setWelcomePage( String welcomePage ) {
        this.welcomePage = welcomePage;
    }

    public String getLoginUrl() {
        return loginUrl;
    }

    public void setLoginUrl( String loginUrl ) {
        this.loginUrl = loginUrl;
    }

    public String getLogoutUrl() {
        return logoutUrl;
    }

    public void setLogoutUrl( String logoutUrl ) {
        this.logoutUrl = logoutUrl;
    }

    public List<SecurityConstraint> getSecurityConstraints() {
        return securityConstraints;
    }

    public void setSecurityConstraints( List<SecurityConstraint> securityConstraints ) {
        this.securityConstraints = securityConstraints;
    }

    public boolean isHandleFormLogin() {
        return handleFormLogin;
    }

    public void setHandleFormLogin( boolean handleFormLogin ) {
        this.handleFormLogin = handleFormLogin;
    }

    public boolean isHandleFormLogout() {
        return handleFormLogout;
    }

    public void setHandleFormLogout( boolean handleFormLogout ) {
        this.handleFormLogout = handleFormLogout;
    }

    public boolean isNotifyBasicAuthRequired() {
        return notifyBasicAuthRequired;
    }

    public void setNotifyBasicAuthRequired( boolean notifyBasicAuthRequired ) {
        this.notifyBasicAuthRequired = notifyBasicAuthRequired;
    }
}
