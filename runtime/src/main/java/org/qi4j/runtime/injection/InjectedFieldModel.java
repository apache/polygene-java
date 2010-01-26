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

package org.qi4j.runtime.injection;

import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.Collection;
import org.qi4j.bootstrap.BindingException;
import org.qi4j.bootstrap.InjectionException;
import org.qi4j.runtime.model.Resolution;
import org.qi4j.runtime.structure.ModelVisitor;
import org.qi4j.runtime.structure.Specification;
import org.qi4j.spi.composite.InjectedFieldDescriptor;
import org.qi4j.spi.util.SerializationUtil;

import static java.util.Collections.*;

/**
 * JAVADOC
 */
public final class InjectedFieldModel
    implements InjectedFieldDescriptor, Serializable
{
    private DependencyModel dependencyModel;
    private Field injectedField;

    private void writeObject( ObjectOutputStream out )
        throws IOException
    {
        try
        {
            out.writeObject( dependencyModel );
            SerializationUtil.writeField( out, injectedField );
        }
        catch( NotSerializableException e )
        {
            System.err.println( "NotSerializable in " + getClass() );
            throw e;
        }
    }

    private void readObject( ObjectInputStream in )
        throws IOException, ClassNotFoundException
    {
        dependencyModel = (DependencyModel) in.readObject();
        injectedField = SerializationUtil.readField( in );
    }

    public InjectedFieldModel( Field injectedField, DependencyModel dependencyModel )
    {
        injectedField.setAccessible( true );
        this.injectedField = injectedField;
        this.dependencyModel = dependencyModel;
    }

    public DependencyModel dependency()
    {
        return dependencyModel;
    }

    public Field field()
    {
        return injectedField;
    }

    public void bind( Resolution resolution )
        throws BindingException
    {
        dependencyModel.bind( resolution.forField( injectedField ) );
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
        catch( IllegalArgumentException e )
        {
            String fieldClassName = injectedField.getType().getName();
            String valueClassName = value.getClass().getName();
            String message = "Cannot inject field of type " + fieldClassName + " with value '" + value +
                             "' of type " + valueClassName;
            throw new InjectionException( message, e );
        }
    }

    public void visitModel( ModelVisitor modelVisitor )
    {
        modelVisitor.visit( this );
    }

    public Collection<DependencyModel> filter( Specification<DependencyModel> specification )
    {
        if( specification.matches( dependencyModel ) )
        {
            return singleton( dependencyModel );
        }
        else
        {
            return emptyList();
        }
    }
}
