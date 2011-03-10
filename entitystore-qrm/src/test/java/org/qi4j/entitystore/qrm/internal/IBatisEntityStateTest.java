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
package org.qi4j.entitystore.qrm.internal;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Map;
import org.junit.Ignore;
import org.junit.Test;
import org.qi4j.api.common.QualifiedName;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.entitystore.qrm.entity.HasFirstName;
import org.qi4j.entitystore.qrm.entity.HasLastName;
import org.qi4j.entitystore.qrm.entity.PersonComposite;
import org.qi4j.spi.entity.EntityState;
import org.qi4j.test.AbstractQi4jTest;

import static junit.framework.Assert.*;

@Ignore( "Until store is upgraded" )
public final class IBatisEntityStateTest
    extends AbstractQi4jTest
{
    private static final String DEFAULT_FIRST_NAME = "Edward";
    private static final String DEFAULT_LAST_NAME = "Yakop";

    @Test
    public void usesGivenFirstNameProperty()
        throws NoSuchMethodException
    {
        final Map<QualifiedName, Object> janeValues = Collections.<QualifiedName, Object>singletonMap( QualifiedName.fromQN( "person:firstName" ), "Jane" );
        final EntityState jane = newPersonEntityState( janeValues );
        final String firstNameProperty = getPropertyValue( jane, HasFirstName.class, "firstName" );
        assertNotNull( firstNameProperty );

        assertEquals( janeValues.get( QualifiedName.fromQN( "person:firstName" ) ), firstNameProperty );
    }

    private String getPropertyValue( final EntityState person, final Class<?> type, final String propertyName )
        throws NoSuchMethodException
    {
        final Method method = type.getMethod( propertyName );
        return (String) person.getProperty( QualifiedName.fromQN( method.getName() ) );
    }

    public final void assemble( final ModuleAssembly module )
        throws AssemblyException
    {
        module.entities( PersonComposite.class );

        module.forMixin( HasFirstName.class ).declareDefaults().firstName().set( DEFAULT_FIRST_NAME );
        module.forMixin( HasLastName.class ).declareDefaults().lastName().set( DEFAULT_LAST_NAME );
    }

    protected EntityState newPersonEntityState( final Map<QualifiedName, Object> initialValues )
    {

        return null; /* new QrmEntityState( spi.getEntityDescriptor( PersonComposite.class, moduleInstance ).entityType(),
                                      new QualifiedIdentity( "1", PersonComposite.class ),
                                      initialValues,
                                      0L,
                                      System.currentTimeMillis(),
                                      EntityStatus.NEW ); */
    }
}
