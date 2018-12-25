package za.sabob.tinroof.auth.bean;

public class UsernameAndPassword {

    public String username;

    public String password;

    public UsernameAndPassword() {
    }

    public UsernameAndPassword( String username, String password ) {
        this.username = username;
        this.password = password;
    }
}
