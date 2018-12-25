package za.sabob.tinroof.auth.bean;

/**
 * Immutable class
 */
public class MatchResult {

    private SecurityConstraint securityConstraint;

    private String matchedPath;

    private boolean excluded = false;

    private boolean included = false;

    public MatchResult( boolean included, boolean excluded, SecurityConstraint sc, String matchedPath ) {
        this.included = included;
        this.excluded = excluded;
        this.securityConstraint = sc;
        this.matchedPath = matchedPath;
    }

    public SecurityConstraint getSecurityConstraint() {
        return securityConstraint;
    }

    public boolean isExcluded() {
        return excluded;
    }

//    public void setExcluded( boolean excluded ) {
//        this.excluded = excluded;
//    }

    public boolean isIncluded() {
        return included;
    }
//
//    public void setIncluded( boolean included ) {
//        this.included = included;
//    }

    public String getMatchedPath() {
        return matchedPath;
    }
//
//    public void setMatchedPath( String matchedPath ) {
//        this.matchedPath = matchedPath;
//    }

    public static MatchResult createNewMatchResultIfMatchedPathIsMoreSpecific( MatchResult mr, boolean included, boolean excluded, SecurityConstraint sc, String matchedPath ) {

        if ( matchedPath == null ) {
            throw new IllegalArgumentException( "matchedPath cannot be null" );
        }

        if ( mr == null ) {
            mr = new MatchResult( included, excluded, sc, matchedPath );
            return mr;
        }

        if ( mr.getMatchedPath() == null || mr.getMatchedPath().length() < matchedPath.length() ) {
            mr = new MatchResult( included, excluded, sc, matchedPath );

        } else {
            mr = new MatchResult( included, excluded, mr.getSecurityConstraint(), mr.getMatchedPath() );
        }

        return mr;
    }

    @Override
    public String toString() {
        return "MathResult: matchedPath: " + getMatchedPath()
                + ", included: " + isIncluded()
                + ", excluded: " + isExcluded()
                + ", securityConstraint: " + getSecurityConstraint();
    }
}
