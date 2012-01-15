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

package org.qi4j.runtime.types;

import org.qi4j.api.common.InvalidApplicationException;
import org.qi4j.api.common.MetaInfo;
import org.qi4j.api.common.Visibility;
import org.qi4j.api.type.*;
import org.qi4j.api.util.Classes;
import org.qi4j.api.value.ValueComposite;
import org.qi4j.functional.HierarchicalVisitorAdapter;
import org.qi4j.functional.Iterables;
import org.qi4j.runtime.association.AssociationsModel;
import org.qi4j.runtime.association.ManyAssociationsModel;
import org.qi4j.runtime.composite.CompositeMethodsModel;
import org.qi4j.runtime.composite.MixinsModel;
import org.qi4j.runtime.property.PropertiesModel;
import org.qi4j.runtime.structure.LayerModel;
import org.qi4j.runtime.structure.ModuleModel;
import org.qi4j.runtime.structure.UsedLayersModel;
import org.qi4j.runtime.value.ValueModel;
import org.qi4j.runtime.value.ValueStateModel;
import org.qi4j.runtime.value.ValuesModel;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;

public class ValueTypeFactory
{
    private static final ValueTypeFactory instance = new ValueTypeFactory();

    public static ValueTypeFactory instance()
    {
        return instance;
    }

    public ValueType newValueType( Type type, Class declaringClass, Class compositeType, LayerModel layer, ModuleModel module )
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
                ValueType collectedType = newValueType( collectionType, declaringClass, compositeType, layer, module );
                valueType = new CollectionType( Classes.RAW_CLASS.map(type) , collectedType );
            }
            else
            {
                valueType = new CollectionType( Classes.RAW_CLASS.map(type) , newValueType( Object.class, declaringClass, compositeType, layer, module ) );
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
                ValueType keyedType = newValueType( keyType, declaringClass, compositeType, layer, module );

                Type valType = pt.getActualTypeArguments()[ 1 ];
                if( valType instanceof TypeVariable )
                {
                    TypeVariable valueTypeVariable = (TypeVariable) valType;
                    valType = Classes.resolveTypeVariable( valueTypeVariable, declaringClass, compositeType );
                }
                ValueType valuedType = newValueType( valType, declaringClass, compositeType, layer, module );

                valueType = new MapType( Classes.RAW_CLASS.map(type) , keyedType, valuedType );
            }
            else
            {
                valueType = new MapType( Classes.RAW_CLASS.map(type) , newValueType( Object.class, declaringClass, compositeType, layer, module ), newValueType( Object.class, declaringClass, compositeType, layer, module ) );
            }
        }
        else if( ValueCompositeType.isValueComposite( type ) )
        {
            // Find ValueModel in module/layer/used layers
            ValueModel model = new ValueFinder(layer, module, Classes.RAW_CLASS.map( type )).getFoundModel();

            if (model == null)
            {
                if (type.equals( ValueComposite.class ))
                {
                    // Create default model
                    MixinsModel mixinsModel = new MixinsModel();
                    model = new ValueModel( ValueComposite.class, (Iterable) Iterables.iterable(ValueComposite.class), Visibility.application, new MetaInfo( ), mixinsModel, new ValueStateModel( new PropertiesModel(), new AssociationsModel(), new ManyAssociationsModel() ), new CompositeMethodsModel( mixinsModel ) );
                } else
                    throw new InvalidApplicationException("["+module.name()+"] Could not find ValueComposite of type "+type);
            }

            return model.valueType();
        }
        else if( EnumType.isEnum( type ) )
        {
            valueType = new EnumType( Classes.RAW_CLASS.map(type)  );
        }else
        {
            valueType = new ValueType(Classes.RAW_CLASS.map(type) );
        }

        return valueType;
    }

    private class ValueFinder
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

            if (foundModel == null)
            {
                visibility = Visibility.layer;
                layer.accept( this );

                if (foundModel == null)
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
        public boolean visitEnter( Object visited ) throws RuntimeException
        {
            if (visited instanceof ValuesModel )
                return true;
            else if (visited instanceof ModuleModel )
                return true;
            else if (visited instanceof UsedLayersModel )
                return true;
            else if (visited instanceof ValueModel )
            {
                ValueModel valueModel = (ValueModel) visited;
                if (valueModel.type().equals( type ) && valueModel.visibility().ordinal() >= visibility.ordinal())
                {
                    foundModel = valueModel;
                }
            }

            return false;
        }

        @Override
        public boolean visitLeave( Object visited ) throws RuntimeException
        {
            return foundModel == null;
        }
    }
}
