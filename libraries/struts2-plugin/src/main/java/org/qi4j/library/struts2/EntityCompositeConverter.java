package org.qi4j.library.struts2;

import com.opensymphony.xwork2.inject.Inject;
import java.util.Map;
import org.apache.struts2.util.StrutsTypeConverter;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;

/**
 * <p>Converts Strings to entities and entities to strings.</p>
 *
 * <p>To convert from a string to an entity the value passed in is expected to be an identity.  The identity can
 * be either a fully qualified identity, e.g org.qi4j.example.Person:368e27f0-ea39-497a-9069-ffb5f41bf174-0, or it can
 * be just the uuid.</p>
 *
 * <p>Conversion to a String is done using the EntityComposites identity and and returning just the uuid portion.</p>
 */
public class EntityCompositeConverter
    extends StrutsTypeConverter
{
    private UnitOfWorkFactory uowf;

    @Inject
    public void setUnitOfWorkFactory( UnitOfWorkFactory uowf )
    {
        this.uowf = uowf;
    }

    @Override
    public Object convertFromString( Map context, String[] values, Class toClass )
    {
        String identity = extractIdentity( values );
        UnitOfWork uow = uowf.currentUnitOfWork();
        return uow.get( toClass, identity );
    }

    @Override
    public String convertToString( Map context, Object o )
    {
        return ( (EntityComposite) o ).identity().get();
    }

    private String extractIdentity( String[] values )
    {
        String identity = values[ 0 ];
        int separatorIndex = identity.indexOf( ':' );
        if( separatorIndex == -1 )
        {
            return identity;
        }
        return identity = identity.substring( separatorIndex + 1 );
    }
}
