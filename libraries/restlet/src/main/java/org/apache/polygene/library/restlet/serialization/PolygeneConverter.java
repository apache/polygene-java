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
package org.apache.polygene.library.restlet.serialization;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.polygene.api.object.ObjectFactory;
import org.restlet.data.MediaType;
import org.restlet.data.Preference;
import org.restlet.engine.converter.ConverterHelper;
import org.restlet.engine.resource.VariantInfo;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.Resource;

/**
 * Converter between Apache Polygene and JSON.
 */
public class PolygeneConverter extends ConverterHelper
{
    /**
     * Variant with media type application/json.
     */
    private static final VariantInfo VARIANT_JSON = new VariantInfo( MediaType.APPLICATION_JSON );
    private static final VariantInfo VARIANT_WWW_FORM_URLENCODED = new VariantInfo( MediaType.APPLICATION_WWW_FORM );

    private final ObjectFactory objectFactory;

    public PolygeneConverter( ObjectFactory objectFactory )
    {
        this.objectFactory = objectFactory;
    }

    /**
     * Creates the marshaling {@link JsonRepresentation}.
     *
     * @param mediaType The target media type.
     * @param source    The source object to marshal.
     *
     * @return The marshaling {@link JsonRepresentation}.
     */
    protected <T> Representation create( MediaType mediaType, T source )
    {
        //noinspection unchecked
        return objectFactory.newObject( JsonRepresentation.class, source );
    }

    /**
     * Creates the unmarshaling {@link JsonRepresentation}.
     *
     * @param source      The source representation to unmarshal.
     * @param objectClass The object class to instantiate.
     *
     * @return The unmarshaling {@link JsonRepresentation}.
     */
    protected <T> Representation create( Representation source, Class<T> objectClass )
    {
        if( VARIANT_WWW_FORM_URLENCODED.isCompatible( source ) )
        {
            return objectFactory.newObject( FormRepresentation.class, source, objectClass );
        }
        else if( VARIANT_JSON.isCompatible( source ) )
        {
            //noinspection unchecked
            return objectFactory.newObject( JsonRepresentation.class, source, objectClass );
        }
        return null;
    }

    public ObjectFactory getObjectFactory()
    {
        return objectFactory;
    }

    @Override
    public List<Class<?>> getObjectClasses( Variant source )
    {
        List<Class<?>> result = new ArrayList<>();

        if( isCompatible( source ) )
        {
            result = addObjectClass( result, Object.class );
            result = addObjectClass( result, JsonRepresentation.class );
        }

        return result;
    }

    @Override
    public List<VariantInfo> getVariants( Class<?> source )
    {
        List<VariantInfo> result = new ArrayList<>();

        if( source != null )
        {
            result = addVariant( result, VARIANT_JSON );
            result = addVariant( result, VARIANT_WWW_FORM_URLENCODED );
        }

        return result;
    }

    /**
     * Indicates if the given variant is compatible with the media types
     * supported by this converter.
     *
     * @param variant The variant.
     *
     * @return True if the given variant is compatible with the media types
     * supported by this converter.
     */
    protected boolean isCompatible( Variant variant )
    {
        //noinspection SimplifiableIfStatement
        if( variant == null )
        {
            return false;
        }

        return VARIANT_JSON.isCompatible( variant ) ||
               VARIANT_WWW_FORM_URLENCODED.isCompatible( variant )
            ;
    }

    @Override
    public float score( Object source, Variant target, Resource resource )
    {
        float result;

        if( source instanceof JsonRepresentation<?> )
        {
            result = 1.0F;
        }
        else
        {
            if( target == null )
            {
                result = 0.5F;
            }
            else if( isCompatible( target ) )
            {
                result = 0.8F;
            }
            else
            {
                result = 0.5F;
            }
        }

        return result;
    }

    @Override
    public <T> float score( Representation source, Class<T> target,
                            Resource resource
    )
    {
        float result = -1.0F;

        if( source instanceof JsonRepresentation<?> )
        {
            result = 1.0F;
        }
        else if( ( target != null )
                 && JsonRepresentation.class.isAssignableFrom( target ) )
        {
            result = 1.0F;
        }
        else if( isCompatible( source ) )
        {
            result = 0.8F;
        }

        return result;
    }

    @SuppressWarnings( "unchecked" )
    @Override
    public <T> T toObject( Representation source, Class<T> target, Resource resources )
        throws IOException
    {
        Object result = null;

        Representation representation = null;
        if( isCompatible( source ) )
        {
            representation = create( source, target );
        }

        if( representation != null )
        {
            // Handle the conversion
            if( ( target != null )
                && JsonRepresentation.class.isAssignableFrom( target ) )
            {
                result = representation;
            }
            else
            {
                if( representation instanceof JsonRepresentation )
                {
                    result = ( (JsonRepresentation) representation ).getObject();
                }
                if( representation instanceof FormRepresentation )
                {
                    result = ( (FormRepresentation) representation ).getObject();
                }
            }
        }

        return (T) result;
    }

    @Override
    public Representation toRepresentation( Object source, Variant target, Resource resource )
    {
        Representation result = null;

        if( source instanceof JsonRepresentation )
        {
            result = (JsonRepresentation<?>) source;
        }
        else
        {
            if( target.getMediaType() == null )
            {
                target.setMediaType( MediaType.APPLICATION_JSON );
            }
            if( isCompatible( target ) )
            {
                result = create( target.getMediaType(), source );
            }
        }

        return result;
    }

    @Override
    public <T> void updatePreferences( List<Preference<MediaType>> preferences,
                                       Class<T> entity
    )
    {
        updatePreferences( preferences, MediaType.APPLICATION_JSON, 1.0F );
    }
}
