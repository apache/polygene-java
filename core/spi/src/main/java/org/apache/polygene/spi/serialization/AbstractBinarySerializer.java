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
package org.apache.polygene.spi.serialization;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.util.Base64;
import org.apache.polygene.api.common.Optional;

import static java.nio.charset.StandardCharsets.UTF_8;

// START SNIPPET: binary
/**
 * Base Binary Serializer.
 *
 * Implementations work on bytes, this base serializer encode these bytes in Base64 to produce Strings.
 */
public abstract class AbstractBinarySerializer extends AbstractSerializer
// END SNIPPET: binary
{
    @Override
    public void serialize( Options options, Writer writer, @Optional Object object )
    {
        try
        {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            serialize( options, output, object );
            byte[] base64 = Base64.getEncoder().encode( output.toByteArray() );
            writer.write( new String( base64, UTF_8 ) );
        }
        catch( IOException ex )
        {
            throw new UncheckedIOException( ex );
        }
    }
}
