package org.qi4j.library.general.test;

import junit.framework.TestCase;
import org.qi4j.api.CompositeFactory;
import org.qi4j.api.annotation.ImplementedBy;
import org.qi4j.api.annotation.ModifiedBy;
import org.qi4j.api.persistence.composite.PersistentComposite;
import org.qi4j.library.framework.properties.PropertiesMixin;
import org.qi4j.library.general.model.DummyValidationModifier;
import org.qi4j.library.general.model.Name;
import org.qi4j.library.general.model.Validatable;
import org.qi4j.library.general.model.DummyPersistentStorage;
import org.qi4j.library.general.model.modifiers.LifecycleValidationModifier;
import org.qi4j.runtime.CompositeFactoryImpl;

public class ValidatableTest extends TestCase
{
    private CompositeFactory compositeFactory;

    protected void setUp() throws Exception
    {
        super.setUp();
        compositeFactory = new CompositeFactoryImpl();
    }


    public void testValidatableSuccessful() throws Exception
    {
        DummyComposite composite = compositeFactory.newInstance( DummyComposite.class );
        composite.setPersistentStorage( new DummyPersistentStorage() );
        composite.create();

        assertTrue(DummyValidationModifier.validateIsCalled);
    }

    @ModifiedBy( { LifecycleValidationModifier.class, DummyValidationModifier.class } )
    @ImplementedBy( { PropertiesMixin.class } )
    private interface DummyComposite extends Name, Validatable, PersistentComposite
    {
    }
}
