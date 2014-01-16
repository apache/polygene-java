/*
 * Copyright (c) 2014, Paul Merlin. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 *     You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.qi4j.library.conversion.values;

import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import org.qi4j.api.entity.EntityBuilder;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.property.Property;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkCompletionException;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueComposite;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.test.AbstractQi4jTest;
import org.qi4j.test.EntityTestAssembler;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

public class NestedValuesConversionTest
    extends AbstractQi4jTest
{

    @Override
    public void assemble( ModuleAssembly ma )
        throws AssemblyException
    {
        ma.services( EntityToValueService.class );
        new EntityTestAssembler().assemble( ma );

        ma.entities( FooEntity.class );
        ma.values( FooValue.class, BarValue.class );
    }

    @Test
    public void testNestedValuesConversion()
        throws UnitOfWorkCompletionException
    {
        UnitOfWork uow = module.newUnitOfWork();
        try
        {
            FooEntity fooEntity = createFooEntity( uow, "Test nested values conversion" );
            EntityToValueService conversion = module.findService( EntityToValueService.class ).get();
            FooValue fooValue = conversion.convert( FooValue.class, fooEntity );
            assertThat( fooValue.name().get(), equalTo( "Test nested values conversion" ) );
            assertThat( fooValue.bar().get().bazar().get(), equalTo( "single" ) );
            List<BarValue> barList = fooValue.barList().get();
            assertThat( barList.size(), equalTo( 3 ) );
            assertThat( barList.get( 0 ).bazar().get(), equalTo( "multi-one" ) );
            assertThat( barList.get( 1 ).bazar().get(), equalTo( "multi-two" ) );
            assertThat( barList.get( 2 ).bazar().get(), equalTo( "multi-three" ) );
            uow.complete();
            uow = null;
        }
        finally
        {
            if( uow != null )
            {
                uow.discard();
            }
        }
    }

    private FooEntity createFooEntity( UnitOfWork uow, String name )
    {
        EntityBuilder<FooEntity> builder = uow.newEntityBuilder( FooEntity.class );
        builder.instance().name().set( name );
        builder.instance().bar().set( createBarValue( "single" ) );
        List<BarValue> bars = Arrays.asList( createBarValue( "multi-one" ),
                                             createBarValue( "multi-two" ),
                                             createBarValue( "multi-three" ) );
        builder.instance().barList().set( bars );
        return builder.newInstance();
    }

    private BarValue createBarValue( String bazar )
    {
        ValueBuilder<BarValue> builder = module.newValueBuilder( BarValue.class );
        builder.prototype().bazar().set( bazar );
        return builder.newInstance();
    }

    public interface FooState
    {

        Property<String> name();

        Property<BarValue> bar();

        Property<List<BarValue>> barList();
    }

    public interface FooValue
        extends FooState, ValueComposite
    {
    }

    public interface FooEntity
        extends FooState, EntityComposite
    {
    }

    public interface BarValue
        extends ValueComposite
    {

        Property<String> bazar();
    }

}
