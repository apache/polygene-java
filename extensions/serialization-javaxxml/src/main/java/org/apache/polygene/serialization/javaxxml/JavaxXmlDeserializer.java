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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.polygene.api.association.AssociationDescriptor;
import org.apache.polygene.api.entity.EntityReference;
import org.apache.polygene.api.injection.scope.This;
import org.apache.polygene.api.injection.scope.Uses;
import org.apache.polygene.api.property.PropertyDescriptor;
import org.apache.polygene.api.serialization.SerializationException;
import org.apache.polygene.api.service.ServiceDescriptor;
import org.apache.polygene.api.structure.ModuleDescriptor;
import org.apache.polygene.api.type.ArrayType;
import org.apache.polygene.api.type.CollectionType;
import org.apache.polygene.api.type.EnumType;
import org.apache.polygene.api.type.MapType;
import org.apache.polygene.api.type.ValueCompositeType;
import org.apache.polygene.api.type.ValueType;
import org.apache.polygene.api.value.ValueBuilder;
import org.apache.polygene.api.value.ValueDescriptor;
import org.apache.polygene.spi.serialization.AbstractTextDeserializer;
import org.apache.polygene.spi.serialization.XmlDeserializer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableMap;
import static java.util.Collections.unmodifiableSet;
import static org.apache.polygene.api.util.Collectors.toMapWithNullValues;

public class JavaxXmlDeserializer extends AbstractTextDeserializer implements XmlDeserializer
{
    private static final String NULL_ELEMENT_NAME = "null";

    @This
    private JavaxXmlAdapters adapters;

    @Uses
    private ServiceDescriptor descriptor;

    @Override
    public <T> T fromXml( ModuleDescriptor module, ValueType valueType, Document state )
    {
        Optional<Element> stateElement = JavaxXml.firstChildElementNamed( state, getSettings().getRootTagName() );
        if( stateElement.isPresent() )
        {
            Optional<Node> stateNode = JavaxXml.firstStateChildNode( stateElement.get() );
            return doDeserialize( module, valueType, stateNode.orElse( null ) );
        }
        return null;
    }

    @SuppressWarnings( "unchecked" )
    private <T> T doDeserialize( ModuleDescriptor module, ValueType valueType, Node xml )
    {
        if( xml == null )
        {
            return valueType.hasType( String.class ) ? (T) "" : null;
        }
        if( xml.getNodeType() == Node.ELEMENT_NODE && NULL_ELEMENT_NAME.equals( ( (Element) xml ).getTagName() ) )
        {
            return null;
        }
        JavaxXmlAdapter<?> adapter = adapters.adapterFor( valueType );
        if( adapter != null )
        {
            return (T) adapter.deserialize( xml, ( element, type ) -> doDeserialize( module, type, element ) );
        }
        if( EnumType.class.isAssignableFrom( valueType.getClass() ) )
        {
            return (T) Enum.valueOf( (Class) valueType.primaryType(), xml.getNodeValue() );
        }
        if( ArrayType.class.isAssignableFrom( valueType.getClass() ) )
        {
            return (T) deserializeArray( module, (ArrayType) valueType, xml );
        }
        if( CollectionType.class.isAssignableFrom( valueType.getClass() ) )
        {
            return (T) deserializeCollection( module, (CollectionType) valueType, xml );
        }
        if( MapType.class.isAssignableFrom( valueType.getClass() ) )
        {
            return (T) deserializeMap( module, (MapType) valueType, xml );
        }
        if( ValueCompositeType.class.isAssignableFrom( valueType.getClass() ) )
        {
            return (T) deserializeValueComposite( module, (ValueCompositeType) valueType, xml );
        }
        return (T) doGuessDeserialize( module, valueType, xml );
    }

    private Object deserializeValueComposite( ModuleDescriptor module, ValueCompositeType valueType, Node xml )
    {
        Optional<String> typeInfo = getTypeInfo( xml );
        if( typeInfo.isPresent() )
        {
            ValueDescriptor descriptor = module.valueDescriptor( typeInfo.get() );
            if( descriptor == null )
            {
                String typeInfoName = getSettings().getTypeInfoTagName();
                throw new SerializationException(
                    typeInfoName + ": " + typeInfo.get() + " could not be resolved while deserializing " + xml );
            }
            valueType = descriptor.valueType();
        }
        ValueBuilder builder = module.instance().newValueBuilderWithState(
            valueType.primaryType(),
            propertyFunction( module, xml ),
            associationFunction( module, xml ),
            manyAssociationFunction( module, xml ),
            namedAssociationFunction( module, xml ) );
        return builder.newInstance();
    }

    private Function<PropertyDescriptor, Object> propertyFunction( ModuleDescriptor module, Node xml )
    {
        return property ->
        {
            Optional<Element> element = JavaxXml.firstChildElementNamed( xml, property.qualifiedName().name() );
            if( element.isPresent() )
            {
                Node valueNode = JavaxXml.firstStateChildNode( element.get() ).orElse( null );
                Object value = doDeserialize( module, property.valueType(), valueNode );
                if( property.isImmutable() )
                {
                    if( value instanceof Set )
                    {
                        return unmodifiableSet( (Set<?>) value );
                    }
                    else if( value instanceof List )
                    {
                        return unmodifiableList( (List<?>) value );
                    }
                    else if( value instanceof Map )
                    {
                        return unmodifiableMap( (Map<?, ?>) value );
                    }
                }
                return value;
            }
            return property.resolveInitialValue( module );
        };
    }

    private Function<AssociationDescriptor, EntityReference> associationFunction( ModuleDescriptor module, Node xml )
    {
        return association ->
            (EntityReference) JavaxXml.firstChildElementNamed( xml, association.qualifiedName().name() )
                                      .map( element -> doDeserialize( module,
                                                                      ValueType.ENTITY_REFERENCE,
                                                                      JavaxXml.firstStateChildNode( element )
                                                                              .orElse( null ) ) )
                                      .orElse( null );
    }

    private Function<AssociationDescriptor, Stream<EntityReference>> manyAssociationFunction( ModuleDescriptor module,
                                                                                              Node xml )
    {
        return association ->
            JavaxXml.firstChildElementNamed( xml, association.qualifiedName().name() )
                    .map( element -> (List) doDeserialize( module,
                                                           ENTITY_REF_LIST_VALUE_TYPE,
                                                           JavaxXml.firstStateChildNode( element )
                                                                   .orElse( null ) ) )
                    .map( List::stream )
                    .orElse( Stream.empty() );
    }

    private Function<AssociationDescriptor, Stream<Map.Entry<String, EntityReference>>> namedAssociationFunction(
        ModuleDescriptor module, Node xml )
    {
        return association ->
            (Stream) JavaxXml.firstChildElementNamed( xml, association.qualifiedName().name() )
                             .map( element -> (Map) doDeserialize( module,
                                                                   ENTITY_REF_MAP_VALUE_TYPE,
                                                                   JavaxXml.firstStateChildNode( element )
                                                                           .orElse( null ) ) )
                             .map( Map::entrySet ).map( Set::stream )
                             .orElse( Stream.empty() );
    }


    private Object deserializeArray( ModuleDescriptor module, ArrayType arrayType, Node xml )
    {
        if( arrayType.isArrayOfPrimitiveBytes() )
        {
            return Base64.getDecoder().decode( xml.getNodeValue().getBytes( UTF_8 ) );
        }
        CollectionType collectionType = CollectionType.listOf( arrayType.collectedType() );
        List collection = (List) deserializeCollection( module, collectionType, xml );
        Object array = Array.newInstance( arrayType.collectedType().primaryType(), collection.size() );
        for( int idx = 0; idx < collection.size(); idx++ )
        {
            Array.set( array, idx, collection.get( idx ) );
        }
        return array;
    }

    @SuppressWarnings( "unchecked" )
    private Collection deserializeCollection( ModuleDescriptor module, CollectionType collectionType, Node xml )
    {
        Supplier<Collection> collectionSupplier = () -> collectionType.isSet()
                                                        ? new LinkedHashSet<>()
                                                        : new ArrayList<>();
        if( !xml.hasChildNodes() )
        {
            return collectionSupplier.get();
        }
        return JavaxXml
            .childElements( xml )
            .map( element ->
                  {
                      if( getSettings().getCollectionElementTagName().equals( element.getTagName() ) )
                      {
                          return doDeserialize( module, collectionType.collectedType(),
                                                JavaxXml.firstStateChildNode( element ).get() );
                      }
                      return doDeserialize( module, collectionType.collectedType(), element );
                  } )
            .collect( Collectors.toCollection( collectionSupplier ) );
    }

    @SuppressWarnings( "unchecked" )
    private Map deserializeMap( ModuleDescriptor module, MapType mapType, Node xml )
    {
        if( !xml.hasChildNodes() )
        {
            return new LinkedHashMap<>();
        }
        Predicate<Element> complexMapping = element -> getSettings().getMapEntryTagName().equals( element.getTagName() )
                                                       && JavaxXml.firstChildElementNamed( element, "key" )
                                                                  .isPresent();
        // This allows deserializing mixed simple/complex mappings for a given map
        return JavaxXml.childElements( xml ).map(
            element ->
            {
                if( complexMapping.test( element ) )
                {
                    Node keyNode = JavaxXml.firstChildElementNamed( element, "key" )
                                           .flatMap( JavaxXml::firstStateChildNode )
                                           .get();
                    Optional<Node> valueNode = JavaxXml.firstChildElementNamed( element, "value" )
                                                       .flatMap( JavaxXml::firstStateChildNode );
                    Object key = doDeserialize( module, mapType.keyType(), keyNode );
                    Object value = valueNode.map( node -> doDeserialize( module, mapType.valueType(), node ) )
                                            .orElse( null );
                    return new HashMap.SimpleImmutableEntry<>( key, value );
                }
                String key = element.getTagName();
                Object value = JavaxXml.firstStateChildNode( element )
                                       .map( node -> doDeserialize( module, mapType.valueType(), node ) )
                                       .orElse( null );
                return (Map.Entry) new HashMap.SimpleImmutableEntry<>( key, value );
            }
        ).collect( toMapWithNullValues( LinkedHashMap::new ) );
    }

    private Object doGuessDeserialize( ModuleDescriptor module, ValueType valueType, Node xml )
    {
        // TODO Could do better by detecting <collection/>, <map/> and <value/>
        Optional<String> typeInfo = getTypeInfo( xml );
        if( typeInfo.isPresent() )
        {
            ValueDescriptor valueDescriptor = module.valueDescriptor( typeInfo.get() );
            if( valueDescriptor != null )
            {
                return deserializeValueComposite( module, valueDescriptor.valueType(), xml );
            }
        }
        if( xml.getNodeType() == Node.CDATA_SECTION_NODE )
        {
            return deserializeBase64( xml.getNodeValue() );
        }
        throw new SerializationException( "Don't know how to deserialize " + valueType + " from " + xml );
    }

    private Object deserializeBase64( String inputString )
    {
        byte[] bytes = inputString.getBytes( UTF_8 );
        bytes = Base64.getDecoder().decode( bytes );
        try( ObjectInputStream oin = new ObjectInputStream( new ByteArrayInputStream( bytes ) ) )
        {
            return oin.readObject();
        }
        catch( IOException | ClassNotFoundException ex )
        {
            throw new SerializationException( "Unable to deserialize Base64 serialized " + inputString, ex );
        }
    }

    private Optional<String> getTypeInfo( Node xml )
    {
        if( xml.getNodeType() != Node.ELEMENT_NODE )
        {
            return Optional.empty();
        }
        String typeInfo = ( (Element) xml ).getAttribute( getSettings().getTypeInfoTagName() );
        if( typeInfo.isEmpty() )
        {
            return Optional.empty();
        }
        return Optional.of( typeInfo );
    }

    private JavaxXmlSettings getSettings()
    {
        return JavaxXmlSettings.orDefault( descriptor.metaInfo( JavaxXmlSettings.class ) );
    }
}
