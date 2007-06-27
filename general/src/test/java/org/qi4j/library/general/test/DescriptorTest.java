package org.qi4j.library.general.test;

import junit.framework.TestCase;
import org.qi4j.api.Composite;
import org.qi4j.api.CompositeFactory;
import org.qi4j.api.annotation.ImplementedBy;
import org.qi4j.api.annotation.ModifiedBy;
import org.qi4j.library.framework.properties.PropertiesMixin;
import org.qi4j.library.general.model.Descriptor;
import org.qi4j.library.general.model.DescriptorMixin;
import org.qi4j.library.general.model.DescriptorModifier;
import org.qi4j.library.general.model.Name;
import org.qi4j.runtime.CompositeFactoryImpl;

public class DescriptorTest extends TestCase
{
    private CompositeFactory compositeFactory;

    protected void setUp() throws Exception
    {
        super.setUp();
        compositeFactory = new CompositeFactoryImpl();
    }


    public void testDescriptorAsMixin() throws Exception
    {
        DummyComposite composite = compositeFactory.newInstance( DummyComposite.class );
        composite.setName( "Sianny" );
        String displayValue = composite.getDisplayValue();
        assertEquals( displayValue, composite.getName() );
    }

    public void testDescriptorWithModifier() throws Exception {
        DummyComposite2 composite = compositeFactory.newInstance( DummyComposite2.class );
        composite.setName( "Sianny" );
        String displayValue = composite.getDisplayValue();
        assertEquals( displayValue, "My name is " + composite.getName() );
    }

    @ImplementedBy( { DescriptorMixin.class, PropertiesMixin.class } )
    private interface DummyComposite extends Descriptor, Name, Composite
    {
    }

    @ModifiedBy( { DescriptorModifier.class } )
    @ImplementedBy( { DescriptorMixin.class, PropertiesMixin.class } )
    private interface DummyComposite2 extends Descriptor, Name, Composite
    {
    }
}
