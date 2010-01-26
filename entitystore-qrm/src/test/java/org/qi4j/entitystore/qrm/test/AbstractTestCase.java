/*  Copyright 2008 Edward Yakop.
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
package org.qi4j.entitystore.qrm.test;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.qi4j.entitystore.qrm.DerbyDatabaseHandler;
import org.qi4j.entitystore.qrm.entity.PersonComposite;
import org.qi4j.spi.composite.TransientDescriptor;
import org.qi4j.spi.entity.EntityState;
import org.qi4j.spi.entity.QualifiedIdentity;
import org.qi4j.spi.property.PropertyDescriptor;
import org.qi4j.test.AbstractQi4jTest;

import static org.junit.Assert.*;

public abstract class AbstractTestCase
    extends AbstractQi4jTest
{
    protected DerbyDatabaseHandler derbyDatabaseHandler;

    /**
     * Returns the jdbc connection to test db. Must not return {@code null}.
     *
     * @return The jdbc connection to test db.
     *
     * @throws SQLException Thrown if initializing connection failed.
     */
    final Connection getJDBCConnection()
        throws SQLException
    {
        return derbyDatabaseHandler.getJDBCConnection();
    }

    @Override
    public void tearDown()
        throws Exception
    {
        if( derbyDatabaseHandler != null )
        {
            derbyDatabaseHandler.shutdown();
        }

        if( unitOfWorkFactory != null && unitOfWorkFactory.currentUnitOfWork() != null )
        {
            unitOfWorkFactory.currentUnitOfWork().discard();
        }

        super.tearDown();
    }

    @Before
    public void setUp()
        throws Exception
    {
        derbyDatabaseHandler = new DerbyDatabaseHandler();
        super.setUp();
    }

    protected Map<String, String> createTestData( final String firstName, final String lastName )
    {
        final Map<String, String> data = new HashMap<String, String>();
        data.put( "FIRST_NAME", firstName );
        data.put( "LAST_NAME", lastName );
        return data;
    }

    protected void assertPersonEqualsInDatabase( final String identity, final Map<String, ?> values )
    {
        final int count = derbyDatabaseHandler.executeStatement( "select * from person where id = '" + identity + "'", new DerbyDatabaseHandler.ResultSetCallback()
        {
            public void row( final ResultSet rs )
                throws SQLException
            {
                org.junit.Assert.assertEquals( "id", identity, rs.getString( "id" ) );
                assertContainsValues( rs, values );
            }
        } );
        org.junit.Assert.assertEquals( "Person with Id " + identity, 1, count );
    }

    private void assertContainsValues( final ResultSet rs, final Map<String, ?> values )
        throws SQLException
    {
        if( values == null )
        {
            return;
        }

        for( final Map.Entry<String, ?> entry : values.entrySet() )
        {
            final String name = entry.getKey();
            org.junit.Assert.assertEquals( name, entry.getValue(), rs.getString( name ) );
        }
    }

    protected static void checkEntityStateProperties( final TransientDescriptor compositeBinding,
                                                      final EntityState state,
                                                      final boolean checkAll
    )
    {
/*
        assertNotNull( "identity", state.qualifiedIdentity() );
        assertNotNull( "identity", state.qualifiedIdentity().identity() );
*/
        if( !checkAll )
        {
            return;
        }

        for( final PropertyDescriptor propertyDescriptor : compositeBinding.state().properties() )
        {
            final String propertyName = propertyDescriptor.qualifiedName().name();
            if( "identity".equals( propertyName ) )
            {
                continue;
            }
/*
            final Property property = (Property) state.getProperty( null );

            assertNotNull( "Property [" + propertyName + ": " + propertyDescriptor.type() + "] is not found.", property );
*/
        }
    }

    protected void assertPersonEntityStateEquals( final String id,
                                                  final String firstName,
                                                  final String lastName,
                                                  final EntityState state
    )
    {
        assertNotNull( state );
        final QualifiedIdentity qualifiedIdentity = null; //state.qualifiedIdentity();

        assertNotNull( "identity", qualifiedIdentity );
        org.junit.Assert.assertEquals( "identity", id, qualifiedIdentity.identity() );

/*
        org.junit.Assert.assertEquals( "identity", id, state.getProperty( QualifiedName.fromQN( "identity" ) ) );
        org.junit.Assert.assertEquals( "firstName", firstName, state.getProperty( QualifiedName.fromQN( "firstName" ) ) );
        org.junit.Assert.assertEquals( "lastName", lastName, state.getProperty( QualifiedName.fromQN( "lastName" ) ) );
*/
    }

    protected void assertPersonEquals( final String id,
                                       final String firstName,
                                       final String lastName,
                                       final PersonComposite person
    )
    {
        org.junit.Assert.assertEquals( "identity", id, person.identity().get() );
        org.junit.Assert.assertEquals( "firstName", firstName, person.firstName().get() );
        org.junit.Assert.assertEquals( "lastName", lastName, person.lastName().get() );
    }

    protected QualifiedIdentity id( final String id )
    {
        return new QualifiedIdentity( id, PersonComposite.class );
    }
}
