package org.apache.polygene.spi.query;

/**
 * This is the exception for Indexing problems. Subtypes should be created for specific problems.
 *
 */
public abstract class IndexingException extends RuntimeException
{
    public IndexingException( String message )
    {
        super( message );
    }
    public IndexingException( String message, Throwable cause )
    {
        super( message, cause );
    }
}
