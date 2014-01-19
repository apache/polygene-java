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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.entity.Identity;
import org.qi4j.test.indexing.model.Nameable;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.qi4j.functional.Iterables.toList;

public class NameableAssert
{
    // id -> name
    private static final Map<String, String> world = new HashMap<>();

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
        assertThat( expectedNames.length + " entries(" + expectedNames.length + ", got " + getNames( references ) + ")",
                    references.size(),
                    equalTo( expectedNames.length ) );
        List<String> sortedNames = getNames( references );
        final List<String> expectedSorted = java.util.Arrays.asList( expectedNames );
        if( sort )
        {
            Collections.sort( sortedNames );
            Collections.sort( expectedSorted );
        }
        assertThat( "names", sortedNames, equalTo( expectedSorted ) );
    }

    public static void trace( Nameable nameable )
    {
        world.put( ( (Identity) nameable ).identity().get(), nameable.name().get() );
    }

    public static void assertName( String expectedName, EntityReference reference )
    {
        final String existingName = getName( reference );
        assertThat( "Name of " + reference, existingName, equalTo( expectedName ) );
    }

    public static String getName( EntityReference reference )
    {
        return world.get( reference.identity() );
    }

    public static List<String> getNames( List<EntityReference> references )
    {
        List<String> result = new ArrayList<>( references.size() );
        for( EntityReference reference : references )
        {
            final String name = getName( reference );
            assertThat( "Name of " + reference, name, notNullValue() );
            result.add( name );
        }
        return result;
    }

    public static String[] allNames()
    {
        return world.values().toArray( new String[ world.size() ] );
    }

    public static void verifyUnorderedResults( final Iterable<? extends Nameable> results, final String... names )
    {
        List<String> expected = new ArrayList<>( Arrays.asList( names ) );
        List<String> unexpected = new ArrayList<>();
        for( Nameable result : results )
        {
            String name = result.name().get();
            if( !expected.remove( name ) )
            {
                unexpected.add( name );
            }
        }
        if( !unexpected.isEmpty() || !expected.isEmpty() )
        {
            String message = "";
            if( !unexpected.isEmpty() )
            {
                message += unexpected + " returned but not expected\n";
            }
            if( !expected.isEmpty() )
            {
                message += expected + " expected but not returned\n";
            }
            fail( message.substring( 0, message.length() - 1 ) );
        }
    }

    public static void verifyOrderedResults( final Iterable<? extends Nameable> results, final String... names )
    {
        List<String> expected = new ArrayList<>( Arrays.asList( names ) );
        List<String> actual = new ArrayList<>();
        for( Nameable result : results )
        {
            actual.add( result.name().get() );
        }

        assertThat( "Result is incorrect", actual, equalTo( expected ) );
    }

    private NameableAssert()
    {
    }
}
