/*
 * Copyright 2008 Alin Dreghiciu.
 * Copyright 2009 Niclas Hedhman.
 *
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.qi4j.index.rdf;

import org.junit.After;
import org.qi4j.api.common.Visibility;
import org.qi4j.api.value.ValueSerialization;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.index.rdf.query.RdfQueryParserFactory;
import org.qi4j.library.fileconfig.FileConfigurationService;
import org.qi4j.library.rdf.entity.EntityStateSerializer;
import org.qi4j.library.rdf.entity.EntityTypeSerializer;
import org.qi4j.library.rdf.repository.NativeConfiguration;
import org.qi4j.library.rdf.repository.NativeRepositoryService;
import org.qi4j.test.EntityTestAssembler;
import org.qi4j.test.indexing.AbstractQueryTest;
import org.qi4j.valueserialization.orgjson.OrgJsonValueSerializationService;

//@Ignore("Getting failures when running under Gradle and new OpenRDF version." )
public class RdfQueryTest extends AbstractQueryTest
{

    @Override
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        super.assemble( module );
        module.services( FileConfigurationService.class );
        module.services( NativeRepositoryService.class, RdfQueryParserFactory.class ).instantiateOnStartup();
        module.services( RdfIndexingEngineService.class ).instantiateOnStartup();
        module.services( OrgJsonValueSerializationService.class ).taggedWith( ValueSerialization.Formats.JSON );
        module.objects( EntityStateSerializer.class, EntityTypeSerializer.class );

        ModuleAssembly config = module.layer().module( "Config" );
        config.entities( NativeConfiguration.class ).visibleIn( Visibility.layer );
        new EntityTestAssembler().assemble( config );
    }

    @Override
    @After
    public void tearDown()
        throws Exception
    {
        java.io.File data = null;
        if( unitOfWork != null )
        {
            NativeConfiguration conf = unitOfWork.get( NativeConfiguration.class, "NativeRepositoryService" );
            data = new java.io.File( conf.dataDirectory().get() );
            unitOfWork.discard();
        }
        if( data != null )
        {
            remove( data );
        }
        super.tearDown();
    }

    private void remove( java.io.File data )
    {
        if( data.isDirectory() )
        {
            for( java.io.File file : data.listFiles() )
            {
                remove( file );
            }

            data.delete();
        }
        else
        {
            data.delete();
        }
    }
}
