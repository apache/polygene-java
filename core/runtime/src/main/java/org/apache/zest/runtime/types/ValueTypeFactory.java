/*
 * Copyright 2009 Niclas Hedhman.
 *
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.zest.runtime.types;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import org.apache.zest.api.common.InvalidApplicationException;
import org.apache.zest.api.common.MetaInfo;
import org.apache.zest.api.common.Visibility;
import org.apache.zest.api.type.CollectionType;
import org.apache.zest.api.type.EnumType;
import org.apache.zest.api.type.MapType;
import org.apache.zest.api.type.Serialization;
import org.apache.zest.api.type.ValueCompositeType;
import org.apache.zest.api.type.ValueType;
import org.apache.zest.api.util.Classes;
import org.apache.zest.api.value.ValueComposite;
import org.apache.zest.functional.HierarchicalVisitorAdapter;
import org.apache.zest.functional.Iterables;
import org.apache.zest.functional.Specifications;
import org.apache.zest.runtime.association.AssociationsModel;
import org.apache.zest.runtime.association.ManyAssociationsModel;
import org.apache.zest.runtime.association.NamedAssociationsModel;
import org.apache.zest.runtime.composite.CompositeMethodsModel;
import org.apache.zest.runtime.composite.MixinsModel;
import org.apache.zest.runtime.property.PropertiesModel;
import org.apache.zest.runtime.structure.LayerModel;
import org.apache.zest.runtime.structure.ModuleModel;
import org.apache.zest.runtime.structure.UsedLayersModel;
import org.apache.zest.runtime.value.ValueModel;
import org.apache.zest.runtime.value.ValueStateModel;
import org.apache.zest.runtime.value.ValuesModel;

public class ValueTypeFactory
{
    private static final ValueTypeFactory instance = new ValueTypeFactory();

    public static ValueTypeFactory instance()
    {
        return instance;
    }

    @SuppressWarnings( {"raw", "unchecked"} )
    public ValueType newValueType( Type type,
                                   Class declaringClass,
                                   Class compositeType,
                                   LayerModel layer,
                                   ModuleModel module,
                                   Serialization.Variant variant
    )
    {
        ValueType valueType = null;
        if( CollectionType.isCollection( type ) )
        {
            if( type instanceof ParameterizedType )
            {
                ParameterizedType pt = (ParameterizedType) type;
                Type collectionType = pt.getActualTypeArguments()[ 0 ];
                if( collectionType instanceof TypeVariable )
                {
                    TypeVariable collectionTypeVariable = (TypeVariable) collectionType;
                    collectionType = Classes.resolveTypeVariable( collectionTypeVariable, declaringClass, compositeType );
                }
                ValueType collectedType = newValueType( collectionType, declaringClass, compositeType, layer, module, variant );
                valueType = new CollectionType( Classes.RAW_CLASS.apply( type ), collectedType );
            }
            else
            {
                ValueType collectedType = newValueType( Object.class, declaringClass, compositeType, layer, module, variant );
                valueType = new CollectionType( Classes.RAW_CLASS.apply( type ), collectedType );
            }
        }
        else if( MapType.isMap( type ) )
        {
            if( type instanceof ParameterizedType )
            {
                ParameterizedType pt = (ParameterizedType) type;
                Type keyType = pt.getActualTypeArguments()[ 0 ];
                if( keyType instanceof TypeVariable )
                {
                    TypeVariable keyTypeVariable = (TypeVariable) keyType;
                    keyType = Classes.resolveTypeVariable( keyTypeVariable, declaringClass, compositeType );
                }
                ValueType keyedType = newValueType( keyType, declaringClass, compositeType, layer, module, variant );
                Type valType = pt.getActualTypeArguments()[ 1 ];
                if( valType instanceof TypeVariable )
                {
                    TypeVariable valueTypeVariable = (TypeVariable) valType;
                    valType = Classes.resolveTypeVariable( valueTypeVariable, declaringClass, compositeType );
                }
                ValueType valuedType = newValueType( valType, declaringClass, compositeType, layer, module, variant );
                valueType = new MapType( Classes.RAW_CLASS.apply( type ), keyedType, valuedType, variant );
            }
            else
            {
                ValueType keyType = newValueType( Object.class, declaringClass, compositeType, layer, module, variant );
                ValueType valuesType = newValueType( Object.class, declaringClass, compositeType, layer, module, variant );
                valueType = new MapType( Classes.RAW_CLASS.apply( type ), keyType, valuesType, variant );
            }
        }
        else if( ValueCompositeType.isValueComposite( type ) )
        {
            // Find ValueModel in module/layer/used layers
            ValueModel model = new ValueFinder( layer, module, Classes.RAW_CLASS.apply( type ) ).getFoundModel();

            if( model == null )
            {
                if( type.equals( ValueComposite.class ) )
                {
                    // Create default model
                    MixinsModel mixinsModel = new MixinsModel();
                    Iterable valueComposite = (Iterable) Iterables.iterable( ValueComposite.class );
                    ValueStateModel valueStateModel = new ValueStateModel( new PropertiesModel(),
                                                                           new AssociationsModel(),
                                                                           new ManyAssociationsModel(),
                                                                           new NamedAssociationsModel() );
                    model = new ValueModel( valueComposite, Visibility.application, new MetaInfo(),
                                            mixinsModel, valueStateModel, new CompositeMethodsModel( mixinsModel ) );
                }
                else
                {
                    throw new InvalidApplicationException( "[" + module.name() + "] Could not find ValueComposite of type " + type );
                }
            }

            return model.valueType();
        }
        else if( EnumType.isEnum( type ) )
        {
            valueType = new EnumType( Classes.RAW_CLASS.apply( type ) );
        }
        else
        {
            valueType = new ValueType( Classes.RAW_CLASS.apply( type ) );
        }

        return valueType;
    }

    @SuppressWarnings( "raw" )
    private static class ValueFinder
        extends HierarchicalVisitorAdapter<Object, Object, RuntimeException>
    {
        private Class type;
        private ValueModel foundModel;
        private Visibility visibility;

        private ValueFinder( LayerModel layer, ModuleModel module, Class type )
        {
            this.type = type;

            visibility = Visibility.module;
            module.accept( this );

            if( foundModel == null )
            {
                visibility = Visibility.layer;
                layer.accept( this );

                if( foundModel == null )
                {
                    visibility = Visibility.application;
                    layer.usedLayers().accept( this );
                }
            }
        }

        public ValueModel getFoundModel()
        {
            return foundModel;
        }

        @Override
        public boolean visitEnter( Object visited )
            throws RuntimeException
        {
            if( visited instanceof ValuesModel )
            {
                return true;
            }
            else if( visited instanceof ModuleModel )
            {
                return true;
            }
            else if (visited instanceof LayerModel )
            {
                return true;
            }
            else if (visited instanceof UsedLayersModel )
            {
                return true;
            }
            else if( visited instanceof ValueModel )
            {
                ValueModel valueModel = (ValueModel) visited;
                boolean typeEquality = Specifications.in( valueModel.types() ).satisfiedBy( type );
                if( typeEquality && valueModel.visibility().ordinal() >= visibility.ordinal() )
                {
                    foundModel = valueModel;
                }
            }

            return false;
        }

        @Override
        public boolean visitLeave( Object visited )
            throws RuntimeException
        {
            return foundModel == null;
        }
    }
}
