/*
 * Copyright (c) 2012, Paul Merlin. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 *     You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.qi4j.valueserialization.orgjson;

import java.io.InputStream;
import java.io.OutputStream;
import org.qi4j.api.structure.Application;
import org.qi4j.api.structure.Module;
import org.qi4j.api.type.ValueType;
import org.qi4j.api.value.ValueSerialization;
import org.qi4j.api.value.ValueSerializationException;
import org.qi4j.functional.Function;
import org.qi4j.functional.Function2;

/**
 * ValueSerialization producing and consuming JSON documents using org.json.
 *
 * <p>
 *     This class is used internally by the Qi4j Runtime to provide default ValueSerialization when no
 *     ValueSerialization Service is available.
 * </p>
 * <p>
 *     In application code, prefer the use of {@link OrgJsonValueSerializationService}.
 * </p>
 */
public class OrgJsonValueSerialization
    implements ValueSerialization
{

    private final OrgJsonValueSerializer serializer;
    private final OrgJsonValueDeserializer deserializer;

    public OrgJsonValueSerialization( Application application, Module module, final Module valuesModule )
    {
        this.serializer = new OrgJsonValueSerializer();
        this.deserializer = new OrgJsonValueDeserializer( application, module, new Function<Application, Module>()
        {
            @Override
            public Module map( Application from )
            {
                return valuesModule;
            }
        } );
    }

    @Override
    public <T> Function<T, String> serialize()
    {
        return serializer.serialize();
    }

    @Override
    public <T> Function<T, String> serialize( Options options )
    {
        return serializer.serialize( options );
    }

    @Override
    @Deprecated
    public <T> Function<T, String> serialize( boolean includeTypeInfo )
    {
        return serializer.serialize( includeTypeInfo );
    }

    @Override
    public String serialize( Object object )
        throws ValueSerializationException
    {
        return serializer.serialize( object );
    }

    @Override
    public String serialize( Options options, Object object )
        throws ValueSerializationException
    {
        return serializer.serialize( options, object );
    }

    @Override
    @Deprecated
    public String serialize( Object object, boolean includeTypeInfo )
        throws ValueSerializationException
    {
        return serializer.serialize( object, includeTypeInfo );
    }

    @Override
    public void serialize( Object object, OutputStream output )
        throws ValueSerializationException
    {
        serializer.serialize( object, output );
    }

    @Override
    public void serialize( Options options, Object object, OutputStream output )
        throws ValueSerializationException
    {
        serializer.serialize( options, object, output );
    }

    @Override
    @Deprecated
    public void serialize( Object object, OutputStream output, boolean includeTypeInfo )
        throws ValueSerializationException
    {
        serializer.serialize( object, output, includeTypeInfo );
    }

    @Override
    public <T> Function<String, T> deserialize( Class<T> type )
    {
        return deserializer.deserialize( type );
    }

    @Override
    public <T> Function<String, T> deserialize( ValueType valueType )
    {
        return deserializer.deserialize( valueType );
    }

    @Override
    public <T> Function2<ValueType, String, T> deserialize()
    {
        return deserializer.deserialize();
    }

    @Override
    public <T> T deserialize( Class<?> type, String input )
        throws ValueSerializationException
    {
        return deserializer.deserialize( type, input );
    }

    @Override
    public <T> T deserialize( ValueType type, String input )
        throws ValueSerializationException
    {
        return deserializer.deserialize( type, input );
    }

    @Override
    public <T> T deserialize( Class<?> type, InputStream input )
        throws ValueSerializationException
    {
        return deserializer.deserialize( new ValueType( type ), input );
    }

    @Override
    public <T> T deserialize( ValueType type, InputStream input )
        throws ValueSerializationException
    {
        return deserializer.deserialize( type, input );
    }
}
