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

package org.apache.polygene.valueserialization.stax;

import org.apache.polygene.api.identity.StringIdentity;
import org.apache.polygene.valueserialization.stax.assembly.StaxValueSerializationAssembler;
import org.junit.Ignore;
import org.junit.Test;
import org.apache.polygene.api.injection.scope.Service;
import org.apache.polygene.api.value.ValueBuilder;
import org.apache.polygene.api.value.ValueSerialization;
import org.apache.polygene.bootstrap.AssemblyException;
import org.apache.polygene.bootstrap.ModuleAssembly;
import org.apache.polygene.test.entity.AbstractConfigurationDeserializationTest;

@Ignore( "Complex configurations are not yet support in Stax ValueSerialization, due to handling arrays with Java serialization.")
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
        ValueBuilder<ConfigSerializationConfig> builder = valueBuilderFactory.newValueBuilder( ConfigSerializationConfig.class );
        builder.prototype().name().set( "main" );
        builder.prototype().host().set( createHost() );
        builder.prototype().identity().set( new StringIdentity( "configtest" )  );
        ConfigSerializationConfig value = builder.newInstance();

        valueSerialization.serialize( value, System.out );
    }

    private Host createHost()
    {
        ValueBuilder<Host> builder = valueBuilderFactory.newValueBuilder( Host.class );
        builder.prototype().ip().set( "12.23.34.45" );
        builder.prototype().port().set( 1234 );
        return builder.newInstance();
    }
}
