package za.sabob.tinroof.auth.bean;

public interface AuthRequest {

    public boolean isUserInRole( String role );

    public String getMethod();

    public String getRemoteUser();

    public String getContextPath();

    public boolean login( String username, String password );

    public boolean logout();

}
