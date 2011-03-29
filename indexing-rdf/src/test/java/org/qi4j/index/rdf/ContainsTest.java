package org.qi4j.index.rdf;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

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
import org.qi4j.entitystore.memory.MemoryEntityStoreService;
import org.qi4j.entitystore.prefs.assembly.PreferenceEntityStoreAssembler;
import org.qi4j.index.rdf.ContainsAllTest.ExampleEntity;
import org.qi4j.index.rdf.ContainsAllTest.ExampleValue;
import org.qi4j.index.rdf.ContainsAllTest.ExampleValue2;
import org.qi4j.index.rdf.assembly.RdfNativeSesameStoreAssembler;
import org.qi4j.library.fileconfig.FileConfiguration;
import org.qi4j.library.rdf.repository.NativeConfiguration;
import org.qi4j.test.AbstractQi4jTest;
import org.qi4j.test.EntityTestAssembler;

public class ContainsTest extends AbstractQi4jTest
{
   public void assemble(ModuleAssembly module) throws AssemblyException
   {
       module.services( FileConfiguration.class );
      ModuleAssembly prefModule = module.layer().module( "PrefModule" );
      prefModule.entities( NativeConfiguration.class ).visibleIn(Visibility.application);
       prefModule.services( MemoryEntityStoreService.class );

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
      QueryBuilder<ExampleEntity> builder = this.queryBuilderFactory.newQueryBuilder(ExampleEntity.class);
      
      builder = builder.where(QueryExpressions.contains(
            QueryExpressions.templateFor(ExampleEntity.class).strings(),
            string
            )
      );
      return builder.newQuery(this.unitOfWorkFactory.currentUnitOfWork()).find();
      
   }
   
   private ExampleEntity findEntityBasedOnValueString(String valueString)
   {
      ValueBuilder<ExampleValue2> vBuilder = this.valueBuilderFactory.newValueBuilder(ExampleValue2.class);
         vBuilder.prototype().stringProperty().set(valueString);
         
         ValueBuilder<ExampleValue> vBuilder2 = this.valueBuilderFactory.newValueBuilder(ExampleValue.class);
         vBuilder2.prototype().valueProperty().set(vBuilder.newInstance());
      
      return this.createComplexQuery(vBuilder2.newInstance()).find();
   }
   
   private Query<ExampleEntity> createComplexQuery(ExampleValue value)
   {
      QueryBuilder<ExampleEntity> builder = this.queryBuilderFactory.newQueryBuilder(ExampleEntity.class);
      builder = builder.where(QueryExpressions.contains(
            QueryExpressions.templateFor(ExampleEntity.class).complexValue(),
            value
            )
         );
      
      return builder.newQuery(this.unitOfWorkFactory.currentUnitOfWork());
   }
   
   private ExampleEntity performContainsStringTest(Set<String> entityStrings, String queryableString) throws Exception
   {
      UnitOfWork creatingUOW = this.unitOfWorkFactory.newUnitOfWork();
      String[] entityStringsArray = new String[entityStrings.size()];
      ContainsAllTest.createEntityWithStrings(creatingUOW, this.valueBuilderFactory, entityStrings.toArray(entityStringsArray));
      creatingUOW.complete();
      
      UnitOfWork queryingUOW = this.unitOfWorkFactory.newUnitOfWork();
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
      UnitOfWork creatingUOW = this.unitOfWorkFactory.newUnitOfWork();
      String[] entityStringsArray = new String[entityStrings.size()];
      ContainsAllTest.createEntityWithComplexValues(creatingUOW, this.valueBuilderFactory, entityStrings.toArray(entityStringsArray));
      creatingUOW.complete();
      
      UnitOfWork queryingUOW = this.unitOfWorkFactory.newUnitOfWork();
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
