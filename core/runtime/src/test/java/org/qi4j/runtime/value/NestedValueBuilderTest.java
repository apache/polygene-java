/*
* Copyright (c) 2012, Paul Merlin. All Rights Reserved.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*
*/
package org.qi4j.runtime.value;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Test;
import org.qi4j.api.common.UseDefaults;
import org.qi4j.api.property.Property;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueComposite;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.test.AbstractQi4jTest;

import static org.junit.Assert.fail;

public class NestedValueBuilderTest
    extends AbstractQi4jTest
{

    static interface InnerValue
        extends ValueComposite
    {

        Property<List<String>> listProp();

        Property<Map<String, String>> mapProp();
    }

    static interface InnerDefaultedValue
        extends ValueComposite
    {

        @UseDefaults
        Property<List<String>> listPropDefault();

        @UseDefaults
        Property<Map<String, String>> mapPropDefault();
    }

    static interface OuterValue
        extends ValueComposite
    {

        Property<List<InnerValue>> innerListProp();
    }

    static interface OuterDefaultedValue
        extends ValueComposite
    {

        @UseDefaults
        Property<List<InnerDefaultedValue>> innerListPropDefault();
    }

    @Override
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.values( InnerValue.class, InnerDefaultedValue.class, OuterValue.class, OuterDefaultedValue.class );
    }

    @Test
    public void testInner()
    {
        ValueBuilder<InnerValue> innerBuilder = module.newValueBuilder( InnerValue.class );
        InnerValue inner = innerBuilder.prototype();
        inner.listProp().set( new ArrayList<String>() );
        inner.mapProp().set( new HashMap<String, String>() );
        inner = innerBuilder.newInstance();
        // If we reach this point, value creation went well
        try
        {
            inner.listProp().get().add( "Should be immutable now!" ); // Must not be allowed
            fail( "List is not immutable!" );
        }
        catch( UnsupportedOperationException e )
        {
            // expected
        }
        try
        {
            inner.mapProp().get().put( "Should be immutable now!", "" ); // Must not be allowed
            fail( "Map is not immutable!" );
        }
        catch( UnsupportedOperationException e )
        {
            // expected
        }
    }

    @Test
    public void testOuter()
    {
        ValueBuilder<InnerValue> innerBuilder = module.newValueBuilder( InnerValue.class );
        InnerValue innerPrototype = innerBuilder.prototype();
        innerPrototype.listProp().set( new ArrayList<String>() );
        innerPrototype.mapProp().set( new HashMap<String, String>() );
        InnerValue innerInstance = innerBuilder.newInstance();
        ValueBuilder<OuterValue> outerBuilder = module.newValueBuilder( OuterValue.class );
        OuterValue outerPrototype = outerBuilder.prototype();
        List<InnerValue> inners = new ArrayList<InnerValue>();
        inners.add( innerInstance );
        outerPrototype.innerListProp().set( inners );
        OuterValue outerInstance = outerBuilder.newInstance();
        System.out.println( outerInstance.toString() );
        // If we reach this point, value creation went well
        try
        {
            outerInstance.innerListProp().get().add( innerInstance ); // Must not be allowed
            fail( "List is not immutable!" );
        }
        catch( UnsupportedOperationException e )
        {
            // expected
        }
    }

    @Test
    public void testDefaultedInner()
    {
        ValueBuilder<InnerDefaultedValue> innerBuilder = module.newValueBuilder( InnerDefaultedValue.class );
        InnerDefaultedValue inner = innerBuilder.newInstance();
        // If we reach this point, value creation went well
        try
        {
            inner.listPropDefault().get().add( "Should not work!" ); // Must not be allowed
            fail( "List is not immutable!" );
        }
        catch( UnsupportedOperationException e )
        {
            // expected
        }
        try
        {
            inner.mapPropDefault().get().put( "Should not work!", "" ); // Must not be allowed
            fail( "List is not immutable!" );
        }
        catch( UnsupportedOperationException e )
        {
            // expected
        }
    }

    @Test
    public void testDefaultedOuter()
    {
        ValueBuilder<InnerDefaultedValue> innerBuilder = module.newValueBuilder( InnerDefaultedValue.class );
        InnerDefaultedValue innerInstance = innerBuilder.newInstance();
        ValueBuilder<OuterDefaultedValue> outerBuilder = module.newValueBuilder( OuterDefaultedValue.class );
        OuterDefaultedValue outerPrototype = outerBuilder.prototype();
        List<InnerDefaultedValue> inners = new ArrayList<InnerDefaultedValue>();
        inners.add( innerInstance );
        outerPrototype.innerListPropDefault().set( inners );
        OuterDefaultedValue outerInstance = outerBuilder.newInstance();
        System.out.println( outerPrototype.toString() );
        // If we reach this point, value creation went well
        try
        {
            outerInstance.innerListPropDefault().get().add( innerInstance ); // Must not be allowed
            fail( "List is not immutable!" );
        }
        catch( UnsupportedOperationException e )
        {
            // expected
        }
    }
}