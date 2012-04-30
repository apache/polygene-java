package com.marcgrue.dcisample_a.infrastructure.wicket.color;

import org.apache.wicket.AttributeModifier;

/**
 * Javadoc
 */
public class CorrectColor extends AttributeModifier
{
    public CorrectColor( Boolean condition )
    {
        super( "class", condition ? "correctColor" : "VA_REMOVE" );
    }
}