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
package org.apache.zest.index.rdf;

import java.io.File;
import org.apache.zest.api.common.Visibility;
import org.apache.zest.bootstrap.AssemblyException;
import org.apache.zest.bootstrap.ModuleAssembly;
import org.apache.zest.index.rdf.assembly.RdfNativeSesameStoreAssembler;
import org.apache.zest.library.rdf.repository.NativeConfiguration;
import org.apache.zest.spi.query.EntityFinderException;
import org.apache.zest.test.EntityTestAssembler;
import org.apache.zest.test.indexing.AbstractQueryTest;
import org.apache.zest.test.util.DelTreeAfter;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

public class RdfQueryTest
    extends AbstractQueryTest
{

    private static final File DATA_DIR = new File( "build/tmp/rdf-query-test" );
    @Rule
    public final DelTreeAfter delTreeAfter = new DelTreeAfter( DATA_DIR );

    @Override
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        super.assemble( module );
        new RdfNativeSesameStoreAssembler( Visibility.module, Visibility.module ).assemble( module );

        ModuleAssembly config = module.layer().module( "Config" );
        config.entities( NativeConfiguration.class ).visibleIn( Visibility.layer );
        config.forMixin( NativeConfiguration.class ).declareDefaults().dataDirectory().set( DATA_DIR.getAbsolutePath() );
        new EntityTestAssembler().assemble( config );
    }

    @Test
    @Ignore( "oneOf() Query Expression not supported by RDF Indexing" )
    @Override
    public void script23()
        throws EntityFinderException
    {
        super.script23();
    }

    @Test
    @Ignore( "Deep queries in complex values are not supported by RDF Indexing" )
    @Override
    public void script29()
    {
        super.script29();
    }

    @Test
    @Ignore( "NamedAssociation are not supported by RDF Indexing" )
    @Override
    public void script35()
    {
        super.script35();
    }

    @Test
    @Ignore( "NamedAssociation are not supported by RDF Indexing" )
    @Override
    public void script36()
    {
        super.script36();
    }

}
