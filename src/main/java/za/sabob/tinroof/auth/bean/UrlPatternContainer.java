package za.sabob.tinroof.auth.bean;

import za.sabob.tinroof.auth.helper.AuthUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class UrlPatternContainer {

    private final static Logger LOGGER = Logger.getLogger( UrlPatternContainer.class.getName() );

    // dirs cater for paths /rest/ui/* and /rest/ui/admin/* and is determined based on if there is a '*' suffix
    private List<String> includeDirs = new ArrayList<>();

    // files cater for file extensions *.jsp and *.js and is determined based on if there is a '*' prefix
    private List<String> includeFiles = new ArrayList<>();

    // values cater for absolute values eg j_security_check and is determined is there is neither a '*' prefix or suffix
    private List<String> includeValues = new ArrayList<>();

    private List<String> excludeDirs = new ArrayList<>();

    private List<String> excludeFiles = new ArrayList<>();

    private List<String> excludeValues = new ArrayList<>();

    public List<String> getIncludeDirs() {
        return includeDirs;
    }

    public void setIncludeDirs( List<String> includeDirs ) {
        this.includeDirs = includeDirs;
    }

    public List<String> getIncludeValues() {
        return includeValues;
    }

    public void setIncludeValues( List<String> includeValues ) {
        this.includeValues = includeValues;
    }

    public List<String> getIncludeFiles() {
        return includeFiles;
    }

    public void setIncludeFiles( List<String> includeFiles ) {
        this.includeFiles = includeFiles;
    }

    public List<String> getExcludeDirs() {
        return excludeDirs;
    }

    public void setExcludeDirs( List<String> excludeDirs ) {
        this.excludeDirs = excludeDirs;
    }

    public List<String> getExcludeFiles() {
        return excludeFiles;
    }

    public void setExcludeFiles( List<String> excludeFiles ) {
        this.excludeFiles = excludeFiles;
    }

    public List<String> getExcludeValues() {
        return excludeValues;
    }

    public void setExcludeValues( List<String> excludeValues ) {
        this.excludeValues = excludeValues;
    }

    public void addInclude( String include ) {

        if ( AuthUtils.isBlank( include ) ) {
            throw new IllegalArgumentException( "include url pattern cannot be blank" );
        }

        if ( include.endsWith( "*" ) ) {
            // This is a directory pattern
            String value = include.substring( 0, include.length() - 1 );
            addDir( value, getIncludeDirs() );

        } else if ( include.startsWith( "*" ) ) {
            // This is a file extension pattern
            String value = include.substring( 1 );
            addFile( value, getIncludeFiles() );

        } else {
            // This is an absolute value
            addValue( include, getIncludeValues() );
        }
    }

    public void addExclude( String exclude ) {

        if ( AuthUtils.isBlank( exclude ) ) {
            throw new IllegalArgumentException( "exclude  url pattern cannot be blank" );
        }


        if ( exclude.endsWith( "*" ) ) {
            // This is a directory pattern
            String value = exclude.substring( 0, exclude.length() - 1 );
            addDir( value, getExcludeDirs() );

        } else if ( exclude.startsWith( "*" ) ) {
            // This is a file extension pattern
            String value = exclude.substring( 1 );
            addFile( value, getExcludeFiles() );

        } else {
            // This is an absolute value
            addValue( exclude, getExcludeFiles() );
        }
    }

    private void addDir( String path, List<String> list ) {

        if ( AuthUtils.isBlank( path ) ) {
            throw new IllegalArgumentException( "include/exclude directory must be a path eg '/rest'" );
        }

        if ( list.contains( path ) ) {
            return;
        }

        list.add( path );

    }

    private void addFile( String file, List<String> list ) {

        if ( AuthUtils.isBlank( file ) ) {
            throw new IllegalArgumentException( "include/exclude file must be a file eg 'index.jsp' or file extension '.jsp'" );
        }

        if ( list.contains( file ) ) {
            return;
        }

        list.add( file );

    }

    private void addValue( String value, List<String> list ) {

        if ( AuthUtils.isBlank( value ) ) {
            throw new IllegalArgumentException( "include/exclude value must be specified without a '*' eg '/ui/index.jsp'" );
        }

        if ( list.contains( value ) ) {
            return;
        }

        list.add( value );

    }
}
