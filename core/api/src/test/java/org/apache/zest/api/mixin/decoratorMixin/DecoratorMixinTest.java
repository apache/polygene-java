/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.zest.api.mixin.decoratorMixin;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import org.junit.Test;
import org.apache.zest.api.composite.TransientBuilder;
import org.apache.zest.bootstrap.AssemblyException;
import org.apache.zest.bootstrap.ModuleAssembly;
import org.apache.zest.test.AbstractZestTest;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

public class DecoratorMixinTest extends AbstractZestTest
{
    // START SNIPPET: assembly
    @Override
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.transients( View1.class );
        module.transients( View2.class );
        module.transients( FooModel.class );
    }
// END SNIPPET: assembly

// START SNIPPET: test

    @Test
    public void testDecoration()
    {
        FooModelImpl model = new FooModelImpl( "Init" );
        View1 view1 = createView1( model );
        View2 view2 = createView2( model );
        assertThat( view1.bar(), equalTo( "Init" ) );
        assertThat( view2.bar(), equalTo( "Init" ) );
        model.setBar( "New Value" );
        assertThat( view1.bar(), equalTo( "New Value" ) );
        assertThat( view2.bar(), equalTo( "New Value" ) );
    }
// END SNIPPET: test

    @Test
    public void testDecorationWithGenericMixin()
    {
        InvocationHandler handler = new FooModelInvocationHandler("Init");
        ClassLoader cl = getClass().getClassLoader();
        FooModel model = (FooModel) Proxy.newProxyInstance( cl, new Class[]{ FooModel.class }, handler );
        View1 view1 = createView1( model );
        View2 view2 = createView2( model );
        assertThat( view1.bar(), equalTo( "Init" ) );
        assertThat( view2.bar(), equalTo( "Init" ) );
        model.setBar( "New Value" );
        assertThat( view1.bar(), equalTo( "New Value" ) );
        assertThat( view2.bar(), equalTo( "New Value" ) );
    }

    // START SNIPPET: create
    public View1 createView1( FooModel model )
    {
        TransientBuilder<View1> builder = module.newTransientBuilder( View1.class );
        builder.use( model );
        return builder.newInstance();
    }
// END SNIPPET: create

    public View2 createView2( FooModel model )
    {
        TransientBuilder<View2> builder = module.newTransientBuilder( View2.class );
        builder.use( model );
        return builder.newInstance();
    }
}
