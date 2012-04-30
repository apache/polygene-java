package com.marcgrue.dcisample_b.data.factory.exception;

/**
 * Thrown when trying to create an invalid Route Specification.
 */
public class CannotCreateRouteSpecificationException extends Exception
{
    private final String msg;

    public CannotCreateRouteSpecificationException( String msg )
    {
        this.msg = msg;
    }

    @Override
    public String getMessage()
    {
        return "Couldn't create a valid Route Specification: " + msg;
    }
}