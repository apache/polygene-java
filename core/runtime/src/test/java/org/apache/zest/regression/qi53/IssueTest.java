/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.zest.regression.qi53;

import org.junit.Test;
import org.apache.zest.api.composite.TransientBuilder;
import org.apache.zest.api.composite.TransientBuilderFactory;
import org.apache.zest.api.composite.TransientComposite;
import org.apache.zest.api.injection.scope.Structure;
import org.apache.zest.api.injection.scope.This;
import org.apache.zest.api.mixin.Mixins;
import org.apache.zest.api.property.Immutable;
import org.apache.zest.api.property.Property;
import org.apache.zest.bootstrap.AssemblyException;
import org.apache.zest.bootstrap.ModuleAssembly;
import org.apache.zest.test.AbstractZestTest;

import static org.junit.Assert.assertEquals;

public class IssueTest
    extends AbstractZestTest
{
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.transients( CostPerUnitComposite.class );
    }

    @Test
    public void genericPropertiesAndParameters()
        throws SecurityException, NoSuchMethodException
    {
        TransientBuilder<CostPerUnitComposite> builder = module.newTransientBuilder( CostPerUnitComposite.class );
        builder.prototype().unit().set( new Unit<Integer>( 10 ) );
        CostPerUnitComposite test = builder.newInstance();
        assertEquals( 10, test.unit().get().value );
        assertEquals( 50, test.toCostPer( new Unit<Integer>( 50 ) ).unit().get().value );
    }

    public interface CostPerUnit
    {
        @Immutable
        Property<Unit<?>> unit();

        CostPerUnit toCostPer( Unit<?> unit );
    }

    public static class Unit<T>
    {
        private T value;

        public Unit( T value )
        {
            this.value = value;
        }

        T get()
        {
            return value;
        }
    }

    public static abstract class CostPerUnitMixin
        implements CostPerUnit
    {

        @This
        CostPerUnit costPerUnit;
        @Structure
        TransientBuilderFactory builderFactory;

        public CostPerUnit toCostPer( Unit<?> unit )
        {
            TransientBuilder<CostPerUnitComposite> builder =
                builderFactory.newTransientBuilder( CostPerUnitComposite.class );

            builder.prototype().unit().set( unit );
            return builder.newInstance();
        }
    }

    @Mixins( { CostPerUnitMixin.class } )
    public interface CostPerUnitComposite
        extends CostPerUnit, TransientComposite
    {
    }
}