/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.qi4j.valueserialization.stax;

import org.junit.Test;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueSerialization;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.spi.uuid.UuidIdentityGeneratorService;
import org.qi4j.test.entity.AbstractConfigurationDeserializationTest;

public class StaxConfigurationDeserializationTest
    extends AbstractConfigurationDeserializationTest
{
    @Service
    private ValueSerialization valueSerialization;

    @Override
    public void assemble( final ModuleAssembly module )
        throws AssemblyException
    {
        super.assemble( module );
        new StaxValueSerializationAssembler()
            .assemble( module );
    }

    @Test
    public void serializeTest()
    {
        ValueBuilder<ConfigSerializationConfig> builder = module.newValueBuilder( ConfigSerializationConfig.class );
        builder.prototype().name().set( "main" );
        builder.prototype().host().set( createHost() );
        builder.prototype().identity().set( "configtest" );
        ConfigSerializationConfig value = builder.newInstance();

        valueSerialization.serialize( value, System.out );
    }

    private Host createHost()
    {
        ValueBuilder<Host> builder = module.newValueBuilder( Host.class );
        builder.prototype().ip().set( "12.23.34.45" );
        builder.prototype().port().set( 1234 );
        return builder.newInstance();
    }
}
