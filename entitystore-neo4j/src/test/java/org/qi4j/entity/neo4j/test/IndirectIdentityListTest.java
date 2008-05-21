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
package org.qi4j.entity.neo4j.test;

import java.util.List;
import org.junit.Ignore;
import org.junit.Test;

/**
 * @author Tobias Ivarsson (tobias.ivarsson@neotechnology.com)
 */
@Ignore
public class IndirectIdentityListTest extends TestBase
{
    public IndirectIdentityListTest()
    {
        super( false, false );
    }

    @Test
    public void testEmptyList() throws Exception
    {
        perform( new CollectionBuilder<List>()
        {
            protected void build( List list )
            {
            }
        } );
    }

    @Test
    public void testSimpleList() throws Exception
    {
        try
        {
            perform( new CollectionBuilder<List>( 1, 2, 3 )
            {
                protected void build( List list )
                {
                    list.add( element( 1 ) );
                    list.add( element( 2 ) );
                    list.add( element( 3 ) );
                }
            } );
        }
        catch( Exception e )
        {
            e.printStackTrace();
            throw e;
        }
    }
}
