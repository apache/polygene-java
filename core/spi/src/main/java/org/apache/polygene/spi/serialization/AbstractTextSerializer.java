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

import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import org.apache.polygene.api.common.Optional;

import static java.nio.charset.StandardCharsets.UTF_8;

// START SNIPPET: text
/**
 * Base Text Serializer.
 *
 * Implementations work on Strings, this base serializer encode these strings in UTF-8 to produce bytes.
 */
public abstract class AbstractTextSerializer extends AbstractSerializer
// END SNIPPET: text
{
    public void serialize( Options options, OutputStream output, @Optional Object object )
    {
        try
        {
            StringWriter writer = new StringWriter();
            serialize( options, writer, object );
            output.write( writer.toString().getBytes( UTF_8 ) );
        }
        catch( IOException ex )
        {
            throw new UncheckedIOException( ex );
        }
    }
}
