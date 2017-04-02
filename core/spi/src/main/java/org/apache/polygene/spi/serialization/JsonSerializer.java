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

import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import javax.json.JsonValue;
import org.apache.polygene.api.common.Optional;
import org.apache.polygene.api.serialization.Serializer;

/**
 * {@literal javax.json} serializer.
 */
public interface JsonSerializer extends Serializer
{
    <T> Function<T, JsonValue> toJsonFunction( Options options );

    default <T> Function<T, JsonValue> toJsonFunction()
    {
        return object -> toJsonFunction( Options.DEFAULT ).apply( object );
    }

    default JsonValue toJson( Options options, @Optional Object object )
    {
        return toJsonFunction( options ).apply( object );
    }

    default JsonValue toJson( @Optional Object object )
    {
        return toJsonFunction( Options.DEFAULT ).apply( object );
    }

    default <T> Stream<JsonValue> toJsonEach( Options options, Stream<T> objects )
    {
        return objects.map( toJsonFunction( options ) );
    }

    default <T> Stream<JsonValue> toJsonEach( Options options, Iterable<T> objects )
    {
        return toJsonEach( options, StreamSupport.stream( objects.spliterator(), false ) );
    }

    default <T> Stream<JsonValue> toJsonEach( Options options, Object... objects )
    {
        return toJsonEach( options, Stream.of( objects ) );
    }

    default <T> Stream<JsonValue> toJsonEach( Stream<T> objects )
    {
        return objects.map( toJsonFunction( Options.DEFAULT ) );
    }

    default <T> Stream<JsonValue> toJsonEach( Iterable<T> objects )
    {
        return toJsonEach( Options.DEFAULT, StreamSupport.stream( objects.spliterator(), false ) );
    }

    default <T> Stream<JsonValue> toJsonEach( Object... objects )
    {
        return toJsonEach( Options.DEFAULT, Stream.of( objects ) );
    }
}
