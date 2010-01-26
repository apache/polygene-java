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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.qi4j.bootstrap.BindingException;
import org.qi4j.bootstrap.InjectionException;
import org.qi4j.runtime.model.Resolution;
import org.qi4j.runtime.structure.ModelVisitor;
import org.qi4j.spi.composite.InjectedMethodDescriptor;
import org.qi4j.spi.util.SerializationUtil;

/**
 * JAVADOC
 */
public final class InjectedMethodModel
    implements InjectedMethodDescriptor, Serializable
{
    // Model
    private Method method;
    private InjectedParametersModel parameters;

    private void writeObject( ObjectOutputStream out )
        throws IOException
    {
        try
        {
            SerializationUtil.writeMethod( out, method );
            out.writeObject( parameters );
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
        method = SerializationUtil.readMethod( in );
        parameters = (InjectedParametersModel) in.readObject();
    }

    public InjectedMethodModel( Method method, InjectedParametersModel parameters )
    {
        this.method = method;
        this.method.setAccessible( true );
        this.parameters = parameters;
    }

    public Method method()
    {
        return method;
    }

    // Binding

    public void bind( Resolution resolution )
        throws BindingException
    {
        parameters.bind( resolution );
    }

    // Context

    public void inject( InjectionContext context, Object instance )
        throws InjectionException
    {
        Object[] params = parameters.newParametersInstance( context );
        try
        {
            if( !method.isAccessible() )
            {
                method.setAccessible( true );
            }
            method.invoke( instance, params );
        }
        catch( IllegalAccessException e )
        {
            throw new InjectionException( e );
        }
        catch( InvocationTargetException e )
        {
            throw new InjectionException( e.getTargetException() );
        }
    }

    public void visitModel( ModelVisitor modelVisitor )
    {
        modelVisitor.visit( this );
        parameters.visitModel( modelVisitor );
    }
}
