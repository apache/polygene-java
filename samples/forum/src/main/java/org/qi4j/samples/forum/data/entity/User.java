package org.qi4j.samples.forum.data.entity;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Immutable;
import org.qi4j.api.property.Property;
import org.qi4j.api.util.Base64Encoder;

/**
 * TODO
 */
@Mixins( User.Mixin.class )
public interface User
    extends EntityComposite
{
    @Immutable
    public Property<String> name();

    public Property<String> realName();

    public Property<String> email();

    public Property<String> password();

    public String hashPassword( String password );

    public boolean isCorrectPassword( String password );

    abstract class Mixin
        implements User
    {
        @Override
        public String hashPassword( String password )
        {
            try
            {
                MessageDigest md = MessageDigest.getInstance( "SHA" );
                md.update( password.getBytes( "UTF-8" ) );
                byte raw[] = md.digest();

                String hash = new String( Base64Encoder.encode( raw, false ) );
                return hash;
            }
            catch( NoSuchAlgorithmException e )
            {
                throw new IllegalStateException( "No SHA algorithm found", e );
            }
            catch( UnsupportedEncodingException e )
            {
                throw new IllegalStateException( e.getMessage(), e );
            }
        }

        @Override
        public boolean isCorrectPassword( String password )
        {
            return password().get().equals( hashPassword( password ) );
        }
    }
}
