/* Copyright 2008 Neo Technology, http://neotechnology.com.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.qi4j.entitystore.neo4j.test;

import java.util.Set;
import org.junit.Test;
import org.qi4j.entitystore.neo4j.Configuration;

/**
 * @author Tobias Ivarsson (tobias.ivarsson@neotechnology.com)
 */
public class DirectIdentitySetTest
    extends TestBase
{
    public DirectIdentitySetTest()
    {
        super( Configuration.DIRECT, false, true );
    }

    @Test
    public void testEmptySet() throws Exception
    {
        perform( new CollectionBuilder<Set<ContainedElement>>()
        {
            protected void build( Set<ContainedElement> set )
            {
            }
        } );
    }

    @Test
    public void testSimpleSet() throws Exception
    {
        perform( new CollectionBuilder<Set<ContainedElement>>( 1, 2, 3 )
        {
            protected void build( Set<ContainedElement> set )
            {
                set.add( element( 1 ) );
                set.add( element( 2 ) );
                set.add( element( 3 ) );
            }
        } );
    }

    @Test
    public void testDuplicates() throws Exception
    {
        perform( new CollectionBuilder<Set<ContainedElement>>( 1, 2, 3 )
        {
            protected void build( Set<ContainedElement> set )
            {
                set.add( element( 1 ) );
                set.add( element( 2 ) );
                set.add( element( 2 ) );
                set.add( element( 3 ) );
                set.add( element( 1 ) );
            }
        } );
    }

    @Test
    public void testRemove() throws Exception
    {
        perform( new CollectionBuilder<Set<ContainedElement>>( 1, 2, 3 )
        {
            protected void build( Set<ContainedElement> set )
            {
                set.add( element( 1 ) );
                set.add( element( 0 ) );
                set.add( element( 2 ) );
                set.add( element( 3 ) );
                set.remove( element( 0 ) );
            }
        } );
    }
}
