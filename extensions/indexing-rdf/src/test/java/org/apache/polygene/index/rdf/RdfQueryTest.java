/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 */
package org.apache.polygene.index.rdf;

import org.apache.polygene.api.common.Visibility;
import org.apache.polygene.bootstrap.AssemblyException;
import org.apache.polygene.bootstrap.ModuleAssembly;
import org.apache.polygene.index.rdf.assembly.RdfNativeSesameStoreAssembler;
import org.apache.polygene.library.rdf.repository.NativeConfiguration;
import org.apache.polygene.test.EntityTestAssembler;
import org.apache.polygene.test.indexing.AbstractQueryTest;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.jupiter.api.Test;
import org.junit.rules.TemporaryFolder;

public class RdfQueryTest
    extends AbstractQueryTest
{

    @Rule
    public final TemporaryFolder tmpDir = new TemporaryFolder();

    @Override
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        super.assemble( module );
        new RdfNativeSesameStoreAssembler( Visibility.module, Visibility.module ).assemble( module );

        ModuleAssembly config = module.layer().module( "Config" );
        config.entities( NativeConfiguration.class ).visibleIn( Visibility.layer );
        config.forMixin( NativeConfiguration.class ).declareDefaults()
              .dataDirectory().set( tmpDir.getRoot().getAbsolutePath() );
        new EntityTestAssembler().assemble( config );
    }

    @Test
    @Ignore( "oneOf() Query Expression not supported by RDF Indexing" )
    @Override
    public void script23()
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
