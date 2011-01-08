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

package org.qi4j.api.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;
import org.qi4j.api.specification.Specification;
import org.qi4j.api.specification.Specifications;

import static org.hamcrest.CoreMatchers.*;

/**
 * Test of Iterables utility methods
 */
public class IterablesTest
{
    private List<String> numbers = Arrays.asList( "1", "2", "3" );

    @Test
    public void testAddAll()
    {
        Assert.assertThat( Iterables.addAll( new ArrayList<String>(), numbers ).toString(), equalTo( "[1, 2, 3]" ) );
    }

    @Test
    public void testCount()
    {
        Assert.assertThat( Iterables.count( numbers ), equalTo( 3L ) );
    }

    @Test
    public void testFilter()
    {
        Assert.assertThat( Iterables.first( Iterables.filter( new Specification<String>()
        {
            public boolean satisfiedBy( String item )
            {
                return item.equals( "2" );
            }
        }, numbers ) ), equalTo( "2" ) );
    }

    @Test
    public void testFirst()
    {
        Assert.assertThat( Iterables.first( numbers ), equalTo( "1" ) );
        Assert.assertThat( Iterables.first( Collections.<Object>emptyList() ), CoreMatchers.<Object>nullValue() );
    }

    @Test
    public void testMatchesAny()
    {
        Assert.assertThat( Iterables.matchesAny( Specifications.in( "2" ), numbers ), equalTo( true ) );
        Assert.assertThat( Iterables.matchesAny( Specifications.in( "4" ), numbers ), equalTo( false ) );
    }

    @Test
    public void testFlatten()
    {
        Assert.assertThat( Iterables.addAll( new ArrayList<String>(), Iterables.flatten( numbers, numbers ) )
                               .toString(), equalTo( "[1, 2, 3, 1, 2, 3]" ) );
    }

    @Test
    public void testFlattenIterables()
    {
        Iterable<Iterable<String>> iterable = Iterables.iterable( numbers, (Iterable<String>) numbers );
        Assert.assertThat( Iterables.addAll( new ArrayList<String>(),
                                             Iterables.flatten( iterable ) )
                               .toString(), equalTo( "[1, 2, 3, 1, 2, 3]" ) );
    }

    @Test
    public void testMap()
    {
        Assert.assertThat( Iterables.addAll( new ArrayList<String>(), Iterables.map( new Function<String, String>()
        {
            public String map( String s )
            {
                return s + s;
            }
        }, numbers ) ).toString(), equalTo( "[11, 22, 33]" ) );
    }

    @Test
    public void testIterableEnumeration()
    {

        Enumeration<String> enumeration = Collections.enumeration( numbers );
        Assert.assertThat( Iterables.addAll( new ArrayList<String>(), Iterables.iterable( enumeration ) )
                               .toString(), equalTo( "[1, 2, 3]" ) );
    }

    @Test
    public void testIterableVarArg()
    {
        Assert.assertThat( Iterables.addAll( new ArrayList<String>(), Iterables.iterable( "1", "2", "3" ) )
                               .toString(), equalTo( "[1, 2, 3]" ) );
    }
}
