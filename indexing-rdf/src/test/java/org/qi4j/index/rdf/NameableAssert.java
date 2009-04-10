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
package org.qi4j.index.rdf;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import org.qi4j.api.entity.Identity;
import org.qi4j.index.rdf.model.Nameable;
import org.qi4j.spi.entity.QualifiedIdentity;

import java.util.*;

public class NameableAssert
{
    // id -> name
    private static Map<String, String> world = new HashMap<String, String>();

    public static void clear()
    {
        world.clear();
    }

    public static void assertNames( Iterable<QualifiedIdentity> identitiesIterable, String... expectedNames )
    {
        assertNames( true, identitiesIterable, expectedNames );
    }

    public static void assertNames( boolean sort, Iterable<QualifiedIdentity> identitiesIterable, String... expectedNames )
    {
        final List<QualifiedIdentity> identities = toList( identitiesIterable );
        assertEquals( expectedNames.length + " entries", expectedNames.length, identities.size() );
        List<String> sortedNames = getNames( identities );
        final List<String> expectedSorted = java.util.Arrays.asList( expectedNames );
        if( sort )
        {
            Collections.sort( sortedNames );
            Collections.sort( expectedSorted );
        }
        assertEquals( "names", expectedSorted, sortedNames );
    }

    public static <T> List<T> toList( final Iterable<T> iterable )
    {
        final List<T> result = new ArrayList<T>();
        for( final T element : iterable )
        {
            result.add( element );
        }
        return result;
    }

    public static void trace( Nameable nameable )
    {
        world.put( ( (Identity) nameable ).identity().get(), nameable.name().get() );
    }

    public static void assertName( String expectedName, QualifiedIdentity identity )
    {
        final String existingName = getName( identity );
        assertEquals( "Name of " + identity, expectedName, existingName );
    }

    public static String getName( QualifiedIdentity identity )
    {
        return world.get( identity.identity() );
    }

    public static List<String> getNames( List<QualifiedIdentity> identities )
    {
        List<String> result = new ArrayList<String>( identities.size() );
        for( QualifiedIdentity identity : identities )
        {
            final String name = getName( identity );
            assertNotNull( "Name of " + identity, name );
            result.add( name );
        }
        return result;
    }

    public static String[] allNames()
    {
        return world.values().toArray( new String[world.size()] );
    }
}
