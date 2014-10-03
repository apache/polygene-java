/*
 * Copyright (c) 2010, Rickard Öberg. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.qi4j.api.specification;

import java.util.function.Function;
import java.util.function.Predicate;
import org.junit.Assert;
import org.junit.Test;
import org.qi4j.functional.Specifications;

import static org.hamcrest.CoreMatchers.equalTo;

/**
 * JAVADOC
 */
public class SpecificationsTest
{
    @Test
    public void testTRUE()
    {
        Assert.assertThat( Specifications.<Object>TRUE().test( new Object() ), equalTo( true ) );
    }

    @Test
    public void testNot()
    {
        Assert.assertThat( Specifications.not( Specifications.<Object>TRUE() )
                               .test( new Object() ), equalTo( false ) );
    }

    @Test
    public void testAnd()
    {
        Predicate<Object> trueSpec = Specifications.<Object>TRUE();
        Predicate<Object> falseSpec = Specifications.not( Specifications.<Object>TRUE() );

        Assert.assertThat( Specifications.and( falseSpec, falseSpec ).test( new Object() ), equalTo( false ) );
        Assert.assertThat( Specifications.and( trueSpec, falseSpec ).test( new Object() ), equalTo( false ) );
        Assert.assertThat( Specifications.and( falseSpec, trueSpec ).test( new Object() ), equalTo( false ) );
        Assert.assertThat( Specifications.and( trueSpec, trueSpec ).test( new Object() ), equalTo( true ) );
    }

    @Test
    public void testOr()
    {
        Predicate<Object> trueSpec = Specifications.<Object>TRUE();
        Predicate<Object> falseSpec = Specifications.not( Specifications.<Object>TRUE() );

        Assert.assertThat( Specifications.or( falseSpec, falseSpec ).test( new Object() ), equalTo( false ) );
        Assert.assertThat( Specifications.or( trueSpec, falseSpec ).test( new Object() ), equalTo( true ) );
        Assert.assertThat( Specifications.or( falseSpec, trueSpec ).test( new Object() ), equalTo( true ) );
        Assert.assertThat( Specifications.or( trueSpec, trueSpec ).test( new Object() ), equalTo( true ) );
    }

    @Test
    public void testIn()
    {
        Assert.assertThat( Specifications.in( "1", "2", "3" ).test( "2" ), equalTo( true ) );
        Assert.assertThat( Specifications.in( "1", "2", "3" ).test( "4" ), equalTo( false ) );
    }

    @Test
    public void testTranslate()
    {
        Function<Object, String> stringifier = new Function<Object, String>()
        {
            @Override
            public String apply( Object s )
            {
                return s.toString();
            }
        };

        Assert.assertTrue( Specifications.translate( stringifier, Specifications.in( "3" ) ).test( 3L ) );
    }
}
