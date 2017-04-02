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

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.apache.polygene.api.common.Visibility;
import org.apache.polygene.api.entity.EntityBuilder;
import org.apache.polygene.api.entity.EntityComposite;
import org.apache.polygene.api.property.Property;
import org.apache.polygene.api.query.Query;
import org.apache.polygene.api.query.QueryBuilder;
import org.apache.polygene.api.query.QueryExpressions;
import org.apache.polygene.api.unitofwork.UnitOfWork;
import org.apache.polygene.api.value.ValueBuilder;
import org.apache.polygene.api.value.ValueBuilderFactory;
import org.apache.polygene.api.value.ValueComposite;
import org.apache.polygene.bootstrap.AssemblyException;
import org.apache.polygene.bootstrap.ModuleAssembly;
import org.apache.polygene.index.rdf.assembly.RdfNativeSesameStoreAssembler;
import org.apache.polygene.library.fileconfig.FileConfigurationAssembler;
import org.apache.polygene.library.fileconfig.FileConfigurationOverride;
import org.apache.polygene.library.rdf.repository.NativeConfiguration;
import org.apache.polygene.test.AbstractPolygeneTest;
import org.apache.polygene.test.EntityTestAssembler;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

// A test to verify that containsAll QueryExpression works properly.
public class ContainsAllTest
    extends AbstractPolygeneTest
{
    @Rule
    public TemporaryFolder tmpDir = new TemporaryFolder();

    public static final String TEST_STRING_1 = "TestString1";
    public static final String TEST_STRING_2 = "Some\\Weird\"$String/[]";
    public static final String TEST_STRING_3 = "TestString3";
    public static final String TEST_STRING_4 = "TestSTring4";

    public interface ExampleValue2
        extends ValueComposite
    {
        Property<String> stringProperty();
    }

    public interface ExampleValue
        extends ValueComposite
    {
        Property<ExampleValue2> valueProperty();
    }

    public interface ExampleEntity
        extends EntityComposite
    {
        Property<Set<String>> strings();

        Property<Set<ExampleValue>> complexValue();
    }

    // This test creates a one-layer, two-module application, with one module
    // being testing module, and another for retrieving configuration for
    // services from preferences. This test assumes that those configurations
    // already exist in preference ES.
    @Override
    public void assemble( ModuleAssembly module )
        throws AssemblyException
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

    public static ExampleEntity createEntityWithStrings( UnitOfWork uow, ValueBuilderFactory vbf, String... strings )
    {
        EntityBuilder<ExampleEntity> builder = uow.newEntityBuilder( ExampleEntity.class );

        populateStrings( builder.instance(), strings );
        populateComplexValue( builder.instance(), vbf );

        return builder.newInstance();
    }

    public static ExampleEntity createEntityWithComplexValues( UnitOfWork uow,
                                                               ValueBuilderFactory vbf,
                                                               String... valueStrings
    )
    {
        EntityBuilder<ExampleEntity> builder = uow.newEntityBuilder( ExampleEntity.class );

        populateStrings( builder.instance() );
        populateComplexValue( builder.instance(), vbf, valueStrings );

        return builder.newInstance();
    }

    private static void populateStrings( ExampleEntity proto, String... strings )
    {
        proto.strings().set( setOf( strings ) );
    }

    private static void populateComplexValue( ExampleEntity proto, ValueBuilderFactory vbf, String... valueStrings )
    {
        Set<ExampleValue> values = new HashSet<>();
        for( String value : valueStrings )
        {
            ValueBuilder<ExampleValue2> vBuilder = vbf.newValueBuilder( ExampleValue2.class );
            vBuilder.prototype().stringProperty().set( value );

            ValueBuilder<ExampleValue> vBuilder2 = vbf.newValueBuilder( ExampleValue.class );
            vBuilder2.prototype().valueProperty().set( vBuilder.newInstance() );
            values.add( vBuilder2.newInstance() );
        }

        proto.complexValue().set( values );
    }

    @Test
    public void simpleContainsAllQuerySuccessTest()
        throws Exception
    {
        ExampleEntity result = this.performContainsAllStringsTest(
            setOf( TEST_STRING_1, TEST_STRING_2, TEST_STRING_3 ),
            setOf( TEST_STRING_1, TEST_STRING_2 )
        );

        Assert.assertTrue( "The entity must have been found.", result != null );
    }

    @Test
    public void fullContainsAllQuerySuccessTest()
        throws Exception
    {
        ExampleEntity result = this.performContainsAllStringsTest(
            setOf( TEST_STRING_1, TEST_STRING_2, TEST_STRING_3 ),
            setOf( TEST_STRING_1, TEST_STRING_2, TEST_STRING_3 )
        );

        Assert.assertTrue( "The entity must have been found.", result != null );
    }

    @Test
    public void simpleContainsAllQueryFailTest()
        throws Exception
    {
        ExampleEntity result = this.performContainsAllStringsTest(
            setOf( TEST_STRING_1, TEST_STRING_2, TEST_STRING_3 ),
            setOf( TEST_STRING_1, TEST_STRING_2, TEST_STRING_3, TEST_STRING_4 )
        );

        Assert.assertTrue( "The entity must not have been found.", result == null );
    }

    @Test
    public void simpleContainsAllQueryWithNullsTest()
        throws Exception
    {
        ExampleEntity result = this.performContainsAllStringsTest(
            setOf( TEST_STRING_1, TEST_STRING_2, TEST_STRING_3 ),
            setOf( TEST_STRING_1, null, TEST_STRING_2 )
        );

        Assert.assertTrue( "The entity must have been found.", result != null );
    }

    @Test
    public void emptyContainsAllQueryTest()
        throws Exception
    {
        ExampleEntity result = this.performContainsAllStringsTest(
            setOf( TEST_STRING_1, TEST_STRING_2 ),
            setOf()
        );

        Assert.assertTrue( "The entity must have been found.", result != null );
    }

    @Test
    public void complexContainsAllSuccessTest()
        throws Exception
    {
        ExampleEntity result = this.performContainsAllStringValueTest(
            setOf( TEST_STRING_1, TEST_STRING_2 ),
            setOf( TEST_STRING_1 )
        );

        Assert.assertTrue( "The entity must have been found.", result != null );
    }

    @Test
    public void fullComplexContainsAllSuccessTest()
        throws Exception
    {
        ExampleEntity result = this.performContainsAllStringValueTest(
            setOf( TEST_STRING_1, TEST_STRING_2 ),
            setOf( TEST_STRING_1, TEST_STRING_2 )
        );

        Assert.assertTrue( "The entity must have been found", result != null );
    }

    @Test
    public void complexContainsAllFailTest()
        throws Exception
    {
        ExampleEntity result = this.performContainsAllStringValueTest(
            setOf( TEST_STRING_1, TEST_STRING_2 ),
            setOf( TEST_STRING_1, TEST_STRING_2, TEST_STRING_3 )
        );

        Assert.assertTrue( "The entity must not have been found.", result == null );
    }

    @Test
    public void complexEmptyContainsAllTest()
        throws Exception
    {
        ExampleEntity result = this.performContainsAllStringValueTest(
            setOf( TEST_STRING_1, TEST_STRING_2 ),
            setOf()
        );

        Assert.assertTrue( "The entity must have been found.", result != null );
    }

    private ExampleEntity findEntity( String... strings )
    {
        QueryBuilder<ExampleEntity> builder = this.queryBuilderFactory.newQueryBuilder( ExampleEntity.class );

        builder = builder.where( QueryExpressions.containsAll(
            QueryExpressions.templateFor( ExampleEntity.class ).strings(),
            Arrays.asList( strings ) ) );
        return this.unitOfWorkFactory.currentUnitOfWork().newQuery( builder ).find();
    }

    private ExampleEntity findEntityBasedOnValueStrings( String... valueStrings )
    {
        Set<ExampleValue> values = new HashSet<ExampleValue>();
        for( String value : valueStrings )
        {
            ValueBuilder<ExampleValue2> vBuilder = this.valueBuilderFactory.newValueBuilder( ExampleValue2.class );
            vBuilder.prototype().stringProperty().set( value );

            ValueBuilder<ExampleValue> vBuilder2 = this.valueBuilderFactory.newValueBuilder( ExampleValue.class );
            vBuilder2.prototype().valueProperty().set( vBuilder.newInstance() );
            values.add( vBuilder2.newInstance() );
        }

        return this.createComplexQuery( values ).find();
    }

    private Query<ExampleEntity> createComplexQuery( Set<ExampleValue> valuez )
    {
        QueryBuilder<ExampleEntity> builder = this.queryBuilderFactory.newQueryBuilder( ExampleEntity.class );
        builder = builder.where( QueryExpressions.containsAll(
            QueryExpressions.templateFor( ExampleEntity.class ).complexValue(),
            valuez
                                 )
        );

        return this.unitOfWorkFactory.currentUnitOfWork().newQuery( builder );
    }

    private ExampleEntity performContainsAllStringsTest( Set<String> entityStrings, Set<String> queryableStrings )
        throws Exception
    {
        UnitOfWork creatingUOW = this.unitOfWorkFactory.newUnitOfWork();
        String[] entityStringsArray = new String[ entityStrings.size() ];
        createEntityWithStrings( creatingUOW, this.valueBuilderFactory, entityStrings.toArray( entityStringsArray ) );
        creatingUOW.complete();

        UnitOfWork queryingUOW = this.unitOfWorkFactory.newUnitOfWork();
        try
        {
            String[] queryableStringsArray = new String[ queryableStrings.size() ];
            ExampleEntity entity = this.findEntity( queryableStrings.toArray( queryableStringsArray ) );
            return entity;
        }
        finally
        {
            queryingUOW.discard();
        }
    }

    private ExampleEntity performContainsAllStringValueTest( Set<String> entityStrings, Set<String> queryableStrings )
        throws Exception
    {
        UnitOfWork creatingUOW = this.unitOfWorkFactory.newUnitOfWork();
        String[] entityStringsArray = new String[ entityStrings.size() ];
        createEntityWithComplexValues( creatingUOW, this.valueBuilderFactory,
                                       entityStrings.toArray( entityStringsArray ) );
        creatingUOW.complete();

        UnitOfWork queryingUOW = this.unitOfWorkFactory.newUnitOfWork();
        try
        {
            String[] queryableStringsArray = new String[ queryableStrings.size() ];
            ExampleEntity entity = this.findEntityBasedOnValueStrings(
                queryableStrings.toArray( queryableStringsArray ) );
            return entity;
        }
        finally
        {
            queryingUOW.discard();
        }
    }

    static <T> Set<T> setOf( T... elements )
    {
        return new HashSet<T>( Arrays.asList( elements ) );
    }
}
