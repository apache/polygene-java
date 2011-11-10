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

package org.qi4j.functional;

import org.junit.Assert;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;

/**
 * JAVADOC
 */
public class SpecificationsTest
{
    @Test
    public void testTRUE()
    {
        Assert.assertThat( Specifications.<Object>TRUE().satisfiedBy( new Object() ), equalTo( true ) );
    }

    @Test
    public void testNot()
    {
        Assert.assertThat( Specifications.not( Specifications.<Object>TRUE() )
                               .satisfiedBy( new Object() ), equalTo( false ) );
    }

    @Test
    public void testAnd()
    {
        Specification<Object> trueSpec = Specifications.<Object>TRUE();
        Specification<Object> falseSpec = Specifications.not( Specifications.<Object>TRUE() );

        Assert.assertThat( Specifications.and( falseSpec, falseSpec ).satisfiedBy( new Object() ), equalTo( false ) );
        Assert.assertThat( Specifications.and( trueSpec, falseSpec ).satisfiedBy( new Object() ), equalTo( false ) );
        Assert.assertThat( Specifications.and( falseSpec, trueSpec ).satisfiedBy( new Object() ), equalTo( false ) );
        Assert.assertThat( Specifications.and( trueSpec, trueSpec ).satisfiedBy( new Object() ), equalTo( true ) );
    }

    @Test
    public void testOr()
    {
        Specification<Object> trueSpec = Specifications.<Object>TRUE();
        Specification<Object> falseSpec = Specifications.not( Specifications.<Object>TRUE() );

        Assert.assertThat( Specifications.or( falseSpec, falseSpec ).satisfiedBy( new Object() ), equalTo( false ) );
        Assert.assertThat( Specifications.or( trueSpec, falseSpec ).satisfiedBy( new Object() ), equalTo( true ) );
        Assert.assertThat( Specifications.or( falseSpec, trueSpec ).satisfiedBy( new Object() ), equalTo( true ) );
        Assert.assertThat( Specifications.or( trueSpec, trueSpec ).satisfiedBy( new Object() ), equalTo( true ) );
    }

    @Test
    public void testIn()
    {
        Assert.assertThat( Specifications.in( "1", "2", "3" ).satisfiedBy( "2" ), equalTo( true ) );
        Assert.assertThat( Specifications.in( "1", "2", "3" ).satisfiedBy( "4" ), equalTo( false ) );
    }

    @Test
    public void testTranslate()
    {
        Function<Object, String> stringifier = new Function<Object, String>()
        {
            @Override
            public String map( Object s )
            {
                return s.toString();
            }
        };

        Assert.assertTrue( Specifications.translate( stringifier, Specifications.in( "3" ) ).satisfiedBy( 3L ) );
    }
}
