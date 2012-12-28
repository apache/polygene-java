/*
 * Copyright 2008 Michael Hunger.
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
package org.qi4j.test.indexing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.entity.Identity;
import org.qi4j.test.indexing.model.Nameable;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.qi4j.functional.Iterables.toList;

public class NameableAssert
{
    // id -> name
    private static Map<String, String> world = new HashMap<String, String>();

    public static void clear()
    {
        world.clear();
    }

    public static void assertNames( Iterable<EntityReference> identitiesIterable, String... expectedNames )
    {
        assertNames( true, identitiesIterable, expectedNames );
    }

    public static void assertNames( boolean sort,
                                    Iterable<EntityReference> identitiesIterable,
                                    String... expectedNames
    )
    {
        final List<EntityReference> references = toList( identitiesIterable );
        assertEquals( expectedNames.length + " entries(" + expectedNames.length + ", got " + getNames( references ) + ")", expectedNames.length, references
            .size() );
        List<String> sortedNames = getNames( references );
        final List<String> expectedSorted = java.util.Arrays.asList( expectedNames );
        if( sort )
        {
            Collections.sort( sortedNames );
            Collections.sort( expectedSorted );
        }
        assertEquals( "names", expectedSorted, sortedNames );
    }

    public static void trace( Nameable nameable )
    {
        world.put( ( (Identity) nameable ).identity().get(), nameable.name().get() );
    }

    public static void assertName( String expectedName, EntityReference reference )
    {
        final String existingName = getName( reference );
        assertEquals( "Name of " + reference, expectedName, existingName );
    }

    public static String getName( EntityReference reference )
    {
        return world.get( reference.identity() );
    }

    public static List<String> getNames( List<EntityReference> references )
    {
        List<String> result = new ArrayList<String>( references.size() );
        for( EntityReference reference : references )
        {
            final String name = getName( reference );
            assertNotNull( "Name of " + reference, name );
            result.add( name );
        }
        return result;
    }

    public static String[] allNames()
    {
        return world.values().toArray( new String[ world.size() ] );
    }
}
