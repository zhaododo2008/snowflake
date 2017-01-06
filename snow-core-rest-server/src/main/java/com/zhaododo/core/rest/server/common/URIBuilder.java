package com.zhaododo.core.rest.server.common;

import java.net.URISyntaxException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class URIBuilder {
  private static final Logger logger = LoggerFactory.getLogger( URIBuilder.class );

  private org.apache.http.client.utils.URIBuilder builder;

  public URIBuilder( String baseUrl ) {
    try {
      builder = new org.apache.http.client.utils.URIBuilder( baseUrl );
    } catch ( URISyntaxException e ) {
      logger.error( "url is not well formed", e );
    }
  }

  public URIBuilder addPath( String subPath ) {
    if ( subPath == null || subPath.isEmpty() || "/".equals( subPath ) ) {
      return this;
    }

    if ( builder != null ) {
      builder.setPath( this.appendSegmentToPath( builder.getPath(), subPath ) );
    }
    return this;
  }

  public URIBuilder addParameter( String key, String value ) {
    if ( builder != null ) {
      builder.addParameter( key, value );
    }
    return this;
  }

  public String build() {
    try {
      if ( builder != null ) {
        return this.builder.build().toString();
      }
    } catch ( URISyntaxException e ) {
      logger.error( "url is not well formed.", e );
    }
    return null;
  }

  private String appendSegmentToPath( String path, String segment ) {
    if ( path == null || path.isEmpty() ) {
      path = "/";
    }

    if ( path.charAt( path.length() - 1 ) == '/' || segment.startsWith( "/" ) ) {
      return path + segment;
    }

    return path + "/" + segment;
  }
}
