/*
 * Copyright (c) 2011, Rickard Öberg. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
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
