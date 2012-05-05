package com.marcgrue.dcisample_b.infrastructure.wicket.color;

import org.apache.wicket.AttributeModifier;

/**
 * CorrectColor
 *
 * Convenience method for adding an AttributeModifier based on a boolean value.
 */
public class CorrectColor extends AttributeModifier
{
    public CorrectColor( Boolean condition )
    {
        super( "class", condition ? "correctColor" : "VA_REMOVE" );
    }
}