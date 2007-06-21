package org.qi4j.test.model1;

import junit.framework.TestCase;
import org.qi4j.api.CompositeInstantiationException;
import org.qi4j.api.FragmentFactory;
import org.qi4j.api.model.MixinModel;
import org.qi4j.api.model.ModifierModel;
import org.qi4j.runtime.FragmentFactoryImpl;

public class FragmentFactoryImplTest extends TestCase
{
    FragmentFactory fragmentFactory;

    protected void setUp() throws Exception
    {
        super.setUp();
        fragmentFactory = new FragmentFactoryImpl();
    }

    public void testNewFragmentForMixinModel() throws Exception
    {
        try
        {
            Object mixin = fragmentFactory.newFragment( new MixinModel( Mixin3.class ), null );
            fail( "FragmentFactoryImpl should not be able to instantiate a Mixin : " + Mixin3.class.getName() );
        }
        catch( CompositeInstantiationException e )
        {
            // Correct
        }
    }

    public void testNewFragmentForModifierModel() throws Exception
    {
        try
        {
            Object modifier = fragmentFactory.newFragment( new ModifierModel( Modifier7.class ), null );
            assertTrue( modifier instanceof Modifier7 );
        }
        catch( CompositeInstantiationException e )
        {
            fail( "FragmentFactoryImpl must be able to instantiate a Modifier : " + Modifier7.class.getName() );
        }
    }

}

