package com.marcgrue.dcisample_b.data.factory.exception;

import com.marcgrue.dcisample_b.data.structure.tracking.TrackingId;

/**
 * If a Cargo can't be created.
 *
 * Usually because a supplied tracking id string
 * - doesn't match the pattern in {@link TrackingId}
 * - is not unique
 */
public class CannotCreateCargoException extends Exception
{
    public CannotCreateCargoException( String s )
    {
        super( s );
    }
}