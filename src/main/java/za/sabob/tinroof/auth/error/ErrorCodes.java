package za.sabob.tinroof.auth.error;

public enum ErrorCodes {

    BASIC_AUTH_INVALID_USERNAME_AND_PASSWORD_TOKEN( 1000 ),
    AUTHENTICATION_ERROR( 1001 ),
    LOGOUT_ERROR( 1002 ),
    AUTHORIZATION_ERROR( 1003 ),
    REQUIRES_BASIC_AUTH( 1004 ),
    REQUIRES_FORM_AUTH( 1005 ),
    PASSWORD_REQUIRED( 1010 ),
    USERNAME_REQUIRED( 1011 );

    private int code;

    ErrorCodes( int code ) {
        this.code = code;
    }

    public int getCodeAsInt() {
        return code;
    }

    public void setCodeAsInt( int code ) {
        this.code = code;
    }
}
