package org.qi4j.bootstrap.assembly;

import org.junit.Test;
import org.qi4j.api.activation.ActivationException;
import org.qi4j.api.structure.Application;
import org.qi4j.bootstrap.AssemblyException;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

public class LayeredApplicationAssemblerTest
{
    @Test
    public void validateThatAssemblerCreatesApplication()
        throws AssemblyException, ActivationException
    {
        TestApplication assembler = new TestApplication( "Test Application", "1.0.1", Application.Mode.test );
        assembler.start();

        assertThat( assembler.application().name(), equalTo("Test Application") );
        assertThat( assembler.application().version(), equalTo("1.0.1") );
    }
}
