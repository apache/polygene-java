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

import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.util.Base64;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.apache.polygene.api.PolygeneAPI;
import org.apache.polygene.api.association.AssociationStateHolder;
import org.apache.polygene.api.common.Optional;
import org.apache.polygene.api.composite.Composite;
import org.apache.polygene.api.composite.CompositeInstance;
import org.apache.polygene.api.composite.StatefulAssociationCompositeDescriptor;
import org.apache.polygene.api.entity.EntityReference;
import org.apache.polygene.api.injection.scope.Structure;
import org.apache.polygene.api.injection.scope.This;
import org.apache.polygene.api.injection.scope.Uses;
import org.apache.polygene.api.mixin.Initializable;
import org.apache.polygene.api.serialization.ConvertedBy;
import org.apache.polygene.api.serialization.Converter;
import org.apache.polygene.api.serialization.Converters;
import org.apache.polygene.api.serialization.SerializationException;
import org.apache.polygene.api.service.ServiceDescriptor;
import org.apache.polygene.api.structure.Module;
import org.apache.polygene.api.type.ArrayType;
import org.apache.polygene.api.type.EnumType;
import org.apache.polygene.api.type.MapType;
import org.apache.polygene.api.type.StatefulAssociationValueType;
import org.apache.polygene.spi.serialization.AbstractTextSerializer;
import org.apache.polygene.spi.serialization.XmlSerializer;
import org.apache.polygene.spi.util.ArrayIterable;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.polygene.api.util.Collectors.toMap;

/**
 * XML Serializer.
 */
public class JavaxXmlSerializer extends AbstractTextSerializer
    implements XmlSerializer, Initializable
{
    private static final String NULL_ELEMENT_NAME = "null";

    @This
    private JavaxXmlFactories xmlFactories;

    @This
    private Converters converters;

    @This
    private JavaxXmlAdapters adapters;

    @Uses
    private ServiceDescriptor descriptor;

    @Structure
    private Module module;

    private JavaxXmlSettings settings;

    @Override
    public void initialize() throws Exception
    {
        settings = JavaxXmlSettings.orDefault( descriptor.metaInfo( JavaxXmlSettings.class ) );
    }

    @Override
    public void serialize( Options options, Writer writer, @Optional Object object )
    {
        Document xmlDocument = toXml( options, object );
        if( xmlDocument == null )
        {
            return;
        }
        try
        {
            // We want plain text nodes to be serialized without surrounding elements
            if( xmlDocument.getNodeType() == Node.TEXT_NODE )
            {
                writer.write( xmlDocument.getNodeValue() );
            }
            else
            {
                xmlFactories.serializationTransformer().transform( new DOMSource( xmlDocument ),
                                                                   new StreamResult( writer ) );
            }
        }
        catch( IOException ex )
        {
            throw new UncheckedIOException( ex );
        }
        catch( TransformerException ex )
        {
            throw new SerializationException( "Unable to transform XML Document to String", ex );
        }
    }

    @Override
    public <T> Function<T, Document> toXmlFunction( Options options )
    {
        return object -> doSerializeRoot( options, object );
    }

    private <T> Document doSerializeRoot( Options options, T object )
    {
        Document doc = xmlFactories.newDocumentForSerialization();
        Element stateElement = doc.createElement( settings.getRootTagName() );
        Node node = doSerialize( doc, options, object, true );
        stateElement.appendChild( node );
        doc.appendChild( stateElement );
        return doc;
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
        if( StatefulAssociationValueType.isStatefulAssociationValue( objectClass ) )
        {
            return serializeStatefulAssociationValue( document, options, object, root );
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
        throw new SerializationException( "Don't know how to serialize " + object );
    }

    private <T> Node serializeStatefulAssociationValue( Document document, Options options, T composite, boolean root )
    {
        CompositeInstance instance = PolygeneAPI.FUNCTION_COMPOSITE_INSTANCE_OF.apply( (Composite) composite );
        StatefulAssociationCompositeDescriptor descriptor =
            (StatefulAssociationCompositeDescriptor) instance.descriptor();
        AssociationStateHolder state = (AssociationStateHolder) instance.state();
        StatefulAssociationValueType<?> valueType = descriptor.valueType();

        Element valueElement = document.createElement( settings.getValueTagName() );
        valueType.properties().forEach(
            property ->
            {
                Object value = state.propertyFor( property.accessor() ).get();
                ConvertedBy convertedBy = property.metaInfo( ConvertedBy.class );
                if( convertedBy != null )
                {
                    value = module.newObject( convertedBy.value() ).toString( value );
                }
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
        if( ( root && options.rootTypeInfo() ) || ( !root && options.nestedTypeInfo() ) )
        {
            valueElement.setAttribute( settings.getTypeInfoTagName(), valueType.primaryType().getName() );
        }
        return valueElement;
    }

    private Node serializeMap( Document document, Options options, Map<?, ?> map )
    {
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
}
