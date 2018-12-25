package za.sabob.tinroof.auth.builder;

public class AuthBuilder {

    private String[] excludeByDefault = new String[0];

    public AuthBuilder() {

    }

    public AuthBuilder excludeByDefault( String... urlPattern ) {
        excludeByDefault = urlPattern;
        return this;
    }

    public final SecurityConstraintBuilder addSecurityConstraint() {
        SecurityConstraintBuilder scBuilder = new SecurityConstraintBuilder( this );
        scBuilder.addSecurityConstraint();
        return scBuilder;
    }

    public String[] getExcludeByDefault() {
        return excludeByDefault;
    }
}
