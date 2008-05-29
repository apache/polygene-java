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

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import static org.qi4j.util.AnnotationUtil.getInjectionAnnotation;
import static org.qi4j.util.ClassUtil.fieldsOf;

/**
 * TODO
 */
public final class InjectedFieldsModel
{
    private List<InjectedFieldModel> fields = new ArrayList<InjectedFieldModel>();

    public InjectedFieldsModel( Class fragmentClass )
    {
        List<Field> fields = fieldsOf( fragmentClass );
        for( Field field : fields )
        {
            Annotation injectionAnnotation = getInjectionAnnotation( field.getAnnotations() );
            if( injectionAnnotation != null )
            {
                DependencyModel dependencyModel = new DependencyModel( injectionAnnotation, field.getGenericType(), fragmentClass, false );
                InjectedFieldModel injectedFieldModel = new InjectedFieldModel( field, dependencyModel );
                this.fields.add( injectedFieldModel );
            }
        }
    }

    public void visitDependencies( DependencyVisitor visitor )
    {
        for( InjectedFieldModel field : fields )
        {
            field.visitDependency( visitor );
        }
    }

    public void bind( BindingContext context )
    {
        for( InjectedFieldModel field : fields )
        {
            field.bind( context );
        }
    }

    public void inject( InjectionContext context, Object instance )
    {
        for( InjectedFieldModel field : fields )
        {
            field.inject( context, instance );
        }
    }
}