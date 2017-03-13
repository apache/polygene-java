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
package org.apache.polygene.serialization.javaxxml;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.UncheckedIOException;
import java.util.Base64;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.polygene.api.PolygeneAPI;
import org.apache.polygene.api.association.AssociationStateHolder;
import org.apache.polygene.api.composite.CompositeInstance;
import org.apache.polygene.api.entity.EntityReference;
import org.apache.polygene.api.injection.scope.This;
import org.apache.polygene.api.injection.scope.Uses;
import org.apache.polygene.api.serialization.Converter;
import org.apache.polygene.api.serialization.Converters;
import org.apache.polygene.api.serialization.SerializationException;
import org.apache.polygene.api.service.ServiceDescriptor;
import org.apache.polygene.api.type.ArrayType;
import org.apache.polygene.api.type.EnumType;
import org.apache.polygene.api.type.MapType;
import org.apache.polygene.api.type.ValueCompositeType;
import org.apache.polygene.api.util.ArrayIterable;
import org.apache.polygene.api.value.ValueComposite;
import org.apache.polygene.api.value.ValueDescriptor;
import org.apache.polygene.spi.serialization.AbstractTextSerializer;
import org.apache.polygene.spi.serialization.XmlSerializer;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.polygene.api.util.Collectors.toMap;

/**
 * XML Serializer.
 */
public class JavaxXmlSerializer extends AbstractTextSerializer implements XmlSerializer
{
    private static final String NULL_ELEMENT_NAME = "null";

    @This
    private Converters converters;

    @This
    private JavaxXmlAdapters adapters;

    @Uses
    private ServiceDescriptor descriptor;

    @Override
    public <T> Function<T, Document> toXmlFunction( Options options )
    {
        return object -> doSerializeRoot( options, object );
    }

    private <T> Document doSerializeRoot( Options options, T object )
    {
        try
        {
            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            doc.setXmlVersion( "1.1" );
            doc.setXmlStandalone( true );
            Element stateElement = doc.createElement( getSettings().getRootTagName() );
            Node node = doSerialize( doc, options, object, true );
            stateElement.appendChild( node );
            doc.appendChild( stateElement );
            return doc;
        }
        catch( ParserConfigurationException ex )
        {
            throw new SerializationException( "Unable to create XML document. "
                                              + "Is your javax.xml subsystem correctly set up?", ex );
        }
    }

    private <T> Node doSerialize( Document document, Options options, T object, boolean root )
    {
        if( object == null )
        {
            return document.createElement( NULL_ELEMENT_NAME );
        }
        Class<?> objectClass = object.getClass();
        Converter<Object> converter = converters.converterFor( objectClass );
        if( converter != null )
        {
            return doSerialize( document, options, converter.toString( object ), false );
        }
        JavaxXmlAdapter<?> adapter = adapters.adapterFor( objectClass );
        if( adapter != null )
        {
            return adapter.serialize( document, object, value -> doSerialize( document, options, value, false ) );
        }
        if( EnumType.isEnum( objectClass ) )
        {
            return document.createTextNode( object.toString() );
        }
        if( ValueCompositeType.isValueComposite( objectClass ) )
        {
            return serializeValueComposite( document, options, object, root );
        }
        if( MapType.isMap( objectClass ) )
        {
            return serializeMap( document, options, (Map<?, ?>) object );
        }
        if( ArrayType.isArray( objectClass ) )
        {
            return serializeArray( document, options, object );
        }
        if( Iterable.class.isAssignableFrom( objectClass ) )
        {
            return serializeIterable( document, options, (Iterable<?>) object );
        }
        if( Stream.class.isAssignableFrom( objectClass ) )
        {
            return serializeStream( document, options, (Stream<?>) object );
        }
        // Fallback to Java Serialization in Base 64
        // Include all arrays!
        return serializeBase64( document, object );
    }

    private <T> Node serializeValueComposite( Document document, Options options, T composite, boolean root )
    {
        CompositeInstance instance = PolygeneAPI.FUNCTION_COMPOSITE_INSTANCE_OF.apply( (ValueComposite) composite );
        ValueDescriptor descriptor = (ValueDescriptor) instance.descriptor();
        AssociationStateHolder state = (AssociationStateHolder) instance.state();
        ValueCompositeType valueType = descriptor.valueType();

        Element valueElement = document.createElement( getSettings().getValueTagName() );
        valueType.properties().forEach(
            property ->
            {
                Object value = state.propertyFor( property.accessor() ).get();
                Element element = document.createElement( property.qualifiedName().name() );
                element.appendChild( doSerialize( document, options, value, false ) );
                valueElement.appendChild( element );
            } );
        valueType.associations().forEach(
            association ->
            {
                EntityReference value = state.associationFor( association.accessor() ).reference();
                Element element = document.createElement( association.qualifiedName().name() );
                element.appendChild( doSerialize( document, options, value, false ) );
                valueElement.appendChild( element );
            }
        );
        valueType.manyAssociations().forEach(
            association ->
            {
                Stream<EntityReference> value = state.manyAssociationFor( association.accessor() ).references();
                Element element = document.createElement( association.qualifiedName().name() );
                element.appendChild( doSerialize( document, options, value, false ) );
                valueElement.appendChild( element );
            }
        );
        valueType.namedAssociations().forEach(
            association ->
            {
                Map<String, EntityReference> value = state.namedAssociationFor( association.accessor() ).references()
                                                          .collect( toMap() );
                Element element = document.createElement( association.qualifiedName().name() );
                element.appendChild( doSerialize( document, options, value, false ) );
                valueElement.appendChild( element );
            }
        );
        if( !root && options.includeTypeInfo() )
        {
            valueElement.setAttribute( getSettings().getTypeInfoTagName(), valueType.primaryType().getName() );
        }
        return valueElement;
    }

    private Node serializeMap( Document document, Options options, Map<?, ?> map )
    {
        JavaxXmlSettings settings = getSettings();
        Element mapElement = document.createElement( settings.getMapTagName() );
        if( map.isEmpty() )
        {
            return mapElement;
        }
        Function<Map.Entry, Node> complexMapping = entry ->
        {
            Element entryElement = document.createElement( settings.getMapEntryTagName() );

            Element keyElement = document.createElement( "key" );
            keyElement.appendChild( doSerialize( document, options, entry.getKey(), false ) );
            entryElement.appendChild( keyElement );

            Element valueElement = document.createElement( "value" );
            valueElement.appendChild( doSerialize( document, options, entry.getValue(), false ) );
            entryElement.appendChild( valueElement );

            return entryElement;
        };

        if( map.keySet().iterator().next() instanceof CharSequence )
        {
            map.entrySet().stream()
               .map( entry ->
                     {
                         try
                         {
                             Element element = document.createElement( entry.getKey().toString() );
                             element.appendChild( doSerialize( document, options, entry.getValue(), false ) );
                             return element;
                         }
                         catch( DOMException ex )
                         {
                             // The key name cannot be encoded as a tag name, fallback to complex mapping
                             // Tag names cannot start with a digit, some characters cannot be escaped etc...
                             return complexMapping.apply( entry );
                         }
                     } )
               .forEach( mapElement::appendChild );
        }
        else
        {
            map.entrySet().stream()
               .map( complexMapping )
               .forEach( mapElement::appendChild );
        }
        return mapElement;
    }

    private <T> Node serializeArray( Document document, Options options, T object )
    {
        ArrayType valueType = ArrayType.of( object.getClass() );
        if( valueType.isArrayOfPrimitiveBytes() )
        {
            byte[] base64 = Base64.getEncoder().encode( (byte[]) object );
            return document.createCDATASection( new String( base64, UTF_8 ) );
        }
        if( valueType.isArrayOfPrimitives() )
        {
            return serializeIterable( document, options, new ArrayIterable( object ) );
        }
        return serializeStream( document, options, Stream.of( (Object[]) object ) );
    }

    private Node serializeIterable( Document document, Options options, Iterable<?> object )
    {
        return serializeStream( document, options, StreamSupport.stream( object.spliterator(), false ) );
    }

    private Node serializeStream( Document document, Options options, Stream<?> object )
    {
        JavaxXmlSettings settings = getSettings();
        Element collectionElement = document.createElement( settings.getCollectionTagName() );
        object.map( each -> doSerialize( document, options, each, false ) )
              .forEach( itemValueNode ->
                        {
                            Element itemElement = document.createElement( settings.getCollectionElementTagName() );
                            itemElement.appendChild( itemValueNode );
                            collectionElement.appendChild( itemElement );
                        } );
        return collectionElement;
    }

    private <T> Node serializeBase64( Document document, T object )
    {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        try( ObjectOutputStream out = new ObjectOutputStream( bout ) )
        {
            out.writeUnshared( object );
            byte[] bytes = Base64.getEncoder().encode( bout.toByteArray() );
            return document.createCDATASection( new String( bytes, UTF_8 ) );
        }
        catch( IOException ex )
        {
            throw new UncheckedIOException( ex );
        }
    }

    private JavaxXmlSettings getSettings()
    {
        return JavaxXmlSettings.orDefault( descriptor.metaInfo( JavaxXmlSettings.class ) );
    }
}
