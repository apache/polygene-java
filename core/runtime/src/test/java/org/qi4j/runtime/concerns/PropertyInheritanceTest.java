package org.qi4j.runtime.concerns;

import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import org.qi4j.api.common.InvalidApplicationException;
import org.qi4j.api.common.UseDefaults;
import org.qi4j.api.concern.ConcernOf;
import org.qi4j.api.concern.Concerns;
import org.qi4j.api.property.InvalidPropertyTypeException;
import org.qi4j.api.property.Property;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.test.AbstractQi4jTest;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

//TODO 2.0 removed this possibility when simplifying the Property handling. So, we are now checking that a decent
// exception is thrown, but should be changed to supported instead.
public class PropertyInheritanceTest extends AbstractQi4jTest
{

    private boolean failed;

    @Override
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.transients( Audit.class );
    }

    @Override
    protected void assemblyException( AssemblyException exception )
        throws AssemblyException
    {
        if( exception.getCause() instanceof InvalidApplicationException )
        {
            if( exception.getCause().getCause() instanceof InvalidPropertyTypeException )
            {
                failed = true;
                return;
            }
        }
        super.assemblyException( exception );
    }

    @Test
    public void givenConcernOnInheritedPropertyWhenAccessingPropertyExpectConcernToBeCalled()
    {
        assertThat( failed, equalTo( true ) );

// TODO: The following test code is testing the feature once it has been implemented. The @Test is needed to ensure check for the right Exception.
//        List<String> data = new ArrayList<String>();
//        data.add( "First" );
//        data.add( "Second" );
//        data.add( "Third" );
//        Audit audit = module.newTransient( Audit.class );
//        AuditTrail trail = audit.trail();
//        trail.set( data );
//        assertThat( audit.trail().get().get( 0 ), equalTo( "1: First" ) );
//        assertThat( audit.trail().get().get( 1 ), equalTo( "2: Second" ) );
//        assertThat( audit.trail().get().get( 2 ), equalTo("3: Third"));
    }

    public static abstract class AuditTrailMarkupConcern extends ConcernOf<AuditTrail>
        implements AuditTrail
    {

        @Override
        public void set( List<String> newValue )
            throws IllegalArgumentException, IllegalStateException
        {
            List<String> markedUp = new ArrayList<String>();
            int counter = 0;
            for( String value : newValue )
            {
                markedUp.add( counter++ + ": " + value );
            }
            next.set( markedUp );
        }
    }

    @Concerns( AuditTrailMarkupConcern.class )
    public interface AuditTrail extends Property<List<String>>
    {
    }

    public interface Audit
    {
        @UseDefaults
        AuditTrail trail();
    }
}
