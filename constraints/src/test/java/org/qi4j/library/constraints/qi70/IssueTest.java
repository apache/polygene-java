package org.qi4j.library.constraints.qi70;

import org.junit.Test;
import org.qi4j.api.composite.TransientBuilder;
import org.qi4j.api.constraint.ConstraintViolationException;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.test.AbstractQi4jTest;

public class IssueTest
    extends AbstractQi4jTest
{

    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.transients( SampleComposite.class );
    }

    @Test( expected = ConstraintViolationException.class )
    public void testNotEmpty()
    {
        TransientBuilder<Sample> cb = transientBuilderFactory.newTransientBuilder( Sample.class );
        cb.prototypeFor( Sample.class ).stuff().set( null );
    }
}
