/*  Copyright 2008 Rickard Öberg.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package org.qi4j.entity.jgroups;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import org.junit.Ignore;
import org.junit.Test;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.bootstrap.SingletonAssembler;
import org.qi4j.composite.Composite;
import org.qi4j.composite.CompositeBuilder;
import org.qi4j.composite.CompositeBuilderFactory;
import org.qi4j.composite.Mixins;
import org.qi4j.entity.EntityBuilder;
import org.qi4j.entity.EntityComposite;
import org.qi4j.entity.EntityCompositeNotFoundException;
import org.qi4j.entity.UnitOfWork;
import org.qi4j.entity.UnitOfWorkCompletionException;
import org.qi4j.entity.association.Association;
import org.qi4j.entity.association.ListAssociation;
import org.qi4j.entity.association.ManyAssociation;
import org.qi4j.entity.association.SetAssociation;
import org.qi4j.injection.scope.Structure;
import org.qi4j.injection.scope.This;
import org.qi4j.property.ImmutableProperty;
import org.qi4j.property.Property;
import org.qi4j.spi.entity.UuidIdentityGeneratorService;
import org.qi4j.test.AbstractQi4jTest;

/**
 * Test of JGroups EntityStore backend.
 */
public class JGroupsEntityStoreTest
    extends AbstractQi4jTest
{
    public void assemble( ModuleAssembly module ) throws AssemblyException
    {
        module.addServices( UuidIdentityGeneratorService.class );
        module.addComposites( TestEntity.class, TestValue.class );
        module.addServices( JGroupsEntityStoreService.class );
    }

    @Ignore
    @Test
    public void whenNewEntityThenFindInReplica()
        throws Exception
    {
        // Create first app
        SingletonAssembler app1 = new SingletonAssembler()
        {
            public void assemble( ModuleAssembly module ) throws AssemblyException
            {
                module.addServices( JGroupsEntityStoreService.class, UuidIdentityGeneratorService.class ).instantiateOnStartup();
                module.addComposites( TestEntity.class );
            }
        };

        // Create second app
        SingletonAssembler app2 = new SingletonAssembler()
        {
            public void assemble( ModuleAssembly module ) throws AssemblyException
            {
                module.addServices( JGroupsEntityStoreService.class, UuidIdentityGeneratorService.class ).instantiateOnStartup();
                module.addComposites( TestEntity.class );
            }
        };

        // Create entity in app 1
        System.out.println( "Create entity" );
        UnitOfWork app1Unit = app1.unitOfWorkFactory().newUnitOfWork();
        TestEntity instance = app1Unit.newEntityBuilder( TestEntity.class ).newInstance();
        instance.name().set( "Foo" );
        app1Unit.complete();

//        Thread.sleep( 5000 );

        // Find entity in app 2
        System.out.println( "Find entity" );
        UnitOfWork app2Unit = app2.unitOfWorkFactory().newUnitOfWork();
        instance = app2Unit.dereference( instance );

        System.out.println( instance.name() );
        app2Unit.discard();

    }

    @Ignore
    @Test
    public void whenNewEntityThenCanFindEntity()
        throws Exception
    {
        try
        {
            UnitOfWork unitOfWork = unitOfWorkFactory.newUnitOfWork();
            TestEntity instance = createEntity( unitOfWork );
            unitOfWork.complete();

            // Find entity
            unitOfWork = unitOfWorkFactory.newUnitOfWork();
            instance = unitOfWork.dereference( instance );

            // Check state
            assertThat( "property has correct value", instance.name().get(), equalTo( "Test" ) );
            assertThat( "property has correct value", instance.unsetName().get(), equalTo( null ) );
            assertThat( "association has correct value", instance.association().get(), equalTo( instance ) );
            assertThat( "manyAssociation has correct value", instance.manyAssociation().iterator().next(), equalTo( instance ) );
            assertThat( "listAssociation has correct value", instance.listAssociation().iterator().next(), equalTo( instance ) );
            assertThat( "setAssociation has correct value", instance.setAssociation().iterator().next(), equalTo( instance ) );
            assertThat( "setAssociation has correct size", instance.setAssociation().size(), equalTo( 1 ) );
            assertThat( "listAssociation has correct size", instance.listAssociation().size(), equalTo( 3 ) );

            unitOfWork.discard();
        }
        catch( Exception e )
        {
            e.printStackTrace();
        }
    }

    @Ignore
    @Test
    public void whenRemovedEntityThenCannotFindEntity()
        throws Exception
    {
        UnitOfWork unitOfWork = unitOfWorkFactory.newUnitOfWork();
        TestEntity newInstance = createEntity( unitOfWork );
        String identity = newInstance.identity().get();
        unitOfWork.complete();

        // Remove entity
        unitOfWork = unitOfWorkFactory.newUnitOfWork();
        TestEntity instance = unitOfWork.dereference( newInstance );
        unitOfWork.remove( instance );
        unitOfWork.complete();

        // Find entity
        unitOfWork = unitOfWorkFactory.newUnitOfWork();
        try
        {
            instance = unitOfWork.find( identity, TestEntity.class );
            fail( "Should not be able to find entity" );
        }
        catch( EntityCompositeNotFoundException e )
        {
            // Ok!
        }
        unitOfWork.discard();
    }

    protected TestEntity createEntity( UnitOfWork unitOfWork )
        throws UnitOfWorkCompletionException
    {
        // Create entity
        EntityBuilder<TestEntity> builder = unitOfWork.newEntityBuilder( TestEntity.class );
        TestEntity instance = builder.newInstance();
        String id = instance.identity().get();

        instance.name().set( "Test" );
        instance.association().set( instance );

        CompositeBuilder<TestValue> testValue = compositeBuilderFactory.newCompositeBuilder( TestValue.class );
        TestValue state = testValue.stateOfComposite();
        state.someValue().set( "Foo" );
        state.otherValue().set( 5 );

        TestValue value = testValue.newInstance();
        //instance.valueProperty().set( value );
        value.mutate();

        instance.manyAssociation().add( instance );

        instance.listAssociation().add( instance );
        instance.listAssociation().add( instance );
        instance.listAssociation().add( instance );

        instance.setAssociation().add( instance );
        instance.setAssociation().add( instance );
        return instance;
    }

    public interface TestEntity
        extends EntityComposite
    {
        Property<String> name();

        Property<String> unsetName();

        Property<TestValue> valueProperty();

        Association<TestEntity> association();

        Association<TestEntity> unsetAssociation();

        ManyAssociation<TestEntity> manyAssociation();

        ListAssociation<TestEntity> listAssociation();

        SetAssociation<TestEntity> setAssociation();
    }

    public interface TestValue
        extends ValueComposite<TestValue>
    {
        ImmutableProperty<String> someValue();

        ImmutableProperty<Integer> otherValue();
    }

    @Mixins( ValueComposite.ValueCompositeMixin.class )
    public interface ValueComposite<T>
        extends Composite
    {
        CompositeBuilder<T> mutate();

        public abstract class ValueCompositeMixin<T>
            implements ValueComposite<T>
        {
            @This Composite composite;
            @Structure CompositeBuilderFactory cbf;

            public CompositeBuilder<T> mutate()
            {
                CompositeBuilder<T> builder = (CompositeBuilder<T>) cbf.newCompositeBuilder( composite.type() );
                T state = builder.stateOfComposite();

                // Copy current state
                Method[] methods = state.getClass().getMethods();
                for( Method method : methods )
                {
                    if( Property.class.isAssignableFrom( method.getReturnType() ) )
                    {
                        try
                        {
                            Property<Object> oldProperty = (Property<Object>) method.invoke( composite );
                            Property<Object> newProperty = (Property<Object>) method.invoke( state );
                            newProperty.set( oldProperty.get() );
                        }
                        catch( IllegalAccessException e )
                        {
                            e.printStackTrace();
                        }
                        catch( InvocationTargetException e )
                        {
                            e.printStackTrace();
                        }
                    }
                }

                return builder;
            }
        }
    }
}