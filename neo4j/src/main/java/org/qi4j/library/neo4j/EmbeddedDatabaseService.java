package org.qi4j.library.neo4j;

import java.io.File;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.kernel.EmbeddedGraphDatabase;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.Activatable;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.service.ServiceDescriptor;
import org.qi4j.library.fileconfig.FileConfiguration;

/**
 * TODO
 */
@Mixins(EmbeddedDatabaseService.Mixin.class)
public interface EmbeddedDatabaseService
    extends ServiceComposite, Activatable
{
    GraphDatabaseService database();

    abstract class Mixin
        implements EmbeddedDatabaseService
    {
        @Service
        FileConfiguration config;

        @Uses
        ServiceDescriptor descriptor;

        EmbeddedGraphDatabase db;

        @Override
        public GraphDatabaseService database()
        {
            return db;
        }

        @Override
        public void activate()
            throws Exception
        {
            String path = new File( config.dataDirectory(), identity().get() ).getAbsolutePath();
            db = new EmbeddedGraphDatabase( path );
        }

        @Override
        public void passivate()
            throws Exception
        {
            db.shutdown();
        }
    }
}
