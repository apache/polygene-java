package org.qi4j.tools.shell.templating;

import java.util.HashMap;
import java.util.Map;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

public class TemplatingEngineTest
{
    @Test
    public void givenCorrectTemplateWhenGeneratingExpectGoodResult()
        throws Exception
    {
        Map<String, String> variables = new HashMap<>();
        variables.put( "REPLACE", "def" );
        TemplateEngine engine = new TemplateEngine( TEMPLATE1 );
        String result = engine.create( variables );
        assertThat( result, equalTo( "abcdefghi" ) );
    }

    @Test
    public void givenCorrectTemplateWhenGeneratingMultipleOutputsExpectGoodResult()
        throws Exception
    {
        TemplateEngine engine = new TemplateEngine( TEMPLATE1 );

        Map<String, String> variables = new HashMap<>();
        variables.put( "REPLACE", "def" );
        String result = engine.create( variables );
        assertThat( result, equalTo( "abcdefghi" ) );

        variables.put( "REPLACE", "rst" );
        result = engine.create( variables );
        assertThat( result, equalTo( "abcrstghi" ) );
    }

    private static final String TEMPLATE1 = "abc@@REPLACE@@ghi";
}
