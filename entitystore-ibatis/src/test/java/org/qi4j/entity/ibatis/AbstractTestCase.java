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
package org.qi4j.entity.ibatis;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import org.qi4j.composite.Composite;
import org.qi4j.entity.UnitOfWork;
import org.qi4j.entity.ibatis.dbInitializer.DBInitializerInfo;
import org.qi4j.entity.ibatis.internal.IBatisEntityState;
import org.qi4j.runtime.composite.CompositeContext;
import org.qi4j.runtime.structure.ModuleContext;
import org.qi4j.spi.composite.CompositeBinding;
import org.qi4j.spi.entity.EntityStatus;
import org.qi4j.spi.entity.QualifiedIdentity;
import org.qi4j.spi.structure.CompositeDescriptor;
import org.qi4j.test.AbstractQi4jTest;

/**
 * @author edward.yakop@gmail.com
 * @since 0.1.0
 */
public abstract class AbstractTestCase extends AbstractQi4jTest
{
    private static final String SCHEMA_FILE = "testDbSchema.sql";
    private static final String DATA_FILE = "testDbData.sql";

    protected UnitOfWork uow;
    protected final DerbyDatabaseHandler derbyDatabaseHandler;

    protected AbstractTestCase()
    {
        derbyDatabaseHandler = new DerbyDatabaseHandler();
    }

    /**
     * Construct a new db initializer info.
     *
     * @return a new db initializer info.
     * @since 0.1.0
     */
    protected final DBInitializerInfo newDbInitializerInfo()
    {
        return derbyDatabaseHandler.newDbInitializerInfo( SCHEMA_FILE, DATA_FILE );
    }


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

    @Override
    public void setUp()
        throws Exception
    {
        super.setUp();

        uow = unitOfWorkFactory.newUnitOfWork();
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
}
