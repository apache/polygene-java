/*
 * Copyright (c) 2008, Rickard …berg. All Rights Reserved.
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

package org.qi4j.runtime.composite.qi;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.qi4j.runtime.structure.qi.ModuleInstance;

/**
 * TODO
 */
public final class CompositeMethodsModel
{
    private Map<Method, CompositeMethodModel> methods = new HashMap<Method, CompositeMethodModel>();
    private CompositeModel composite;

    public CompositeMethodsModel( CompositeModel composite )
    {
        this.composite = composite;
        implementMixinType( composite.type(), composite.mixins() );
    }

    // Binding
    public void bind( BindingContext bindingContext )
    {
        for( CompositeMethodModel compositeMethodComposite : methods.values() )
        {
            compositeMethodComposite.bind( bindingContext );
        }
    }

    // Context
    public Object invoke( Object[] mixins, Object proxy, Method method, Object[] args, ModuleInstance moduleInstance )
        throws Throwable
    {
        CompositeMethodModel compositeMethod = methods.get( method );

        if( compositeMethod != null )
        {
            return compositeMethod.invoke( proxy, args, mixins, moduleInstance );
        }
        else
        {
            return null; // TODO handle Object methods
        }
    }

    public void implementMixinType( Class mixinType, MixinsModel mixinsModel )
    {
        for( Method method : mixinType.getMethods() )
        {
            if( methods.get( method ) == null )
            {
                List<ParameterModel> parameterModels = new ArrayList<ParameterModel>();
                for( Type type : method.getGenericParameterTypes() )
                {
                    ParameterModel parameterModel = new ParameterModel( method.getAnnotations(), type );
                    parameterModels.add( parameterModel );
                }
                ParametersModel parameters = new ParametersModel( parameterModels );
                CompositeMethodModel methodComposite = new CompositeMethodModel( method, parameters, composite );

                methods.put( method, methodComposite );
                mixinsModel.implementMethod( method );
            }
        }
    }
}
