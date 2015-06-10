package org.qi4j.library.scripting;

import org.junit.Test;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

public class ScriptUtilImplTest
{
    @Test
    public void testDefaultStream()
    {
        ScriptUtil underTest = new ScriptUtilImpl();
        assertThat( underTest.getOut(), equalTo(System.out));
    }

    @Test( expected = ScriptException.class )
    public void testException()
    {
        throw new ScriptException( "This is a test exception." );
    }
}
