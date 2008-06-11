/*
 * Copyright (c) 2008, Rickard Öberg. All Rights Reserved.
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
import org.qi4j.runtime.composite.BindingException;
import org.qi4j.runtime.structure.qi.Binder;
import static org.qi4j.util.AnnotationUtil.getInjectionAnnotation;
import static org.qi4j.util.ClassUtil.fieldsOf;

/**
 * TODO
 */
public final class InjectedFieldsModel
    implements Binder
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

    public void bind( Resolution context ) throws BindingException
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

    /**
     * TODO
     */
    private static final class InjectedFieldModel
    {
        private DependencyModel dependencyModel;
        private Field injectedField;
        private Resolution resolution;

        public InjectedFieldModel( Field injectedField, DependencyModel dependencyModel )
        {
            injectedField.setAccessible( true );
            this.injectedField = injectedField;
            this.dependencyModel = dependencyModel;
        }

        public void bind( Resolution resolution ) throws BindingException
        {
            this.resolution = new Resolution( resolution.application(), resolution.layer(), resolution.module(), resolution.composite(), resolution.method(), injectedField );
            dependencyModel.bind( resolution );
        }

        public void inject( InjectionContext context, Object instance )
        {
            Object value = dependencyModel.inject( context );
            try
            {
                injectedField.set( instance, value );
            }
            catch( IllegalAccessException e )
            {
                throw new InjectionException( e );
            }
        }

        public void visitDependency( DependencyVisitor visitor )
        {
            visitor.visit( dependencyModel, resolution );
        }
    }
}