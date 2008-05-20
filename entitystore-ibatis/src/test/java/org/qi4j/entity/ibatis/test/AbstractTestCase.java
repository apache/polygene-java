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
package org.qi4j.entity.ibatis.test;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import org.qi4j.composite.Composite;
import org.qi4j.entity.ibatis.internal.IBatisEntityState;
import org.qi4j.entity.ibatis.entity.PersonComposite;
import org.qi4j.entity.ibatis.DerbyDatabaseHandler;
import org.qi4j.runtime.composite.CompositeContext;
import org.qi4j.runtime.structure.ModuleContext;
import org.qi4j.spi.composite.CompositeBinding;
import org.qi4j.spi.entity.EntityStatus;
import org.qi4j.spi.entity.QualifiedIdentity;
import org.qi4j.spi.structure.CompositeDescriptor;
import org.qi4j.test.AbstractQi4jTest;
import org.junit.Before;

/**
 * @author edward.yakop@gmail.com
 * @since 0.1.0
 */
public abstract class AbstractTestCase extends AbstractQi4jTest
{
    protected DerbyDatabaseHandler derbyDatabaseHandler;


    /**
     * Returns the jdbc connection to test db. Must not return {@code null}.
     *
     * @return The jdbc connection to test db.
     * @throws SQLException Thrown if initializing connection failed.
     * @since 0.1.0
     */
    final Connection getJDBCConnection()
        throws SQLException
    {
        return derbyDatabaseHandler.getJDBCConnection();
    }

    @Override public void tearDown() throws Exception
    {
        if (derbyDatabaseHandler!=null)
            derbyDatabaseHandler.shutdown();

        if (unitOfWorkFactory!=null && unitOfWorkFactory.currentUnitOfWork()!=null)
            unitOfWorkFactory.currentUnitOfWork().discard();
        
        super.tearDown();
    }

    protected IBatisEntityState newPersonEntityState( final Map<String, Object> initialValues )
    {
        final CompositeDescriptor compositeDescriptor = getCompositeDescriptor( PersonComposite.class );

        return new IBatisEntityState( compositeDescriptor,
                                      new QualifiedIdentity( "1", PersonComposite.class.getName() ),
                                      initialValues,
                                      0L,
                                      EntityStatus.NEW );
        // return new IBatisEntityState( new QualifiedIdentity( "1", PersonComposite.class.getName() ), getCompositeBinding( PersonComposite.class ), initialValues, EntityStatus.NEW, statusNew, unitOfWork, dao );
    }

    protected CompositeDescriptor getCompositeDescriptor( final Class<? extends Composite> compositeType )
    {
        final CompositeContext compositeContext = getCompositeContext( compositeType );
        return compositeContext.getCompositeResolution().getCompositeDescriptor();
    }

    protected CompositeBinding getCompositeBinding( final Class<? extends Composite> compositeType )
    {
        final CompositeContext compositeContext = getCompositeContext( compositeType );
        final CompositeBinding compositeBinding = compositeContext.getCompositeBinding();
        junit.framework.Assert.assertNotNull( compositeBinding );
        return compositeBinding;
    }

    protected CompositeContext getCompositeContext( final Class<? extends Composite> compositeType )
    {
        final ModuleContext moduleContext = moduleInstance.getModuleContext();
        final CompositeContext compositeContext = moduleContext.getCompositeContext( compositeType );
        junit.framework.Assert.assertNotNull( compositeContext );
        return compositeContext;
    }

    @Before public void setUp() throws Exception
    {
        derbyDatabaseHandler = new DerbyDatabaseHandler();
        super.setUp();
    }
}
