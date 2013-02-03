package org.qi4j.api.mixin.decoratorMixin;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import org.junit.Test;
import org.qi4j.api.composite.TransientBuilder;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.test.AbstractQi4jTest;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

public class DecoratorMixinTest extends AbstractQi4jTest
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
