/**
 *
 * Copyright 2009-2011 Rickard Öberg AB
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.qi4j.library.rest.server.restlet;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.qi4j.api.composite.TransientComposite;
import org.qi4j.api.constraint.Constraint;
import org.qi4j.api.constraint.ConstraintDeclaration;
import org.qi4j.api.constraint.Constraints;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.object.NoSuchObjectException;
import org.qi4j.api.structure.Module;
import org.qi4j.library.rest.server.api.ObjectSelection;
import org.qi4j.library.rest.server.api.constraint.InteractionConstraint;
import org.qi4j.library.rest.server.api.constraint.InteractionConstraintDeclaration;
import org.qi4j.library.rest.server.api.constraint.InteractionValidation;
import org.qi4j.library.rest.server.api.constraint.RequiresValid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JAVADOC
 */
public class InteractionConstraintsService
    implements InteractionConstraints
{
    @Structure
    Module module;

    Logger logger = LoggerFactory.getLogger( InteractionConstraintsService.class );

    private Map<Method, InteractionConstraintsBinding> methodsConstraints = new ConcurrentHashMap<Method, InteractionConstraintsBinding>();
    private Map<Class, InteractionConstraintsBinding> classConstraints = new ConcurrentHashMap<Class, InteractionConstraintsBinding>();

    @Override
    public boolean isValid( Method method, ObjectSelection objectSelection, Module module )
    {
        return getConstraints( method, module ).isValid( objectSelection );
    }

    @Override
    public boolean isValid( Class resourceClass, ObjectSelection objectSelection, Module module )
    {
        return getConstraints( resourceClass, module ).isValid( objectSelection );
    }

    private InteractionConstraintsBinding getConstraints( Method method, Module module )
    {
        InteractionConstraintsBinding constraintBindings = methodsConstraints.get( method );
        if( constraintBindings == null )
        {
            constraintBindings = findConstraints( method, module );
            methodsConstraints.put( method, constraintBindings );
        }
        return constraintBindings;
    }

    private InteractionConstraintsBinding getConstraints( Class aClass, Module module )
    {
        InteractionConstraintsBinding constraintBindings = classConstraints.get( aClass );
        if( constraintBindings == null )
        {
            constraintBindings = findConstraints( aClass, module );
            classConstraints.put( aClass, constraintBindings );
        }
        return constraintBindings;
    }

    private InteractionConstraintsBinding findConstraints( Method method, Module module )
    {
        List<Binding> methodConstraintBindings = new ArrayList<Binding>();

        for( Annotation annotation : method.getAnnotations() )
        {
            if( annotation.annotationType().equals( RequiresValid.class ) )
            {
                RequiresValid requiresValid = (RequiresValid) annotation;

                Class contextClass = method.getDeclaringClass();
                if( InteractionValidation.class.isAssignableFrom( contextClass ) )
                {
                    InteractionValidation validation = null;
                    if( TransientComposite.class.isAssignableFrom( contextClass ) )
                    {
                        validation = (InteractionValidation) module.newTransient( contextClass );
                    }
                    else
                    {
                        validation = (InteractionValidation) module.newObject( contextClass );
                    }
                    methodConstraintBindings.add( new RequiresValidBinding( requiresValid, validation ) );
                }
            }
            else if( annotation.annotationType().getAnnotation( ConstraintDeclaration.class ) != null )
            {
                Constraints constraints = annotation.annotationType().getAnnotation( Constraints.class );

                for( Class<? extends Constraint<?, ?>> aClass : constraints.value() )
                {
                    try
                    {
                        Constraint<Annotation, Object> constraint = (Constraint<Annotation, Object>) aClass.newInstance();
                        Class roleClass = (Class) ( (ParameterizedType) aClass.getGenericInterfaces()[ 0 ] ).getActualTypeArguments()[ 1 ];
                        ConstraintBinding constraintBinding = new ConstraintBinding( constraint, annotation, roleClass );
                        methodConstraintBindings.add( constraintBinding );
                    }
                    catch( InstantiationException e )
                    {
                        e.printStackTrace();
                    }
                    catch( IllegalAccessException e )
                    {
                        e.printStackTrace();
                    }
                }
            }
            else if( annotation.annotationType().getAnnotation( InteractionConstraintDeclaration.class ) != null )
            {
                Class<? extends InteractionConstraint> constraintClass = annotation.annotationType()
                    .getAnnotation( InteractionConstraintDeclaration.class )
                    .value();
                InteractionConstraint<Annotation> constraint = null;
                try
                {
                    try
                    {
                        constraint = module.newObject( constraintClass );
                    }
                    catch( NoSuchObjectException e )
                    {
                        constraint = constraintClass.newInstance();
                    }
                }
                catch( Exception e )
                {
                    continue; // Skip this constraint
                }
                InteractionConstraintBinding constraintBinding = new InteractionConstraintBinding( constraint, annotation );
                methodConstraintBindings.add( constraintBinding );
            }
        }

        if( methodConstraintBindings.isEmpty() )
        {
            methodConstraintBindings = null;
        }

        return new InteractionConstraintsBinding( methodConstraintBindings );
    }

    private InteractionConstraintsBinding findConstraints( Class aClass, Module module )
    {
        List<Binding> classConstraintBindings = new ArrayList<Binding>();

        for( Annotation annotation : aClass.getAnnotations() )
        {
            if( annotation.annotationType().getAnnotation( ConstraintDeclaration.class ) != null )
            {
                Constraints constraints = annotation.annotationType().getAnnotation( Constraints.class );

                for( Class<? extends Constraint<?, ?>> constraintClass : constraints.value() )
                {
                    try
                    {
                        Constraint<Annotation, Object> constraint = (Constraint<Annotation, Object>) constraintClass.newInstance();
                        Class roleClass = (Class) ( (ParameterizedType) constraint.getClass()
                            .getGenericInterfaces()[ 0 ] ).getActualTypeArguments()[ 1 ];
                        ConstraintBinding constraintBinding = new ConstraintBinding( constraint, annotation, roleClass );
                        classConstraintBindings.add( constraintBinding );
                    }
                    catch( InstantiationException e )
                    {
                        e.printStackTrace();
                    }
                    catch( IllegalAccessException e )
                    {
                        e.printStackTrace();
                    }
                }
            }
            else if( annotation.annotationType().getAnnotation( InteractionConstraintDeclaration.class ) != null )
            {
                Class<? extends InteractionConstraint> constraintClass = annotation.annotationType()
                    .getAnnotation( InteractionConstraintDeclaration.class )
                    .value();
                InteractionConstraint<Annotation> constraint = null;
                try
                {
                    try
                    {
                        constraint = module.newObject( constraintClass );
                    }
                    catch( NoSuchObjectException e )
                    {
                        constraint = constraintClass.newInstance();
                    }
                }
                catch( Exception e )
                {
                    continue; // Skip this constraint
                }
                InteractionConstraintBinding constraintBinding = new InteractionConstraintBinding( constraint, annotation );
                classConstraintBindings.add( constraintBinding );
            }
        }

        if( classConstraintBindings.isEmpty() )
        {
            classConstraintBindings = null;
        }

        return new InteractionConstraintsBinding( classConstraintBindings );
    }

    interface Binding
    {
        boolean isValid( ObjectSelection objectSelection );
    }

    public static class InteractionConstraintsBinding
    {
        List<Binding> bindings;

        public InteractionConstraintsBinding( List<Binding> bindings )
        {
            this.bindings = bindings;
        }

        public boolean isValid( ObjectSelection objectSelection )
        {
            if( bindings != null )
            {
                for( Binding constraintBinding : bindings )
                {
                    if( !constraintBinding.isValid( objectSelection ) )
                    {
                        return false;
                    }
                }
            }

            return true;
        }
    }

    public class RequiresValidBinding
        implements Binding
    {
        RequiresValid annotation;
        private final InteractionValidation validation;

        public RequiresValidBinding( RequiresValid annotation, InteractionValidation validation )
        {
            this.validation = validation;
            this.annotation = annotation;
        }

        @Override
        public boolean isValid( ObjectSelection objectSelection )
        {
            try
            {
                return validation.isValid( annotation.value() );
            }
            catch( IllegalArgumentException e )
            {
                return false;
            }
            catch( Throwable e )
            {
                logger.warn( "Could not check validation constraint for '" + annotation.value() + "'", e );
                return false;
            }
        }
    }

    public class ConstraintBinding
        implements Binding
    {
        Constraint<Annotation, Object> constraint;
        Annotation annotation;
        Class roleClass;

        public ConstraintBinding( Constraint<Annotation, Object> constraint, Annotation annotation, Class roleClass )
        {
            this.constraint = constraint;
            this.annotation = annotation;
            this.roleClass = roleClass;
        }

        @Override
        public boolean isValid( ObjectSelection objectSelection )
        {
            try
            {
                Object checkedObject = roleClass.equals( ObjectSelection.class ) ? objectSelection : objectSelection.get( roleClass );

                return constraint.isValid( annotation, checkedObject );
            }
            catch( IllegalArgumentException e )
            {
                return false;
            }
            catch( Throwable e )
            {
                logger.warn( "Could not check constraint " + constraint.getClass().getName(), e );
                return false;
            }
        }
    }

    public class InteractionConstraintBinding
        implements Binding
    {
        InteractionConstraint<Annotation> constraint;
        Annotation annotation;

        public InteractionConstraintBinding( InteractionConstraint<Annotation> constraint, Annotation annotation )
        {
            this.constraint = constraint;
            this.annotation = annotation;
        }

        @Override
        public boolean isValid( ObjectSelection objectSelection )
        {
            try
            {
                return constraint.isValid( annotation, objectSelection );
            }
            catch( Throwable e )
            {
                logger.warn( "Could not check constraint " + constraint.getClass().getName(), e );
                return false;
            }
        }
    }
}