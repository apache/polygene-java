package com.marcgrue.dcisample_a.infrastructure.wicket.color;

import org.apache.wicket.AttributeModifier;

/**
 * Javadoc
 */

public class ErrorColor extends AttributeModifier
{
    public ErrorColor( Boolean condition )
    {
        super( "class", condition ? "errorColor" : "VA_REMOVE" );
    }
}