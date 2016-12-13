/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 */
package org.apache.polygene.api.unitofwork;

import org.junit.Test;
import org.apache.polygene.api.entity.EntityBuilderTemplate;
import org.apache.polygene.api.entity.EntityComposite;
import org.apache.polygene.api.property.Property;
import org.apache.polygene.bootstrap.AssemblyException;
import org.apache.polygene.bootstrap.ModuleAssembly;
import org.apache.polygene.test.AbstractPolygeneTest;
import org.apache.polygene.test.EntityTestAssembler;

/**
 * TODO
 */
public class UnitOfWorkTemplateTest
    extends AbstractPolygeneTest
{
    @Override
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        new EntityTestAssembler().assemble( module );
        module.entities( TestEntity.class );
    }

    @Test
    public void testTemplate()
        throws UnitOfWorkCompletionException
    {
        new UnitOfWorkTemplate<Void, RuntimeException>()
        {
            @Override
            protected Void withUnitOfWork( UnitOfWork uow )
                throws RuntimeException
            {
                new EntityBuilderTemplate<TestEntity>( TestEntity.class )
                {
                    @Override
                    protected void build( TestEntity prototype )
                    {
                        prototype.name().set( "Rickard" );
                    }
                }.newInstance( module.instance() );

                return null;
            }
        }.withModule( module.instance() );
    }

    interface TestEntity
        extends EntityComposite
    {
        Property<String> name();
    }
}
