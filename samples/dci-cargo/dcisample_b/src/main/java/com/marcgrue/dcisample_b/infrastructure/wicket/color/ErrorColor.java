package com.marcgrue.dcisample_b.infrastructure.wicket.color;

import org.apache.wicket.AttributeModifier;

/**
 *
 * ErrorColor
 *
 * Convenience method for adding an AttributeModifier based on a boolean value.
 */
public class ErrorColor extends AttributeModifier
{
    public ErrorColor( Boolean condition )
    {
        super( "class", condition ? "errorColor" : "VA_REMOVE" );
    }
}