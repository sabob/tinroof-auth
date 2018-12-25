package za.sabob.tinroof.auth.bean;

import za.sabob.tinroof.auth.helper.AuthUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SecurityConstraint {

    private final static Logger LOGGER = Logger.getLogger( SecurityConstraint.class.getName() );

    private UrlPatternContainer urlPatternContainer = new UrlPatternContainer();

    private List<String> roles = new ArrayList<>();

    private List<HttpMethod> httpMethods = new ArrayList<>();

    private List<HttpMethod> httpMethodOmissions = new ArrayList<>();

    public UrlPatternContainer getUrlPatternContainer() {
        return urlPatternContainer;
    }

    public void setUrlPatternContainer( UrlPatternContainer urlPatternContainer ) {

        this.urlPatternContainer = urlPatternContainer;
    }

    public void addInclude( String... includeArray ) {

        if ( includeArray == null ) {
            return;
        }

        Stream.of( includeArray )
                .filter( Objects::nonNull )
                .distinct()
                .forEach( include -> {
                    urlPatternContainer.addInclude( include );
                } );
    }

    public void addExclude( String... excludeArray ) {

        if ( excludeArray == null ) {
            return;
        }

        Stream.of( excludeArray )
                .filter( Objects::nonNull )
                .distinct()
                .forEach( exclude -> {
                    urlPatternContainer.addExclude( exclude );
                } );
    }

    public List<HttpMethod> getHttpMethods() {
        return httpMethods;
    }

    public void setHttpMethods( List<HttpMethod> httpMethods ) {
        this.httpMethods = httpMethods;
    }

    public void addHttpMethod( HttpMethod... httpMethodArray ) {

        Stream<HttpMethod> s1 = Stream.of( httpMethodArray );
        Stream<HttpMethod> s2 = this.httpMethods.stream();

        this.httpMethods = Stream.concat( s1, s2 )
                .filter( Objects::nonNull )
                .distinct()
                .collect( Collectors.toList() );
    }

    public List<HttpMethod> getHttpMethodOmissions() {
        return httpMethodOmissions;
    }

    public void setHttpMethodOmissions( List<HttpMethod> httpMethodOmissions ) {
        this.httpMethodOmissions = httpMethodOmissions;
    }

    public void addHttpMethodOmission( HttpMethod... httpMethodOmissionArray ) {

        Stream<HttpMethod> s1 = Stream.of( httpMethodOmissionArray );
        Stream<HttpMethod> s2 = this.httpMethodOmissions.stream();

        this.httpMethodOmissions = Stream.concat( s1, s2 )
                .filter( Objects::nonNull )
                .distinct()
                .collect( Collectors.toList() );
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles( List<String> roles ) {
        this.roles = roles;
    }

    public void addRole( String... roleArray ) {

        Stream<String> s1 = Stream.of( roleArray );
        Stream<String> s2 = this.roles.stream();

        this.roles = Stream.concat( s1, s2 )
                .filter( Objects::nonNull )
                .distinct()
                .collect( Collectors.toList() );
    }

    public boolean allowAccess( HttpServletRequest req ) {

        MatchResult matchResult = match( req );

        if ( matchResult.isIncluded() ) {

            boolean isAuthorizedRequest = authorize( req );

            if ( isAuthorizedRequest ) {
                return true;

            }
        }
        return false;
    }

    public boolean doesHttpMethodApply( HttpServletRequest req ) {
        return doesHttpMethodApply( req.getMethod() );
    }

    public boolean doesHttpMethodApply( String method ) {

        Optional<HttpMethod> optional = HttpMethod.lookup( method );

        if ( optional.isPresent() ) {
            return doesHttpMethodApply( optional.get() );

        } else {
            return true;
        }
    }

    public boolean doesHttpMethodApply( HttpMethod value ) {

        for ( HttpMethod omission : getHttpMethodOmissions() ) {
            if ( value == omission ) {
                return false;
            }
        }

        // No http method defined for constraint, so it always apply
        if ( getHttpMethods().isEmpty() ) {
            return true;
        }

        for ( HttpMethod method : getHttpMethods() ) {
            if ( value == method ) {
                return true;
            }
        }

        return false;
    }

    public MatchResult match( HttpServletRequest req ) {

        String requestPath = AuthUtils.getResourcePath( req );
        MatchResult matchResult = match( requestPath );
        return matchResult;
    }

    public MatchResult match( String requestPath ) {

        MatchResult matchResult = matchExcludePaths( null, requestPath );

        if ( matchResult.isExcluded() ) {
            return matchResult;
        }

        matchResult = matchIncludePaths( matchResult, requestPath );

        return matchResult;
    }

    public boolean authorize( HttpServletRequest req ) {

        for ( String role : getRoles() ) {
            if ( req.isUserInRole( role ) ) {
                return true;
            }
        }
        return false;
    }

    private MatchResult matchIncludePaths( MatchResult mr, String requestPath ) {

        if ( mr == null ) {
            mr = new MatchResult( false, false, this, null );
        }


        // Try match path on absolute value
        boolean matchFound = false;

        List<String> includeValues = urlPatternContainer.getIncludeValues();

        // Try match path as a file
        for ( String includeValue : includeValues ) {

            matchFound = matchValue( requestPath, includeValue );
            if ( matchFound ) {
                mr = MatchResult.createNewMatchResultIfMatchedPathIsMoreSpecific( mr, true, mr.isExcluded(), this, includeValue );
            }
        }

        // Try match path on dir
        List<String> includeDirs = urlPatternContainer.getIncludeDirs();

        if ( !matchFound ) {

            for ( String includeDir : includeDirs ) {

                matchFound = matchDirPattern( requestPath, includeDir );
                if ( matchFound ) {
                    mr = MatchResult.createNewMatchResultIfMatchedPathIsMoreSpecific( mr, true, mr.isExcluded(), this, includeDir );
                }
            }
        }

        if ( !matchFound ) {

            List<String> includeFiles = urlPatternContainer.getIncludeFiles();

            // Try match path as a file
            for ( String includeFile : includeFiles ) {

                matchFound = matchFilePattern( requestPath, includeFile );
                if ( matchFound ) {
                    mr = MatchResult.createNewMatchResultIfMatchedPathIsMoreSpecific( mr, true, mr.isExcluded(), this, includeFile );
                }
            }
        }

        return mr;
    }

    private MatchResult matchExcludePaths( MatchResult mr, String requestPath ) {

        if ( mr == null ) {
            mr = new MatchResult( false, false, this, null );
        }

        // Try to match on value
        List<String> excludeValues = urlPatternContainer.getExcludeValues();

        for ( String excludeValue : excludeValues ) {

            boolean matchFound = matchValue( requestPath, excludeValue );

            if ( matchFound ) {
                // Excluded match found, no need to continue
                mr = new MatchResult( mr.isIncluded(), true, this, excludeValue );
                return mr;
            }
        }

        // Try to match on dir
        List<String> excludeDirs = urlPatternContainer.getExcludeDirs();

        for ( String excludeDir : excludeDirs ) {

            boolean matchFound = matchDirPattern( requestPath, excludeDir );

            if ( matchFound ) {
                // Excluded match found, no need to continue
                mr = new MatchResult( mr.isIncluded(), true, this, excludeDir );
                return mr;
            }
        }

        // Try to match on file
        List<String> excludeFiles = urlPatternContainer.getExcludeFiles();

        for ( String excludeFile : excludeFiles ) {

            boolean matchFound = matchFilePattern( requestPath, excludeFile );

            if ( matchFound ) {
                // Excluded match found, no need to continue
                mr = new MatchResult( mr.isIncluded(), true, this, excludeFile );
                return mr;
            }
        }

        return mr;
    }

    private boolean matchFilePattern( String requestPath, String filePattern ) {

        requestPath = AuthUtils.removeStart( requestPath, "/" );
        filePattern = AuthUtils.removeStart( filePattern, "/" );

        String[] requestSegments = requestPath.split( "/" );
        String[] secureSegments = filePattern.split( "/" );
        boolean segmentsMatch = matchSegments( requestSegments, secureSegments );
//
//        if ( requestPath.endsWith( filePattern ) ) {
//            return true;
//        }

        return segmentsMatch;
    }

    private boolean matchDirPattern( String requestPath, String dirPattern ) {

        requestPath = AuthUtils.removeStart( requestPath, "/" );
        dirPattern = AuthUtils.removeStart( dirPattern, "/" );

        String[] requestSegments = requestPath.split( "/" );
        String[] secureSegments = dirPattern.split( "/" );
        boolean segmentsMatch = matchSegments( requestSegments, secureSegments );
        return segmentsMatch;
    }

    private boolean matchValue( String requestPath, String value ) {

        requestPath = AuthUtils.removeStart( requestPath, "/" );
        value = AuthUtils.removeStart( value, "/" );

        if ( requestPath.equals( value ) ) {
            return true;
        }

        return false;
    }

    private boolean matchSegments( String[] requestPathSegments, String[] securePathSegments ) {

        // if request path is "/rest" and secure path is longer "/rest/ui" then there won't be a match so exit early
        if ( requestPathSegments.length < securePathSegments.length ) {
            return false;
        }

        for ( int i = 0; i < securePathSegments.length; i++ ) {
            String securePathSegment = securePathSegments[i];
            String requestPathSegment = requestPathSegments[i];

            if ( !requestPathSegment.equalsIgnoreCase( securePathSegment ) ) {
                return false;
            }
        }

        return true;
    }

    public String toString() {
        return "SecurityConstraint: \nincludeDirs: " + urlPatternContainer.getIncludeDirs()
                + "\nincludeFiles: " + urlPatternContainer.getIncludeFiles()
                + "\nexcludeDirs: " + urlPatternContainer.getExcludeDirs()
                + "\nexcludeFiles: " + urlPatternContainer.getExcludeFiles()
                + "\nrole: " + getRoles();
    }

    public static void main( String[] args ) {
        SecurityConstraint sc = new SecurityConstraint();
        //Stream s1 = Stream.of( "one", "two", "three" );
        //Stream s2 = Stream.of( "one", "four", "five" );
        //List result = sc.createUrlPatternList( s1, s2 );
        //System.out.println( result );

//        sc.addInclude( "rest/xxx/", "rest/ui", "/rest/ui/pok/" );
//        //sc.addExclude( "rest/xxx/" );//
//        MatchResult matchResult = sc.match( "/rest/ui/pok" );
//        System.out.println( "Matched path : " + matchResult.getMatchedPath() );
//        System.out.println( "INCLUDED : " + matchResult.isIncluded() );
//        System.out.println( "EXCLUDED : " + matchResult.isExcluded() );
        //sc = new SecurityConstraint();

        sc.addInclude( "*.jsp" );
        sc.addInclude( "*/rest/ui/admin/index.jsp" );
        //sc.addExclude( "*/moo/index.jsp" );
        sc.addInclude( "*index.jsp" );
        //sc.addInclude( "/moo/j_security_check" );

        //sc.addInclude( "*/ui" );
        sc.addInclude( "/rest/ui/*" );
        //sc.addInclude( "/rest/*" );

        MatchResult matchResult = sc.match( "/rest/ui/admin/index.jsp" );
        System.out.println( "Matched path : " + matchResult.getMatchedPath() );
        System.out.println( "EXCLUDED : " + matchResult.isExcluded() );
        System.out.println( "INCLUDED : " + matchResult.isIncluded() );
    }
}
