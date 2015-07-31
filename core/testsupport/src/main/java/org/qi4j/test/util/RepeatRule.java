/*
 * Copyright 2014 Paul Merlin.
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
package org.qi4j.test.util;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 * JUnit Rule to run tests repeatedly.
 *
 * <p>To repeat a complete test class:</p>
 * <pre>@ClassRule public static final RepeatRule REPEAT = new RepeatRule( 10 );</pre>
 *
 * <p>To repeat each test on the same test class instance:</p>
 * <pre>@Rule public final RepeatRule repeat = new RepeatRule( 10 );</pre>
 */
public class RepeatRule
    implements TestRule
{
    private final int count;

    public RepeatRule( int count )
    {
        this.count = count;
    }

    @Override
    public Statement apply( final Statement base, Description description )
    {
        return new Statement()
        {
            @Override
            public void evaluate()
                throws Throwable
            {
                for( int repeat = 0; repeat < count; repeat++ )
                {
                    base.evaluate();
                }
            }
        };
    }

}
