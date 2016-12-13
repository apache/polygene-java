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
package org.apache.zest.index.rdf;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.apache.zest.api.common.Visibility;
import org.apache.zest.api.query.Query;
import org.apache.zest.api.query.QueryBuilder;
import org.apache.zest.api.query.QueryExpressions;
import org.apache.zest.api.unitofwork.UnitOfWork;
import org.apache.zest.api.value.ValueBuilder;
import org.apache.zest.bootstrap.AssemblyException;
import org.apache.zest.bootstrap.ModuleAssembly;
import org.apache.zest.index.rdf.ContainsAllTest.ExampleEntity;
import org.apache.zest.index.rdf.ContainsAllTest.ExampleValue;
import org.apache.zest.index.rdf.ContainsAllTest.ExampleValue2;
import org.apache.zest.index.rdf.assembly.RdfNativeSesameStoreAssembler;
import org.apache.zest.library.fileconfig.FileConfigurationAssembler;
import org.apache.zest.library.fileconfig.FileConfigurationOverride;
import org.apache.zest.library.rdf.repository.NativeConfiguration;
import org.apache.zest.test.AbstractPolygeneTest;
import org.apache.zest.test.EntityTestAssembler;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class ContainsTest extends AbstractPolygeneTest
{
    @Rule
    public TemporaryFolder tmpDir = new TemporaryFolder();

    @Override
    public void assemble( ModuleAssembly module ) throws AssemblyException
    {
        new FileConfigurationAssembler()
            .withOverride( new FileConfigurationOverride().withConventionalRoot( tmpDir.getRoot() ) )
            .assemble( module );
        ModuleAssembly prefModule = module.layer().module( "PrefModule" );
        prefModule.entities( NativeConfiguration.class ).visibleIn( Visibility.application );
        prefModule.forMixin( NativeConfiguration.class ).declareDefaults()
                  .dataDirectory().set( new File( tmpDir.getRoot(), "rdf-data" ).getAbsolutePath() );
        new EntityTestAssembler().assemble( prefModule );

        module.entities( ExampleEntity.class );
        module.values( ExampleValue.class, ExampleValue2.class );

        EntityTestAssembler testAss = new EntityTestAssembler();
        testAss.assemble( module );

        RdfNativeSesameStoreAssembler rdfAssembler = new RdfNativeSesameStoreAssembler();
        rdfAssembler.assemble( module );
    }

    @Test
    public void simpleContainsSuccessTest() throws Exception
    {
        ExampleEntity result = this.performContainsStringTest(
            new HashSet<>( Arrays.asList(
                ContainsAllTest.TEST_STRING_1, ContainsAllTest.TEST_STRING_2, ContainsAllTest.TEST_STRING_3
            ) ),
            ContainsAllTest.TEST_STRING_3
        );

        Assert.assertTrue( "The entity must have been found", result != null );
    }

    @Test
    public void simpleContainsSuccessFailTest() throws Exception
    {
        ExampleEntity result = this.performContainsStringTest(
            new HashSet<>( Arrays.asList(
                ContainsAllTest.TEST_STRING_1, ContainsAllTest.TEST_STRING_2, ContainsAllTest.TEST_STRING_3
            ) ),
            ContainsAllTest.TEST_STRING_4
        );

        Assert.assertTrue( "The entity must not have been found", result == null );
    }

    @Test( expected = IllegalArgumentException.class )
    public void simplecontainsNullTest() throws Exception
    {
        this.performContainsStringTest(
            new HashSet<>( Arrays.asList(
                ContainsAllTest.TEST_STRING_1, ContainsAllTest.TEST_STRING_2, ContainsAllTest.TEST_STRING_3
            ) ),
            null
        );
    }

    @Test
    public void simpleContainsStringValueSuccessTest() throws Exception
    {
        ExampleEntity result = this.performContainsStringValueTest(
            new HashSet<>( Arrays.asList(
                ContainsAllTest.TEST_STRING_1, ContainsAllTest.TEST_STRING_2, ContainsAllTest.TEST_STRING_3
            ) ),
            ContainsAllTest.TEST_STRING_3
        );

        Assert.assertTrue( "The entity must have been found", result != null );
    }

    @Test
    public void simpleContainsStringValueFailTest() throws Exception
    {
        ExampleEntity result = this.performContainsStringTest(
            new HashSet<>( Arrays.asList(
                ContainsAllTest.TEST_STRING_1, ContainsAllTest.TEST_STRING_2, ContainsAllTest.TEST_STRING_3
            ) ),
            ContainsAllTest.TEST_STRING_4
        );

        Assert.assertTrue( "The entity must not have been found", result == null );
    }

    private ExampleEntity findEntity( String string )
    {
        QueryBuilder<ExampleEntity> builder = this.queryBuilderFactory.newQueryBuilder( ExampleEntity.class );

        builder = builder.where( QueryExpressions.contains(
            QueryExpressions.templateFor( ExampleEntity.class ).strings(),
            string
                                 )
        );
        return this.unitOfWorkFactory.currentUnitOfWork().newQuery( builder ).find();
    }

    private ExampleEntity findEntityBasedOnValueString( String valueString )
    {
        ValueBuilder<ExampleValue2> vBuilder = this.valueBuilderFactory.newValueBuilder( ExampleValue2.class );
        vBuilder.prototype().stringProperty().set( valueString );

        ValueBuilder<ExampleValue> vBuilder2 = this.valueBuilderFactory.newValueBuilder( ExampleValue.class );
        vBuilder2.prototype().valueProperty().set( vBuilder.newInstance() );

        return this.createComplexQuery( vBuilder2.newInstance() ).find();
    }

    private Query<ExampleEntity> createComplexQuery( ExampleValue value )
    {
        QueryBuilder<ExampleEntity> builder = this.queryBuilderFactory.newQueryBuilder( ExampleEntity.class );
        builder = builder.where( QueryExpressions.contains(
            QueryExpressions.templateFor( ExampleEntity.class ).complexValue(),
            value
                                 )
        );

        return this.unitOfWorkFactory.currentUnitOfWork().newQuery( builder );
    }

    private ExampleEntity performContainsStringTest( Set<String> entityStrings, String queryableString )
        throws Exception
    {
        UnitOfWork creatingUOW = this.unitOfWorkFactory.newUnitOfWork();
        String[] entityStringsArray = new String[ entityStrings.size() ];
        ContainsAllTest.createEntityWithStrings( creatingUOW, this.valueBuilderFactory,
                                                 entityStrings.toArray( entityStringsArray ) );
        creatingUOW.complete();

        UnitOfWork queryingUOW = this.unitOfWorkFactory.newUnitOfWork();
        try
        {
            return this.findEntity( queryableString );
        }
        finally
        {
            queryingUOW.discard();
        }
    }

    private ExampleEntity performContainsStringValueTest( Set<String> entityStrings, String queryableString )
        throws Exception
    {
        UnitOfWork creatingUOW = this.unitOfWorkFactory.newUnitOfWork();
        String[] entityStringsArray = new String[ entityStrings.size() ];
        ContainsAllTest.createEntityWithComplexValues( creatingUOW, this.valueBuilderFactory,
                                                       entityStrings.toArray( entityStringsArray ) );
        creatingUOW.complete();

        UnitOfWork queryingUOW = this.unitOfWorkFactory.newUnitOfWork();
        try
        {
            return this.findEntityBasedOnValueString( queryableString );
        }
        finally
        {
            queryingUOW.discard();
        }
    }
}
