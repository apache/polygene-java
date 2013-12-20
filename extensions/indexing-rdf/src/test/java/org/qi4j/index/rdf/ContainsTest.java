package org.qi4j.index.rdf;

import java.io.File;
import org.junit.Assert;
import org.junit.Test;
import org.qi4j.api.common.Visibility;
import org.qi4j.api.query.Query;
import org.qi4j.api.query.QueryBuilder;
import org.qi4j.api.query.QueryExpressions;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.index.rdf.ContainsAllTest.ExampleEntity;
import org.qi4j.index.rdf.ContainsAllTest.ExampleValue;
import org.qi4j.index.rdf.ContainsAllTest.ExampleValue2;
import org.qi4j.index.rdf.assembly.RdfNativeSesameStoreAssembler;
import org.qi4j.library.fileconfig.FileConfigurationService;
import org.qi4j.library.rdf.repository.NativeConfiguration;
import org.qi4j.test.AbstractQi4jTest;
import org.qi4j.test.EntityTestAssembler;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.junit.Rule;
import org.qi4j.test.util.DelTreeAfter;

public class ContainsTest extends AbstractQi4jTest
{

   private static final File DATA_DIR = new File( "build/tmp/contains-test" );
   @Rule
   public final DelTreeAfter delTreeAfter = new DelTreeAfter( DATA_DIR );

   @Override
   public void assemble(ModuleAssembly module) throws AssemblyException
   {
       module.services( FileConfigurationService.class );
      ModuleAssembly prefModule = module.layer().module( "PrefModule" );
      prefModule.entities( NativeConfiguration.class ).visibleIn(Visibility.application);
      prefModule.forMixin( NativeConfiguration.class ).declareDefaults().dataDirectory().set( DATA_DIR.getAbsolutePath() );
      new EntityTestAssembler().assemble( prefModule );

      module.entities( ExampleEntity.class );
      module.values( ExampleValue.class, ExampleValue2.class );

      EntityTestAssembler testAss = new EntityTestAssembler();
      testAss.assemble(module);

      RdfNativeSesameStoreAssembler rdfAssembler = new RdfNativeSesameStoreAssembler();
      rdfAssembler.assemble(module);
   }

   @Test
   public void simpleContainsSuccessTest() throws Exception
   {
      ExampleEntity result = this.performContainsStringTest(
            new HashSet<String>(Arrays.asList(
                  ContainsAllTest.TEST_STRING_1, ContainsAllTest.TEST_STRING_2, ContainsAllTest.TEST_STRING_3
                  )),
            ContainsAllTest.TEST_STRING_3
            );

      Assert.assertTrue("The entity must have been found", result != null);
   }

   @Test
   public void simpleContainsSuccessFailTest() throws Exception
   {
      ExampleEntity result = this.performContainsStringTest(
            new HashSet<String>(Arrays.asList(
                  ContainsAllTest.TEST_STRING_1, ContainsAllTest.TEST_STRING_2, ContainsAllTest.TEST_STRING_3
                  )),
            ContainsAllTest.TEST_STRING_4
            );

      Assert.assertTrue("The entity must not have been found", result == null);
   }

   @Test(expected = IllegalArgumentException.class)
   public void simplecontainsNullTest() throws Exception
   {
      this.performContainsStringTest(
            new HashSet<String>(Arrays.asList(
                  ContainsAllTest.TEST_STRING_1, ContainsAllTest.TEST_STRING_2, ContainsAllTest.TEST_STRING_3
                  )),
            null
            );

   }

   @Test
   public void simpleContainsStringValueSuccessTest() throws Exception
   {
      ExampleEntity result = this.performContainsStringValueTest(
            new HashSet<String>(Arrays.asList(
                  ContainsAllTest.TEST_STRING_1, ContainsAllTest.TEST_STRING_2, ContainsAllTest.TEST_STRING_3
                  )),
            ContainsAllTest.TEST_STRING_3
            );

      Assert.assertTrue("The entity must have been found", result != null);
   }

   @Test
   public void simpleContainsStringValueFailTest() throws Exception
   {
      ExampleEntity result = this.performContainsStringTest(
            new HashSet<String>(Arrays.asList(
                  ContainsAllTest.TEST_STRING_1, ContainsAllTest.TEST_STRING_2, ContainsAllTest.TEST_STRING_3
                  )),
            ContainsAllTest.TEST_STRING_4
            );

      Assert.assertTrue("The entity must not have been found", result == null);
   }

   private ExampleEntity findEntity(String string)
   {
      QueryBuilder<ExampleEntity> builder = this.module.newQueryBuilder(ExampleEntity.class);

      builder = builder.where(QueryExpressions.contains(
            QueryExpressions.templateFor(ExampleEntity.class).strings(),
            string
            )
      );
      return this.module.currentUnitOfWork().newQuery( builder ).find();

   }

   private ExampleEntity findEntityBasedOnValueString(String valueString)
   {
      ValueBuilder<ExampleValue2> vBuilder = this.module.newValueBuilder(ExampleValue2.class);
         vBuilder.prototype().stringProperty().set(valueString);

         ValueBuilder<ExampleValue> vBuilder2 = this.module.newValueBuilder(ExampleValue.class);
         vBuilder2.prototype().valueProperty().set(vBuilder.newInstance());

      return this.createComplexQuery(vBuilder2.newInstance()).find();
   }

   private Query<ExampleEntity> createComplexQuery(ExampleValue value)
   {
      QueryBuilder<ExampleEntity> builder = this.module.newQueryBuilder(ExampleEntity.class);
      builder = builder.where(QueryExpressions.contains(
            QueryExpressions.templateFor(ExampleEntity.class).complexValue(),
            value
            )
         );

      return this.module.currentUnitOfWork().newQuery( builder);
   }

   private ExampleEntity performContainsStringTest(Set<String> entityStrings, String queryableString) throws Exception
   {
      UnitOfWork creatingUOW = this.module.newUnitOfWork();
      String[] entityStringsArray = new String[entityStrings.size()];
      ContainsAllTest.createEntityWithStrings(creatingUOW, this.module, entityStrings.toArray(entityStringsArray));
      creatingUOW.complete();

      UnitOfWork queryingUOW = this.module.newUnitOfWork();
      try
      {
         ExampleEntity entity = this.findEntity(queryableString);
         return entity;
      }
      finally
      {
         queryingUOW.discard();
      }
   }

   private ExampleEntity performContainsStringValueTest(Set<String> entityStrings, String queryableString) throws Exception
   {
      UnitOfWork creatingUOW = this.module.newUnitOfWork();
      String[] entityStringsArray = new String[entityStrings.size()];
      ContainsAllTest.createEntityWithComplexValues(creatingUOW, this.module, entityStrings.toArray(entityStringsArray));
      creatingUOW.complete();

      UnitOfWork queryingUOW = this.module.newUnitOfWork();
      try
      {
         ExampleEntity entity = this.findEntityBasedOnValueString(queryableString);
         return entity;
      }
      finally
      {
         queryingUOW.discard();
      }
   }
}
