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
package org.apache.polygene.serialization.msgpack;

import java.util.Base64;
import org.apache.polygene.bootstrap.ModuleAssembly;
import org.apache.polygene.test.serialization.AbstractPlainValueSerializationTest;
import org.msgpack.core.MessagePack;

public class MessagePackPlainValueSerializationTest extends AbstractPlainValueSerializationTest
{
    @Override
    public void assemble( ModuleAssembly module )
    {
        new MessagePackSerializationAssembler().withMessagePackSettings( withTestSettings( new MessagePackSettings() ) )
                                               .assemble( module );
    }

    @Override
    protected String getSingleStringRawState( String state ) throws Exception
    {
        return MessagePack.newDefaultUnpacker( Base64.getDecoder().decode( state ) )
                          .unpackValue().asStringValue().asString();
    }
}
