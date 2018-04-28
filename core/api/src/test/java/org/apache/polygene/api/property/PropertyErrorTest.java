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

package org.apache.polygene.api.property;

import org.apache.polygene.api.constraint.ConstraintViolationException;
import org.apache.polygene.api.entity.EntityComposite;
import org.apache.polygene.api.unitofwork.UnitOfWork;
import org.apache.polygene.bootstrap.AssemblyException;
import org.apache.polygene.bootstrap.ModuleAssembly;
import org.apache.polygene.test.AbstractPolygeneTest;
import org.apache.polygene.test.EntityTestAssembler;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Error messages for Properties
 */
public class PropertyErrorTest
    extends AbstractPolygeneTest
{
    public void assemble( ModuleAssembly module )
    {
        new EntityTestAssembler().assemble( module );
        module.entities( PersonEntity.class );
    }

    @Test
    public void givenEntityWithNonOptionPropertyWhenInstantiatedThenException()
        throws Exception
    {
        assertThrows( ConstraintViolationException.class, () -> {
            UnitOfWork unitOfWork = unitOfWorkFactory.newUnitOfWork();
            try
            {
                PersonEntity person = unitOfWork.newEntity( PersonEntity.class );
                unitOfWork.complete();
            }
            finally
            {
                unitOfWork.discard();
            }
        } );
    }

    interface PersonEntity
        extends EntityComposite
    {
        Property<String> foo();
    }
}