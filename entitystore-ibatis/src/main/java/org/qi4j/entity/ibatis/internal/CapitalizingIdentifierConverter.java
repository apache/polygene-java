package org.qi4j.entity.ibatis.internal;

import org.qi4j.entity.ibatis.IdentifierConverter;

/**
 * @autor Michael Hunger
 * @since 19.05.2008
 */
public class CapitalizingIdentifierConverter implements IdentifierConverter
{
    public String convertIdentifier( final String qualifiedIdentifier )
    {
        final String name = unqualify( qualifiedIdentifier );
        if (name.equalsIgnoreCase( "identity" )) return "ID";
        return name.toUpperCase();
    }

    private String unqualify( final String qualifiedIdentifier )
    {
        final int colonIndex = qualifiedIdentifier.lastIndexOf( ":" );
        if( colonIndex == -1 )
        {
            return qualifiedIdentifier;
        }
        else
        {
            return qualifiedIdentifier.substring( colonIndex + 1 );
        }
    }

}
