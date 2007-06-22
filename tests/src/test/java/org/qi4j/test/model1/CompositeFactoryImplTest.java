package org.qi4j.test.model1;

import junit.framework.TestCase;
import org.qi4j.api.CompositeFactory;
import org.qi4j.api.CompositeInstantiationException;
import org.qi4j.runtime.CompositeFactoryImpl;

public class CompositeFactoryImplTest extends TestCase
{
    CompositeFactory compositeFactory;

    protected void setUp() throws Exception
    {
        super.setUp();
        compositeFactory = new CompositeFactoryImpl();
    }

    public void testNewInstanceNotExtendingComposite() throws Exception
    {

        try
        {
            Class aClass = Composition8.class;
            Composition8 composition8 = (Composition8) compositeFactory.newInstance( aClass );
            fail( "CompositeFactory.newInstance() should return CompositeInstantiationException when creating a new instance for " + aClass.getName() );
        }
        catch( CompositeInstantiationException e )
        {
            // Correct
        }
    }

    public void testNewComposition9() throws Exception
    {
        try
        {
            Composition9 composition9 = compositeFactory.newInstance( Composition9.class );
            composition9.setValue( "test value" );
        }
        catch( Exception e )
        {
            fail( "Fail to instantiate composite: " + Composition9.class );
            e.printStackTrace();
        }
    }

    public void testNewComposition10() throws Exception
    {
        try
        {
            Composition10 composition10 = compositeFactory.newInstance( Composition10.class );
//            composition10.setValue( "test value" );
        }
        catch( Exception e )
        {
            fail( "Fail to instantiate composite: " + Composition10.class );
            e.printStackTrace();
        }
    }
}
