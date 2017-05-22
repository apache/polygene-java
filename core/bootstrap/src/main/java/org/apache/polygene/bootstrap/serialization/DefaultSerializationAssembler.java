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
 */
package org.apache.polygene.bootstrap.serialization;

import org.apache.polygene.api.serialization.Converters;
import org.apache.polygene.api.serialization.Deserializer;
import org.apache.polygene.api.serialization.Serialization;
import org.apache.polygene.api.serialization.Serializer;
import org.apache.polygene.bootstrap.Assembler;
import org.apache.polygene.bootstrap.ModuleAssembly;
import org.apache.polygene.serialization.javaxjson.JavaxJsonAdapters;
import org.apache.polygene.serialization.javaxjson.JavaxJsonFactories;
import org.apache.polygene.serialization.javaxjson.JavaxJsonSerialization;
import org.apache.polygene.spi.serialization.JsonDeserializer;
import org.apache.polygene.spi.serialization.JsonSerialization;
import org.apache.polygene.spi.serialization.JsonSerializer;

public class DefaultSerializationAssembler
    implements Assembler
{
    public static final String IDENTITY = "default-serialization";

    @Override
    public void assemble( ModuleAssembly module )
    {
        module.services( JavaxJsonSerialization.class )
              .withTypes( Serialization.class,
                          Serializer.class, Deserializer.class,
                          Converters.class,
                          JsonSerialization.class,
                          JsonSerializer.class, JsonDeserializer.class,
                          JavaxJsonAdapters.class,
                          JavaxJsonFactories.class )
              .identifiedBy( IDENTITY )
              .taggedWith( Serialization.Format.JSON );
    }
}
