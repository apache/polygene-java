package org.qi4j.index.rdf;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.junit.Assert;
import org.junit.Test;
import org.qi4j.api.common.Visibility;
import org.qi4j.api.entity.EntityBuilder;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.property.Property;
import org.qi4j.api.query.Query;
import org.qi4j.api.query.QueryBuilder;
import org.qi4j.api.query.QueryExpressions;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import org.qi4j.api.value.ValueComposite;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.entitystore.memory.MemoryEntityStoreService;
import org.qi4j.entitystore.prefs.assembly.PreferenceEntityStoreAssembler;
import org.qi4j.index.rdf.assembly.RdfNativeSesameStoreAssembler;
import org.qi4j.library.fileconfig.FileConfiguration;
import org.qi4j.library.rdf.repository.NativeConfiguration;
import org.qi4j.test.AbstractQi4jTest;
import org.qi4j.test.EntityTestAssembler;

// A test to verify that containsAll QueryExpression works properly.
public class ContainsAllTest
    extends AbstractQi4jTest
{

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
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.services( FileConfiguration.class );
        ModuleAssembly prefModule = module.layer().module( "PrefModule" );
        prefModule.entities( NativeConfiguration.class ).visibleIn( Visibility.application );
        prefModule.services( MemoryEntityStoreService.class );

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
        proto.strings().set( new HashSet<String>( Arrays.asList( strings ) ) );
    }

    private static void populateComplexValue( ExampleEntity proto, ValueBuilderFactory vbf, String... valueStrings )
    {
        Set<ExampleValue> values = new HashSet<ExampleValue>();
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
            new HashSet<String>( Arrays.asList(
                TEST_STRING_1, TEST_STRING_2, TEST_STRING_3
            )
            ),
            new HashSet<String>( Arrays.asList(
                TEST_STRING_1, TEST_STRING_2
            )
            )
        );

        Assert.assertTrue( "The entity must have been found.", result != null );
    }

    @Test
    public void fullContainsAllQuerySuccessTest()
        throws Exception
    {
        ExampleEntity result = this.performContainsAllStringsTest(
            new HashSet<String>( Arrays.asList(
                TEST_STRING_1, TEST_STRING_2, TEST_STRING_3
            )
            ),
            new HashSet<String>( Arrays.asList(
                TEST_STRING_1, TEST_STRING_2, TEST_STRING_3
            )
            )
        );

        Assert.assertTrue( "The entity must have been found.", result != null );
    }

    @Test
    public void simpleContainsAllQueryFailTest()
        throws Exception
    {
        ExampleEntity result = this.performContainsAllStringsTest(
            new HashSet<String>( Arrays.asList(
                TEST_STRING_1, TEST_STRING_2, TEST_STRING_3
            )
            ),
            new HashSet<String>( Arrays.asList(
                TEST_STRING_1, TEST_STRING_2, TEST_STRING_3, TEST_STRING_4
            )
            )
        );

        Assert.assertTrue( "The entity must not have been found.", result == null );
    }

    @Test
    public void simpleContainsAllQueryWithNullsTest()
        throws Exception
    {
        ExampleEntity result = this.performContainsAllStringsTest(
            new HashSet<String>( Arrays.asList(
                TEST_STRING_1, TEST_STRING_2, TEST_STRING_3
            )
            ),
            new HashSet<String>( Arrays.asList(
                TEST_STRING_1, null, TEST_STRING_2
            )
            )
        );

        Assert.assertTrue( "The entity must have been found.", result != null );
    }

    @Test
    public void emptyContainsAllQueryTest()
        throws Exception
    {
        ExampleEntity result = this.performContainsAllStringsTest(
            new HashSet<String>( Arrays.asList(
                TEST_STRING_1, TEST_STRING_2
            )
            ),
            new HashSet<String>()
        );

        Assert.assertTrue( "The entity must have been found.", result != null );
    }

    @Test
    public void complexContainsAllSuccessTest()
        throws Exception
    {
        ExampleEntity result = this.performContainsAllStringValueTest(
            new HashSet<String>( Arrays.asList(
                TEST_STRING_1, TEST_STRING_2
            )
            ),
            new HashSet<String>( Arrays.asList(
                TEST_STRING_1
            )
            )
        );

        Assert.assertTrue( "The entity must have been found.", result != null );
    }

    @Test
    public void fullComplexContainsAllSuccessTest()
        throws Exception
    {
        ExampleEntity result = this.performContainsAllStringValueTest(
            new HashSet<String>( Arrays.asList(
                TEST_STRING_1, TEST_STRING_2
            )
            ),
            new HashSet<String>( Arrays.asList(
                TEST_STRING_1, TEST_STRING_2
            )
            )
        );

        Assert.assertTrue( "The entity must have been found", result != null );
    }

    @Test
    public void complexContainsAllFailTest()
        throws Exception
    {
        ExampleEntity result = this.performContainsAllStringValueTest(
            new HashSet<String>( Arrays.asList(
                TEST_STRING_1, TEST_STRING_2
            )
            ),
            new HashSet<String>( Arrays.asList(
                TEST_STRING_1, TEST_STRING_2, TEST_STRING_3
            )
            )
        );

        Assert.assertTrue( "The entity must not have been found.", result == null );
    }

    @Test
    public void complexEmptyContainsAllTest()
        throws Exception
    {
        ExampleEntity result = this.performContainsAllStringValueTest(
            new HashSet<String>( Arrays.asList(
                TEST_STRING_1, TEST_STRING_2
            )
            ),
            new HashSet<String>()
        );

        Assert.assertTrue( "The entity must have been found.", result != null );
    }

    private ExampleEntity findEntity( String... strings )
    {
        QueryBuilder<ExampleEntity> builder = this.queryBuilderFactory.newQueryBuilder( ExampleEntity.class );

        builder = builder.where( QueryExpressions.containsAll(
            QueryExpressions.templateFor( ExampleEntity.class ).strings(),
            new HashSet<String>( Arrays.asList( strings ) ) )
        );
        return builder.newQuery( this.unitOfWorkFactory.currentUnitOfWork() ).find();
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

        return builder.newQuery( this.unitOfWorkFactory.currentUnitOfWork() );
    }

    private ExampleEntity performContainsAllStringsTest( Set<String> entityStrings, Set<String> queryableStrings )
        throws Exception
    {
        UnitOfWork creatingUOW = this.unitOfWorkFactory.newUnitOfWork();
        String[] entityStringsArray = new String[entityStrings.size()];
        createEntityWithStrings( creatingUOW, this.valueBuilderFactory, entityStrings.toArray( entityStringsArray ) );
        creatingUOW.complete();

        UnitOfWork queryingUOW = this.unitOfWorkFactory.newUnitOfWork();
        try
        {
            String[] queryableStringsArray = new String[queryableStrings.size()];
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
        String[] entityStringsArray = new String[entityStrings.size()];
        createEntityWithComplexValues( creatingUOW, this.valueBuilderFactory, entityStrings.toArray( entityStringsArray ) );
        creatingUOW.complete();

        UnitOfWork queryingUOW = this.unitOfWorkFactory.newUnitOfWork();
        try
        {
            String[] queryableStringsArray = new String[queryableStrings.size()];
            ExampleEntity entity = this.findEntityBasedOnValueStrings( queryableStrings.toArray( queryableStringsArray ) );
            return entity;
        }
        finally
        {
            queryingUOW.discard();
        }
    }
}
