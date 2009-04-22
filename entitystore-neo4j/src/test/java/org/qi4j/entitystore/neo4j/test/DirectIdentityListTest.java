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

import java.util.List;
import org.junit.Test;
import org.junit.Ignore;
import org.qi4j.entitystore.neo4j.Configuration;

/**
 * @author Tobias Ivarsson (tobias.ivarsson@neotechnology.com)
 */
@Ignore("Until Neo4j is upgraded")
public class DirectIdentityListTest
    extends TestBase
{
    public DirectIdentityListTest()
    {
        super( Configuration.DIRECT, false, true );
    }

    // Tests...

    @Test
    public void testEmptyList() throws Exception
    {
        perform( new CollectionBuilder<List<ContainedElement>>()
        {
            protected void build( List<ContainedElement> list )
            {
            }
        } );
    }

    @Test
    public void testSimpleList() throws Exception
    {
        perform( new CollectionBuilder<List<ContainedElement>>( 0, 1, 2 )
        {
            protected void build( List<ContainedElement> list )
            {
                list.add( element( 0 ) );
                list.add( element( 1 ) );
                list.add( element( 2 ) );
            }
        } );
    }

    @Test
    public void testRemove() throws Exception
    {
        perform( new CollectionBuilder<List<ContainedElement>>( 0, 2 )
        {
            protected void build( List<ContainedElement> list )
            {
                list.add( element( 0 ) );
                list.add( element( 1 ) );
                list.add( element( 2 ) );
                list.remove( element( 1 ) );
            }
        } );
    }

    @Test
    public void testSet() throws Exception
    {
        perform( new CollectionBuilder<List<ContainedElement>>( 0, 3, 2 )
        {
            protected void build( List<ContainedElement> list )
            {
                list.add( element( 0 ) );
                list.add( element( 1 ) );
                list.add( element( 2 ) );
                list.set( 1, element( 3 ) );
            }
        } );
    }

    @Test
    public void testAdd() throws Exception
    {
        perform( new CollectionBuilder<List<ContainedElement>>( 0, 3, 1, 2 )
        {
            protected void build( List<ContainedElement> list )
            {
                list.add( element( 0 ) );
                list.add( element( 1 ) );
                list.add( element( 2 ) );
                list.add( 1, element( 3 ) );
            }
        } );
    }

    @Test
    public void testSharedElement() throws Exception
    {
        perform( new CollectionBuilder<List<ContainedElement>>( 0, 1, 2 )
        {
            protected void build( List<ContainedElement> list )
            {
                list.add( element( 0 ) );
                list.add( element( 1 ) );
                list.add( element( 2 ) );
            }
        }, new CollectionBuilder<List<ContainedElement>>( 3, 1, 4 )
        {
            protected void build( List<ContainedElement> list )
            {
                list.add( element( 3 ) );
                list.add( element( 1 ) );
                list.add( element( 4 ) );
            }
        } );
    }

    @Test
    public void testDuplicateOccurances() throws Exception
    {
        perform( new CollectionBuilder<List<ContainedElement>>( 0, 1, 2, 1 )
        {
            protected void build( List<ContainedElement> list )
            {
                list.add( element( 0 ) );
                list.add( element( 1 ) );
                list.add( element( 2 ) );
                list.add( element( 1 ) );
            }
        } );
    }

    @Test
    public void testDuplicateInARow() throws Exception
    {
        perform( new CollectionBuilder<List<ContainedElement>>( 0, 1, 1, 2 )
        {
            protected void build( List<ContainedElement> list )
            {
                list.add( element( 0 ) );
                list.add( element( 1 ) );
                list.add( element( 1 ) );
                list.add( element( 2 ) );
            }
        } );
    }

    @Test
    public void testRemoveDuplicate() throws Exception
    {
        perform( new CollectionBuilder<List<ContainedElement>>( 0, 1, 2 )
        {
            protected void build( List<ContainedElement> list )
            {
                list.add( element( 0 ) );
                list.add( element( 1 ) );
                list.add( element( 1 ) );
                list.add( element( 2 ) );
                list.remove( 1 );
            }
        } );
    }
}
