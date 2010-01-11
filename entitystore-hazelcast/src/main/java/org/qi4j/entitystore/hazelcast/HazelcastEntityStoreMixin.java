package org.qi4j.entitystore.hazelcast;

import com.hazelcast.core.Hazelcast;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Map;
import org.qi4j.api.configuration.Configuration;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.service.Activatable;
import org.qi4j.entitystore.map.MapEntityStore;
import org.qi4j.spi.entity.EntityType;
import org.qi4j.spi.entitystore.EntityNotFoundException;
import org.qi4j.spi.entitystore.EntityStoreException;
import org.qi4j.spi.entitystore.EntityStoreUnitOfWork;

/**
 * @author Paul Merlin <paul@nosphere.org>
 */
public class HazelcastEntityStoreMixin
    implements Activatable,
               MapEntityStore
{

    @This
    private Configuration<HazelcastConfiguration> config;
    private Map<String, String> stringMap;

    public void activate()
        throws Exception
    {
        stringMap = Hazelcast.getMap( config.configuration().mapName().get() );
    }

    public void passivate()
        throws Exception
    {
        stringMap = null;
    }

    public Reader get( EntityReference ref )
        throws EntityStoreException
    {
        final String serializedState = stringMap.get( ref.identity() );
        if( serializedState == null )
        {
            throw new EntityNotFoundException( ref );
        }
        return new StringReader( serializedState );
    }

    public void applyChanges( MapChanges changes )
        throws IOException
    {
        changes.visitMap( new MapChanger()
        {

            public Writer newEntity( final EntityReference ref, EntityType entityType )
                throws IOException
            {
                return new StringWriter( 1000 )
                {

                    @Override
                    public void close()
                        throws IOException
                    {
                        super.close();
                        stringMap.put( ref.identity(), toString() );
                    }
                };
            }

            public Writer updateEntity( EntityReference ref, EntityType entityType )
                throws IOException
            {
                return newEntity( ref, entityType );
            }

            public void removeEntity( EntityReference ref, EntityType entityType )
                throws EntityNotFoundException
            {
                stringMap.remove( ref.identity() );
            }
        } );
    }

    public void visitMap( MapEntityStoreVisitor visitor )
    {
        for( Map.Entry<String, String> eachEntry : stringMap.entrySet() )
        {
            visitor.visitEntity( new StringReader( eachEntry.getValue() ) );
        }
    }
}
