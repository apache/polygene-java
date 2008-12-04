/*
 * Copyright 2008 Niclas Hedhman.
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
package org.qi4j.library.spaces.tests;

import java.io.Serializable;
import java.util.Iterator;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.qi4j.library.spaces.Space;
import org.qi4j.library.spaces.SpaceTransaction;
import org.qi4j.test.AbstractQi4jTest;

public abstract class SpacesTestRig extends AbstractQi4jTest
{
    public SpacesTestRig()
    {
    }

    @After
    public void tearDown()
        throws Exception
    {
        try
        {
            // need to remove all entries in the JavaSpaces...
            SpaceTransaction transaction = space().newTransaction();
            while( space().takeIfExists( null ) != null )
            {
                // empty
            }
            transaction.commit();
        }
        catch( Exception e )
        {
            e.printStackTrace();
        }
        finally
        {
            super.tearDown();
        }
    }


    protected abstract Space space();

    @Test
    public void whenNoTransactionAndWriteCityThenTakeCityExpectCityGoneFromSpace()
        throws Exception
    {
        City kualaLumpur = createKualaLumpur();
        space().write( "123", kualaLumpur );
        Assert.assertNotNull( space().readIfExists( "123" ) );
        City clone = (City) space().takeIfExists( "123" );
        Assert.assertNotNull( clone );
        Assert.assertNull( space().readIfExists( "123" ) );
    }

    @Test
    public void whenTransactionAndWriteCityThenTakeCityExpectCityGoneFromSpace()
        throws Exception
    {
        SpaceTransaction transaction = space().newTransaction();
        City kualaLumpur = createKualaLumpur();
        space().write( "123", kualaLumpur );
        Assert.assertNotNull( space().readIfExists( "123" ) );
        City clone = (City) space().takeIfExists( "123" );
        Assert.assertNotNull( clone );
        Assert.assertNull( space().readIfExists( "123" ) );
        transaction.commit();
    }

    @Test
    public void whenTransactionAndWriteCityButAbortTransactionExpectNoEntryInSpace()
        throws Exception
    {
        SpaceTransaction transaction = space().newTransaction();
        Assert.assertNull( space().readIfExists( "123" ) );
        City kualaLumpur = createKualaLumpur();
        space().write( "123", kualaLumpur );
        Assert.assertNotNull( space().readIfExists( "123" ) );
        transaction.abort();
        Assert.assertNull( space().readIfExists( "123" ) );
    }

    @Test
    public void whenTransactionAndWriteCityAndCommitThenTakeCityButAbortExpectCityStillInSpace()
        throws Exception
    {
        SpaceTransaction transaction = space().newTransaction();
        City kualaLumpur = createKualaLumpur();
        space().write( "123", kualaLumpur );
        Assert.assertNotNull( space().readIfExists( "123" ) );
        transaction.commit();
        transaction = space().newTransaction();
        City clone = (City) space().takeIfExists( "123" );
        Assert.assertNotNull( clone );
        Assert.assertNull( space().readIfExists( "123" ) );
        transaction.abort();
        Assert.assertNotNull( space().readIfExists( "123" ) );
    }

    @Test
    public void whenAddingSixCitiesExceptToIterateThem()
        throws Exception
    {
        SpaceTransaction transaction = space().newTransaction();
        City kualaLumpur = createKualaLumpur();
        space().write( "123", kualaLumpur );
        space().write( "123", kualaLumpur );
        space().write( "123", kualaLumpur );
        space().write( "123", kualaLumpur );
        space().write( "123", kualaLumpur );
        space().write( "123", kualaLumpur );
        transaction.commit();
        Iterator it = space().iterator();
        int count = 0;
        while( it.hasNext() )
        {
            count++;
            System.out.println( "City:" + it.next() );
        }
        Assert.assertEquals( 6, count );
    }

    private City createKualaLumpur()
    {
        Country country = new Country();
        country.name = "Malaysia";
        City city = new City();
        city.name = "Kuala Lumpur";
        city.country = country;
        return city;
    }

    public static class City
        implements Serializable
    {
        Country country;
        String name;

        public String toString()
        {
            return name + ", " + country;
        }
    }

    public static class Country
        implements Serializable
    {
        String name;

        public String toString()
        {
            return name;
        }
    }
}
