package org.qi4j.spi.entity;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import org.qi4j.api.common.QualifiedName;
import org.qi4j.api.common.TypeName;
import org.qi4j.spi.util.Base64Encoder;

public class SchemaVersion
{
    private final MessageDigest md;

    public SchemaVersion()
    {
        try
        {
            md = MessageDigest.getInstance( "SHA" );
        }
        catch( NoSuchAlgorithmException e )
        {
            throw new RuntimeException( "Error creating SHA Schema version", e );
        }
    }

    public void versionize( TypeName typeName )
    {
        versionize( typeName.name() );
    }

    public void versionize( QualifiedName qualifiedName )
    {
        versionize( qualifiedName.toString() );
    }

    public String base64()
    {
        try
        {
            return new String( Base64Encoder.encode( calculate(), false ), "UTF-8" );
        }
        catch( UnsupportedEncodingException e )
        {
            throw new IllegalStateException( e );
        }
    }

    public byte[] calculate()
    {
        return md.digest();
    }

    public void versionize( String value )
    {
        try
        {
            md.update( value.getBytes( "UTF-8" ) );
        }
        catch( UnsupportedEncodingException e )
        {
            throw new RuntimeException( "Error getting bytes from " + value, e );
        }
    }
}
