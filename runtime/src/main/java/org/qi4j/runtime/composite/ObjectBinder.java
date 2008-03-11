/*
 * Copyright (c) 2007, Rickard Öberg. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.qi4j.runtime.composite;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.qi4j.spi.composite.AssociationModel;
import org.qi4j.spi.composite.BindingException;
import org.qi4j.spi.composite.ConstructorBinding;
import org.qi4j.spi.composite.ConstructorResolution;
import org.qi4j.spi.composite.FieldBinding;
import org.qi4j.spi.composite.MethodBinding;
import org.qi4j.spi.composite.ObjectBinding;
import org.qi4j.spi.composite.ObjectResolution;
import org.qi4j.spi.injection.BindingContext;
import org.qi4j.spi.injection.InjectionProviderFactory;
import org.qi4j.spi.injection.InvalidInjectionException;
import org.qi4j.spi.property.AssociationBinding;
import org.qi4j.spi.property.PropertyBinding;
import org.qi4j.spi.property.PropertyModel;
import org.qi4j.spi.structure.AssociationDescriptor;
import org.qi4j.spi.structure.PropertyDescriptor;

/**
 * TODO
 */
public final class ObjectBinder extends CompositeBinder
{
    public ObjectBinder( InjectionProviderFactory injectionProviderFactory )
    {
        super( injectionProviderFactory );
    }

    public ObjectBinding bindObject( BindingContext bindingContext )
    {
        try
        {
            ObjectResolution objectResolution = (ObjectResolution) bindingContext.getAbstractResolution();
            ConstructorResolution constructorResolution = objectResolution.getConstructorResolutions().iterator().next(); // TODO Pick the best one
            ConstructorBinding constructorBinding = bindConstructor( bindingContext, constructorResolution );
            Iterable<FieldBinding> fieldBindings = bindFields( bindingContext, objectResolution.getFieldResolutions() );
            Iterable<MethodBinding> methodBindings = bindMethods( bindingContext, objectResolution.getMethodResolutions() );

            List<PropertyBinding> propertyBindings = new ArrayList<PropertyBinding>();
            Iterable<PropertyModel> propertyModels = objectResolution.getObjectModel().getPropertyModels();
            for( PropertyModel propertyModel : propertyModels )
            {
                PropertyDescriptor propertyDescriptor = bindingContext.getModuleResolution().getModuleModel().getPropertyDescriptor( propertyModel.getAccessor() );
                Map<Class, Serializable> propertyInfos;
                if( propertyDescriptor != null )
                {
                    propertyInfos = propertyDescriptor.getPropertyInfos();
                }
                else
                {
                    propertyInfos = Collections.emptyMap();
                }

                PropertyBinding propertyBinding = new PropertyBinding( objectResolution.getPropertyResolution( propertyModel.getName() ), propertyInfos, propertyDescriptor != null ? propertyDescriptor.getDefaultValue() : null );
                propertyBindings.add( propertyBinding );
            }

            List<AssociationBinding> associationBindings = new ArrayList<AssociationBinding>();
            Iterable<AssociationModel> associationModels = objectResolution.getObjectModel().getAssociationModels();
            for( AssociationModel associationModel : associationModels )
            {
                AssociationDescriptor associationDescriptor = bindingContext.getModuleResolution().getModuleModel().getAssociationDescriptor( associationModel.getAccessor() );
                Map<Class, Serializable> associationInfos;
                if( associationDescriptor != null )
                {
                    associationInfos = associationDescriptor.getAssociationInfos();
                }
                else
                {
                    associationInfos = Collections.emptyMap();
                }

                AssociationBinding associationBinding = new AssociationBinding( objectResolution.getAssociationResolution( associationModel.getQualifiedName() ), associationInfos );
                associationBindings.add( associationBinding );
            }


            ObjectBinding objectBinding = new ObjectBinding( objectResolution, constructorBinding, fieldBindings, methodBindings, propertyBindings, associationBindings );
            return objectBinding;
        }
        catch( InvalidInjectionException e )
        {
            throw new BindingException( "Could not bind injections", e );
        }
    }
}
