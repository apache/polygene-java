/*
 * Copyright 2013 Paul Merlin.
 *
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.qi4j.api.activation;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Collections;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertThat;

public class PassivationExceptionTest
{

    @Test
    public void testEmpty()
    {
        PassivationException empty = new PassivationException( Collections.<Exception>emptyList() );
        assertThat( empty.getMessage(), containsString( "has 0 cause" ) );
    }

    @Test
    public void testSingle()
    {
        PassivationException single = new PassivationException( Collections.singletonList( new Exception( "single" ) ) );
        assertThat( single.getMessage(), containsString( "1" ) );

        StringWriter writer = new StringWriter();
        single.printStackTrace( new PrintWriter( writer ) );
        writer.flush();
        String stack = writer.toString();

        assertThat( single.getMessage(), containsString( "has 1 cause" ) );
        assertThat( stack, containsString( "Suppressed: java.lang.Exception: single" ) );
    }

    @Test
    public void testMultiple()
    {
        PassivationException multi = new PassivationException( Arrays.asList( new Exception( "one" ),
                                                                              new Exception( "two" ),
                                                                              new Exception( "three" ) ) );
        assertThat( multi.getMessage(), containsString( "3" ) );

        StringWriter writer = new StringWriter();
        multi.printStackTrace( new PrintWriter( writer ) );
        writer.flush();
        String stack = writer.toString();

        assertThat( multi.getMessage(), containsString( "has 3 cause(s)" ) );

        assertThat( stack, containsString( "Suppressed: java.lang.Exception: one" ) );
        assertThat( stack, containsString( "Suppressed: java.lang.Exception: two" ) );
        assertThat( stack, containsString( "Suppressed: java.lang.Exception: three" ) );
    }

}
