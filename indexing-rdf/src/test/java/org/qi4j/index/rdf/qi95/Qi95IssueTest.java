/*
 * Copyright 2008 Richard Wallace. All Rights Reserved.
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
package org.qi4j.index.rdf.qi95;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Random;
import org.junit.Test;
import org.qi4j.api.common.Visibility;
import org.qi4j.api.entity.EntityBuilder;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.property.Property;
import org.qi4j.api.query.QueryBuilder;
import org.qi4j.api.query.QueryBuilderFactory;
import org.qi4j.api.structure.Module;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.bootstrap.ApplicationAssembler;
import org.qi4j.bootstrap.ApplicationAssembly;
import org.qi4j.bootstrap.ApplicationAssemblyFactory;
import org.qi4j.bootstrap.Assembler;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.Energy4Java;
import org.qi4j.bootstrap.LayerAssembly;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.entitystore.jdbm.JdbmConfiguration;
import org.qi4j.entitystore.jdbm.assembly.JdbmEntityStoreAssembler;
import org.qi4j.index.rdf.assembly.RdfMemoryStoreAssembler;
import org.qi4j.index.rdf.assembly.RdfNativeSesameStoreAssembler;
import org.qi4j.library.rdf.repository.NativeConfiguration;
import org.qi4j.spi.structure.ApplicationSPI;
import org.qi4j.test.EntityTestAssembler;

import static org.junit.Assert.*;

public class Qi95IssueTest
{

    @Test
    public void canCreateAndQueryWithNativeRdfAndJdbm()
        throws Exception
    {
        ApplicationSPI application = createApplication( nativeRdf, jdbmStore, domain );
        try
        {
            application.activate();
            Module domain = application.findModule( "Domain", "Domain" );
            UnitOfWorkFactory unitOfWorkFactory = domain.unitOfWorkFactory();
            createABunchOfStuffAndDoQueries( unitOfWorkFactory, domain.queryBuilderFactory() );
        }
        finally
        {
            application.passivate();
        }
    }

    @Test
    public void canCreateAndQueryWithAllInMemory()
        throws Exception
    {
        ApplicationSPI application = createApplication( inMemoryRdf, inMemoryStore, domain );
        try
        {
            application.activate();
            Module domain = application.findModule( "Domain", "Domain" );
            UnitOfWorkFactory unitOfWorkFactory = domain.unitOfWorkFactory();
            createABunchOfStuffAndDoQueries( unitOfWorkFactory, domain.queryBuilderFactory() );
        }
        finally
        {
            application.passivate();
        }
    }

    @Test
    public void canCreateAndQueryWithNativeRdfWithInMemoryStore()
        throws Exception
    {
        ApplicationSPI application = createApplication( nativeRdf, inMemoryStore, domain );
        try
        {
            application.activate();
            Module domain = application.findModule( "Domain", "Domain" );
            UnitOfWorkFactory unitOfWorkFactory = domain.unitOfWorkFactory();
            createABunchOfStuffAndDoQueries( unitOfWorkFactory, domain.queryBuilderFactory() );
        }
        finally
        {
            application.passivate();
        }
    }

    @Test
    public void canCreateAndQueryWithInMemoryRdfWithJdbm()
        throws Exception
    {
        ApplicationSPI application = createApplication( inMemoryRdf, jdbmStore, domain );
        try
        {
            application.activate();

            Module domain = application.findModule( "Domain", "Domain" );
            UnitOfWorkFactory unitOfWorkFactory = domain.unitOfWorkFactory();
            createABunchOfStuffAndDoQueries( unitOfWorkFactory, domain.queryBuilderFactory() );
        }
        finally
        {
            application.passivate();
        }
    }

    public void createABunchOfStuffAndDoQueries( UnitOfWorkFactory unitOfWorkFactory,
                                                 QueryBuilderFactory queryBuilderFactory
    )
        throws Exception
    {
        UnitOfWork uow = unitOfWorkFactory.newUnitOfWork();
        newItemType( uow, "Band" );
        newItemType( uow, "Bracelet" );
        newItemType( uow, "Necklace" );
        uow.complete();

        uow = unitOfWorkFactory.newUnitOfWork();
        QueryBuilder<ItemType> qb = queryBuilderFactory.newQueryBuilder( ItemType.class );
        Iterable<ItemType> initialList = copyOf( qb.newQuery( uow ) );

        assertTrue( "Band is not in the initial list", hasItemTypeNamed( "Band", initialList ) );
        assertTrue( "Bracelet is not in the initial list", hasItemTypeNamed( "Bracelet", initialList ) );
        assertTrue( "Necklace is not in the initial list", hasItemTypeNamed( "Necklace", initialList ) );

        newItemType( uow, "Watch" );
        uow.complete();

        uow = unitOfWorkFactory.newUnitOfWork();
        qb = queryBuilderFactory.newQueryBuilder( ItemType.class );
        Iterable<ItemType> listAfterFirstQueryAndAdd = copyOf( qb.newQuery( uow ) );

        assertTrue( "Band is not in the list after the first query and add", hasItemTypeNamed( "Band", listAfterFirstQueryAndAdd ) );
        assertTrue( "Bracelet is not in the list after the first query and add", hasItemTypeNamed( "Bracelet", listAfterFirstQueryAndAdd ) );
        assertTrue( "Necklace is not in the list after the first query and add", hasItemTypeNamed( "Necklace", listAfterFirstQueryAndAdd ) );
        assertTrue( "Watch is not in the list after the first query and add", hasItemTypeNamed( "Watch", listAfterFirstQueryAndAdd ) );

        newItemType( uow, "Ear ring" );
        uow.complete();

        uow = unitOfWorkFactory.newUnitOfWork();
        Iterable<ItemType> finalList = copyOf( qb.newQuery( uow ) );
        assertTrue( "Band is not in the final list", hasItemTypeNamed( "Band", finalList ) );
        assertTrue( "Bracelet is not in the final list", hasItemTypeNamed( "Bracelet", finalList ) );
        assertTrue( "Necklace is not in the final list", hasItemTypeNamed( "Necklace", finalList ) );
        assertTrue( "Watch is not in the final list", hasItemTypeNamed( "Watch", finalList ) );
        assertTrue( "Ear ring is not in the final list", hasItemTypeNamed( "Ear ring", finalList ) );
        uow.complete();
    }

    private ApplicationSPI createApplication( final ModuleAssemblyBuilder queryServiceModuleBuilder,
                                              final ModuleAssemblyBuilder entityStoreModuleBuilder,
                                              final LayerAssemblyBuilder domainLayerBuilder
    )
        throws AssemblyException
    {
        Energy4Java qi4j = new Energy4Java();
        ApplicationSPI application = qi4j.newApplication( new ApplicationAssembler()
        {
            public ApplicationAssembly assemble( ApplicationAssemblyFactory applicationFactory )
                throws AssemblyException
            {
                ApplicationAssembly applicationAssembly = applicationFactory.newApplicationAssembly();

                LayerAssembly configLayer = applicationAssembly.layer( "Config" );
                configModule.buildModuleAssembly( configLayer, "Configuration" );

                LayerAssembly infrastructureLayer = applicationAssembly.layer( "Infrastructure" );
                infrastructureLayer.uses( configLayer );

                queryServiceModuleBuilder.buildModuleAssembly( infrastructureLayer, "Query Service" );
                entityStoreModuleBuilder.buildModuleAssembly( infrastructureLayer, "Entity Store" );

                LayerAssembly domainLayer = domainLayerBuilder.buildLayerAssembly( applicationAssembly );
                domainLayer.uses( infrastructureLayer );
                return applicationAssembly;
            }
        } );
        return application;
    }

    interface LayerAssemblyBuilder
    {
        LayerAssembly buildLayerAssembly( ApplicationAssembly appAssembly )
            throws AssemblyException;
    }

    interface ModuleAssemblyBuilder
    {
        ModuleAssembly buildModuleAssembly( LayerAssembly layer, String name )
            throws AssemblyException;
    }

    final ModuleAssemblyBuilder nativeRdf = new ModuleAssemblyBuilder()
    {
        public ModuleAssembly buildModuleAssembly( LayerAssembly layer, String name )
            throws AssemblyException
        {
            return addModule( layer, name, new RdfNativeSesameStoreAssembler() );
        }
    };

    final ModuleAssemblyBuilder inMemoryStore = new ModuleAssemblyBuilder()
    {
        public ModuleAssembly buildModuleAssembly( LayerAssembly layer, String name )
            throws AssemblyException
        {
            return addModule( layer, name, new EntityTestAssembler() );
        }
    };

    final ModuleAssemblyBuilder inMemoryRdf = new ModuleAssemblyBuilder()
    {
        public ModuleAssembly buildModuleAssembly( LayerAssembly layer, String name )
            throws AssemblyException
        {
            return addModule( layer, name, new RdfMemoryStoreAssembler() );
        }
    };

    final ModuleAssemblyBuilder jdbmStore = new ModuleAssemblyBuilder()
    {
        public ModuleAssembly buildModuleAssembly( LayerAssembly layer, String name )
            throws AssemblyException
        {
            return addModule( layer, name, jdbmEntityStoreAssembler() );
        }
    };

    final ModuleAssemblyBuilder configModule = new ModuleAssemblyBuilder()
    {
        public ModuleAssembly buildModuleAssembly( LayerAssembly layer, String name )
            throws AssemblyException
        {
            return addModule( layer, name, entityStoreConfigAssembler() );
        }
    };

    final LayerAssemblyBuilder domain = new LayerAssemblyBuilder()
    {
        public LayerAssembly buildLayerAssembly( ApplicationAssembly appAssembly )
            throws AssemblyException
        {
            LayerAssembly domainLayer = appAssembly.layer( "Domain" );
            addModule( domainLayer, "Domain", new Assembler()
            {
                @SuppressWarnings( "unchecked" )
                public void assemble( ModuleAssembly module )
                    throws AssemblyException
                {
                    module.entities(
                        ItemTypeEntity.class
                    );
                }
            } );
            return domainLayer;
        }
    };

    private Assembler entityStoreConfigAssembler()
    {
        return new Assembler()
        {
            public void assemble( ModuleAssembly module )
                throws AssemblyException
            {
                new EntityTestAssembler( Visibility.module ).assemble( module );

                module.entities( NativeConfiguration.class ).visibleIn( Visibility.application );
                module.forMixin( NativeConfiguration.class )
                    .declareDefaults()
                    .dataDirectory()
                    .set( rdfDirectory().getAbsolutePath() );

                module.entities( JdbmConfiguration.class ).visibleIn( Visibility.application );
                module.forMixin( JdbmConfiguration.class )
                    .declareDefaults()
                    .file()
                    .set( jdbmDirectory().getAbsolutePath() );
            }
        };
    }

    private Assembler jdbmEntityStoreAssembler()
    {
        return new Assembler()
        {
            @SuppressWarnings( "unchecked" )
            public void assemble( ModuleAssembly module )
                throws AssemblyException
            {
                new JdbmEntityStoreAssembler( Visibility.application ).assemble( module );
            }
        };
    }

    private ModuleAssembly addModule( LayerAssembly layerAssembly, String name, Assembler assembler )
        throws AssemblyException
    {
        ModuleAssembly moduleAssembly = layerAssembly.module( name );
        assembler.assemble( moduleAssembly );
        return moduleAssembly;
    }

    private File rdfDirectory()
    {
        return createTempDirectory( "rdf-", "-index" );
    }

    private File jdbmDirectory()
    {
        return createTempDirectory( "jdbm-", "-store" );
    }

    private File createTempDirectory( String prefix, String suffix )
    {
        String tempDir = System.getProperty( "java.io.tmpdir" );
        String dirName = prefix + Integer.toHexString( new Random().nextInt() & 0xffff ) + suffix;
        File t = new File( tempDir, dirName );
        t.mkdirs();
        return t;
    }

    boolean hasItemTypeNamed( String name, Iterable<ItemType> list )
    {
        for( ItemType i : list )
        {
            Property<String> nameProperty = i.name();
            String entityName = nameProperty.get();
            if( entityName.equals( name ) )
            {
                return true;
            }
        }
        return false;
    }

    private Iterable<ItemType> copyOf( Iterable<ItemType> iterable )
    {
        Collection<ItemType> copy = new ArrayList<ItemType>();
        for( ItemType i : iterable )
        {
            copy.add( i );
        }
        return Collections.unmodifiableCollection( copy );
    }

    private ItemType newItemType( UnitOfWork uow, String name )
    {
        EntityBuilder<ItemType> builder = uow.newEntityBuilder( ItemType.class );
        builder.instance().name().set( name );
        return builder.newInstance();
    }

    interface ItemType
    {
        Property<String> name();
    }

    interface ItemTypeEntity
        extends ItemType, EntityComposite
    {
    }
}
