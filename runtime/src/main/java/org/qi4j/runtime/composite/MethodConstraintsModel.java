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

package org.qi4j.runtime.composite;

import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import org.qi4j.api.common.Optional;
import org.qi4j.api.constraint.Name;
import org.qi4j.runtime.structure.ModelVisitor;
import org.qi4j.spi.constraint.MethodConstraintsDescriptor;
import org.qi4j.spi.util.Annotations;
import org.qi4j.spi.util.SerializationUtil;

/**
 * JAVADOC
 */
public final class MethodConstraintsModel
    implements MethodConstraintsDescriptor, Serializable
{
    private List<ValueConstraintsModel> parameterConstraintModels;
    private Method method;

    private void writeObject( ObjectOutputStream out )
        throws IOException
    {
        try
        {
            SerializationUtil.writeMethod( out, method );
            out.writeObject( parameterConstraintModels );
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
        parameterConstraintModels = (List<ValueConstraintsModel>) in.readObject();
    }

    public MethodConstraintsModel( Method method, ConstraintsModel constraintsModel )
    {
        this.method = method;
        parameterConstraintModels = null;
        Annotation[][] parameterAnnotations = method.getParameterAnnotations();
        Type[] parameterTypes = method.getGenericParameterTypes();
        boolean constrained = false;
        for( int i = 0; i < parameterAnnotations.length; i++ )
        {
            Annotation[] parameterAnnotation = parameterAnnotations[ i ];

            Name nameAnnotation = Annotations.getAnnotationOfType( parameterAnnotation, Name.class );
            String name = nameAnnotation == null ? "param" + ( i + 1 ) : nameAnnotation.value();

            boolean optional = Annotations.getAnnotationOfType( parameterAnnotation, Optional.class ) != null;
            ValueConstraintsModel parameterConstraintsModel = constraintsModel.constraintsFor( parameterAnnotation, parameterTypes[ i ], name, optional );
            if( parameterConstraintsModel.isConstrained() )
            {
                constrained = true;
            }

            if( parameterConstraintModels == null )
            {
                parameterConstraintModels = new ArrayList<ValueConstraintsModel>();
            }
            parameterConstraintModels.add( parameterConstraintsModel );
        }

        if( !constrained )
        {
            parameterConstraintModels = null; // No constraints for this method
        }
    }

    public Method method()
    {
        return method;
    }

    public boolean isConstrained()
    {
        return !parameterConstraintModels.isEmpty();
    }

    public MethodConstraintsInstance newInstance()
    {
        return parameterConstraintModels == null ? new MethodConstraintsInstance() : new MethodConstraintsInstance( method, parameterConstraintModels );
    }

    public void visitModel( ModelVisitor modelVisitor )
    {
        modelVisitor.visit( this );
        if( parameterConstraintModels == null )
        {
            return;
        }

        for( ValueConstraintsModel parameterConstraintModel : parameterConstraintModels )
        {
            parameterConstraintModel.visitModel( modelVisitor );
        }
    }
}
