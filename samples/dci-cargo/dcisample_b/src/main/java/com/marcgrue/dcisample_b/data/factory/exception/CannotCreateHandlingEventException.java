package com.marcgrue.dcisample_b.data.factory.exception;

import com.marcgrue.dcisample_b.data.structure.handling.HandlingEvent;

/**
 * If a {@link HandlingEvent} can't be created from a given set of parameters.
 *
 * It is a checked exception because it's not a programming error, but rather a
 * special case that the application is built to handle. It can occur during normal
 * program execution.
 */
public class CannotCreateHandlingEventException extends Exception
{
    public CannotCreateHandlingEventException( String s )
    {
        super( s );
    }
}