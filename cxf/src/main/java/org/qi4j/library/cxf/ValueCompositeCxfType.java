/*
 * Copyright 2010 Niclas Hedhman.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.qi4j.library.cxf;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Map;
import javax.xml.namespace.QName;
import org.apache.cxf.aegis.Context;
import org.apache.cxf.aegis.DatabindingException;
import org.apache.cxf.aegis.type.AegisType;
import org.apache.cxf.aegis.type.TypeMapping;
import org.apache.cxf.aegis.type.collection.CollectionType;
import org.apache.cxf.aegis.type.collection.MapType;
import org.apache.cxf.aegis.xml.MessageReader;
import org.apache.cxf.aegis.xml.MessageWriter;
import org.apache.cxf.common.xmlschema.XmlSchemaUtils;
import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaAttribute;
import org.apache.ws.commons.schema.XmlSchemaComplexType;
import org.apache.ws.commons.schema.XmlSchemaElement;
import org.apache.ws.commons.schema.XmlSchemaSequence;
import org.qi4j.api.common.Optional;
import org.qi4j.api.common.QualifiedName;
import org.qi4j.api.common.TypeName;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectBuilder;
import org.qi4j.api.object.ObjectBuilderFactory;
import org.qi4j.api.property.Property;
import org.qi4j.api.property.StateHolder;
import org.qi4j.api.util.Classes;
import org.qi4j.api.value.NoSuchValueException;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import org.qi4j.api.value.ValueComposite;
import org.qi4j.spi.composite.StateDescriptor;
import org.qi4j.spi.property.PropertyDescriptor;
import org.qi4j.spi.structure.ModuleSPI;
import org.qi4j.spi.value.ValueDescriptor;

public class ValueCompositeCxfType extends AegisType
{
    @Structure
    private ValueBuilderFactory vbf;

    @Structure
    private ObjectBuilderFactory obf;

    @Structure
    private ModuleSPI module;

    public ValueCompositeCxfType( @Uses Type type, @Uses TypeMapping typeMapping )
    {
        setTypeMapping( typeMapping );
        setTypeClass( type );
        setSchemaType( NamespaceUtil.convertJavaTypeToQName( type ) );
    }

    @Override
    public Object readObject( final MessageReader reader, final Context context )
        throws DatabindingException
    {
        QName qname = getSchemaType();
        final String className = ( qname.getNamespaceURI() + "." + qname.getLocalPart() ).substring( 20 );
        ValueBuilder<?> builder = vbf.newValueBuilder( (Class<?>) typeClass );
        // Read attributes
        builder.withState( new StateHolder()
        {
            public <T> Property<T> getProperty( final Method propertyMethod )
            {
                // ignore, not used
                return null;
            }

            public <T> Property<T> getProperty( final QualifiedName name )
            {
                // ignore, not used
                return null;
            }

            public <ThrowableType extends Throwable> void visitProperties( final StateVisitor<ThrowableType> visitor )
                throws ThrowableType
            {
                ValueDescriptor descriptor = module.valueDescriptor( className );
                StateDescriptor stateDescriptor = descriptor.state();
                while( reader.hasMoreElementReaders() )
                {
                    MessageReader childReader = reader.getNextElementReader();
                    QName childName = childReader.getName();
                    QualifiedName childQualifiedName = QualifiedName.fromClass( (Class) typeClass,
                                                                                childName.getLocalPart() );
                    PropertyDescriptor propertyDescriptor = stateDescriptor.getPropertyByQualifiedName(
                        childQualifiedName );
                    Type propertyType = propertyDescriptor.type();
                    AegisType type = getTypeMapping().getType( propertyType );
                    Object value = type.readObject( childReader, context );
                    visitor.visitProperty( childQualifiedName, value );
                }
            }
        } );
        return builder.newInstance();
    }

    @Override
    public void writeObject( Object object, final MessageWriter writer, final Context context )
        throws DatabindingException
    {
        ValueComposite composite = (ValueComposite) object;
        writer.writeXsiType( NamespaceUtil.convertJavaTypeToQName( composite.type() ) );
        StateHolder state = composite.state();
        state.visitProperties( new StateHolder.StateVisitor<RuntimeException>()
        {
            public void visitProperty( QualifiedName name, Object value )
            {
                AegisType type = null;
                if( value instanceof ValueComposite )
                {
                    ValueComposite composite = (ValueComposite) value;
                    type = getTypeMapping().getType( NamespaceUtil.convertJavaTypeToQName( composite.type() ) );
                }
                else
                {
                    if( value != null )
                    {
                        type = getOrCreateNonQi4jType( value );
                    }
                }
                QName childName = new QName( "", name.name() );
                MessageWriter cwriter = writer.getElementWriter( childName );
                if( type != null )
                {
                    type.writeObject( value, cwriter, context );
                }
                else
                {
//                    cwriter.writeXsiNil();
                }
                cwriter.close();
            }
        } );
    }

    private AegisType getOrCreateNonQi4jType( Object value )
    {
        AegisType type;TypeMapping mapping = getTypeMapping();
        Class<?> javaType = value.getClass();
        type = mapping.getType( javaType );
        if( type == null )
        {
            // This might be wrong and instead the ultimate top parent should be used. This works, since
            // we know that we are the top parent.
            type = getTypeMapping().getTypeCreator().createType( javaType );
            mapping.register( type );
        }
        return type;
    }

    @Override
    public void writeSchema( XmlSchema root )
    {
        XmlSchemaComplexType complex = new XmlSchemaComplexType( root );
        complex.setName( getSchemaType().getLocalPart() );
        root.addType( complex );
        root.getItems().add( complex );

        XmlSchemaSequence sequence = new XmlSchemaSequence(); // No clue why this?
        complex.setParticle( sequence );  // No idea what this is for

        ValueDescriptor descriptor = module.valueDescriptor( getTypeClass().getName() );

        for( PropertyDescriptor p : descriptor.state().properties() )
        {
            if( isValueComposite( p.type() ) )
            {
                XmlSchemaElement element = new XmlSchemaElement();
                element.setName( p.qualifiedName().name() );
                element.setNillable( p.metaInfo( Optional.class ) != null ); // see below
                sequence.getItems().add( element );
                AegisType nested = getOrCreateAegisType( p.type(), root );
                element.setRefName( nested.getSchemaType() );
            }
            else if( isCollectionOrMap( p ) )
            {
                XmlSchemaElement element = new XmlSchemaElement();
                element.setName( p.qualifiedName().name() );
                element.setNillable( p.metaInfo( Optional.class ) != null ); // see below
                sequence.getItems().add( element );
                AegisType nested = getOrCreateAegisType( p.type(), root );
                element.setRefName( nested.getSchemaType() );
            }
            else
            {
                XmlSchemaAttribute attribute = new XmlSchemaAttribute();
                complex.getAttributes().add( attribute );
                attribute.setName( p.qualifiedName().name() );
                AegisType nested = getTypeMapping().getType( p.type() );
                attribute.setSchemaTypeName( nested.getSchemaType() );
            }
            QName name = NamespaceUtil.convertJavaTypeToQName( p.type() );
            String ns = name.getNamespaceURI();
            if( !ns.equals( root.getTargetNamespace() ) )
            {
                XmlSchemaUtils.addImportIfNeeded( root, ns );
            }
        }
    }

    private AegisType getOrCreateAegisType( Type type, XmlSchema root )
    {
        AegisType nested = getTypeMapping().getType( type );
        if( nested == null )
        {
            nested = createType( type, root );
            nested.writeSchema( root );
        }
        return nested;
    }

    private AegisType createType( Type type, XmlSchema root )
    {
        if( isCollection( type ) )
        {
            AegisType componentType = getOrCreateAegisType( getCollectionComponentType( type ), root );
            CollectionType resultType = new CollectionType( componentType );
            TypeName name = TypeName.nameOf( type );
            QName schemaType = new QName( "http://www.w3.org/2001/XMLSchema", "list" );
            resultType.setSchemaType( schemaType );
            return resultType;
        }
        else if( isMap( type ) )
        {
            AegisType keyType = getOrCreateAegisType( getMapKeyComponentType( type ), root );
            AegisType valueType = getOrCreateAegisType( getMapValueComponentType( type ), root );
            TypeName name = TypeName.nameOf( type );
            QName schemaType = new QName( name.toURI(), "map" );
            return new MapType( schemaType, keyType, valueType );
        }
        else if( isValueComposite( type ) )
        {
            ObjectBuilder<ValueCompositeCxfType> builder = obf.newObjectBuilder( ValueCompositeCxfType.class );
            builder.use( getTypeMapping() );
            builder.use( type );
            ValueCompositeCxfType aegisType = builder.newInstance();
            getTypeMapping().register( aegisType );
            return aegisType;
        }
        else
        {
            throw new NoSuchValueException( type.toString(), module.name() );
        }
    }

    private boolean isCollectionOrMap( final PropertyDescriptor p )
    {
        Type type = p.type();
        return isCollectionOrMap( type );
    }

    private boolean isCollection( Type type )
    {
        if( isCollectionClass( type ) )
        {
            return true;
        }
        if( type instanceof ParameterizedType )
        {
            ParameterizedType param = (ParameterizedType) type;
            Type rawType = param.getRawType();
            if( isCollectionClass( rawType ) )
            {
                return true;
            }
        }
        return false;
    }

    private boolean isMap( Type type )
    {
        if( isMapClass( type ) )
        {
            return true;
        }
        if( type instanceof ParameterizedType )
        {
            ParameterizedType param = (ParameterizedType) type;
            Type rawType = param.getRawType();
            if( isCollectionClass( rawType ) )
            {
                return true;
            }
        }
        return false;
    }

    private boolean isCollectionOrMap( Type type )
    {
        return isMap( type ) || isCollection( type );
    }

    private boolean isCollectionClass( Type type )
    {
        if( type instanceof Class )
        {
            Class clazz = (Class) type;
            return Collection.class.isAssignableFrom( clazz );
        }
        return false;
    }

    private boolean isMapClass( Type type )
    {
        if( type instanceof Class )
        {
            Class clazz = (Class) type;
            return Map.class.isAssignableFrom( clazz );
        }
        return false;
    }

    private Type getCollectionComponentType( Type p )
    {
        return getActualTypeArgument( p, 0 );
    }

    private Type getMapKeyComponentType( Type p )
    {
        return getActualTypeArgument( p, 0 );
    }

    private Type getMapValueComponentType( Type p )
    {
        return getActualTypeArgument( p, 1 );
    }

    private Type getActualTypeArgument( Type p, int index )
    {
        if( p instanceof ParameterizedType )
        {
            ParameterizedType type = (ParameterizedType) p;
            return type.getActualTypeArguments()[ index ];
        }
        return null;
    }

    private boolean isValueComposite( Type type )
    {
        Class clazz = Classes.getRawClass( type );
        ValueDescriptor descriptor = module.valueDescriptor( clazz.getName() );
        return descriptor != null;
    }

    @Override
    public boolean isComplex()
    {
        return true;
    }
}
