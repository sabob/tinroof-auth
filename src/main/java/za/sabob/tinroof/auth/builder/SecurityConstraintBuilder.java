package za.sabob.tinroof.auth.builder;

import za.sabob.tinroof.auth.bean.HttpMethod;
import za.sabob.tinroof.auth.bean.SecurityConstraint;

import java.util.ArrayList;
import java.util.List;

public class SecurityConstraintBuilder {

    private List<SecurityConstraint> securityConstraints = new ArrayList<>();

    private SecurityConstraint securityConstraint;

    private AuthBuilder parent;

    SecurityConstraintBuilder( AuthBuilder parent ) {
        this.parent = parent;
        //addSecurityConstraint();
    }
//
//    public SecurityConstraintBuilder() {
//        addSecurityConstraint();
//    }

    public final SecurityConstraintBuilder addSecurityConstraint() {
        this.securityConstraint = new SecurityConstraint();
        this.securityConstraint.addExclude( parent.getExcludeByDefault() );
        securityConstraints.add( this.securityConstraint );
        return this;
    }

    public SecurityConstraintBuilder include( String... urlPattern ) {
        securityConstraint.addInclude( urlPattern );
        return this;
    }

    public SecurityConstraintBuilder httpMethod( HttpMethod... methods ) {
        securityConstraint.addHttpMethod( methods );
        return this;
    }

    public SecurityConstraintBuilder httpMethodOmission( HttpMethod... methods ) {
        securityConstraint.addHttpMethodOmission( methods );
        return this;
    }

    public SecurityConstraintBuilder exclude( String... urlPattern ) {
        securityConstraint.addExclude( urlPattern );
        return this;
    }

    public SecurityConstraintBuilder roles( String... role ) {
        securityConstraint.addRole( role );
        return this;
    }

    public SecurityConstraint buildOne() {
        return securityConstraint;
    }

    public List<SecurityConstraint> build() {
        return securityConstraints;
    }

    public static void main( String[] args ) {
        new AuthBuilder().addSecurityConstraint();

        List<SecurityConstraint> constraints = new AuthBuilder()
                .excludeByDefault( "DEFAULT_1", "DEFAULT_2" )
                .addSecurityConstraint()
                .include( "/*", "/rest/*" )
                .exclude( "/performBasicAuthLogin.jsp", "j_security_check" )
                .roles( "Administrator" )

                .addSecurityConstraint()
                .include( "/moo" )
                .include()
                .include( null )
                .roles( "Assessor" )
                .exclude( null, null, "xx" )
                .roles(
                        "Administrator",
                        "Administrator",
                        "Administrator",
                        "Assessor"
                )
                .httpMethod( HttpMethod.GET )
                .httpMethodOmission( HttpMethod.DELETE )
                .build();

        constraints.stream().forEach( System.out::println );

        SecurityConstraint constraint = new AuthBuilder().addSecurityConstraint()
                .include( "/*", "/rest/*" )
                .exclude( "/performBasicAuthLogin.jsp", "j_security_check" )
                .roles( "Administrator" )
                .addSecurityConstraint()
                .include( "/moo" )
                .roles( "Assessor" )
                .buildOne();

        System.out.println( "--------------------" );
        System.out.println( constraint );

    }

}
