package za.sabob.tinroof.auth.error;

public class AuthError {

    private ErrorCodes code;

    private String message;

    public AuthError() {

    }

    public AuthError( ErrorCodes code, String message ) {
        this.code = code;
        this.message = message;

    }

    public int getCode() {

        return code.getCodeAsInt();
    }

    public void setCode( ErrorCodes code ) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage( String message ) {
        this.message = message;
    }
}
