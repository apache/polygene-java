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

import org.qi4j.api.common.InvalidApplicationException;
import org.qi4j.api.constraint.Constraint;
import org.qi4j.api.constraint.ConstraintImplementationNotFoundException;
import org.qi4j.api.constraint.Constraints;
import org.qi4j.api.util.Classes;
import org.qi4j.functional.Iterables;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static org.qi4j.api.util.Annotations.hasAnnotation;
import static org.qi4j.api.util.Annotations.type;
import static org.qi4j.functional.Iterables.filter;
import static org.qi4j.functional.Specifications.translate;

/**
 * JAVADOC
 */
public final class ConstraintsModel
{
    private final List<ConstraintDeclaration> constraints = new ArrayList<ConstraintDeclaration>();
    private final Class declaringType;

    public ConstraintsModel( Class declaringType )
    {
        this.declaringType = declaringType;
        // Find constraint declarations
        Iterable<Type> interfaces = Classes.INTERFACES_OF.map( declaringType );

        for( Type anInterface : interfaces )
        {
            addConstraintDeclarations( anInterface );
        }
    }

    public ValueConstraintsModel constraintsFor( Iterable<Annotation> constraintAnnotations,
                                                 Type valueType,
                                                 String name,
                                                 boolean optional
    )
    {
        List<AbstractConstraintModel> constraintModels = new ArrayList<AbstractConstraintModel>();
        nextConstraint:
        for( Annotation constraintAnnotation : filter( translate( type(), hasAnnotation( org.qi4j.api.constraint.ConstraintDeclaration.class ) ), constraintAnnotations ) )
        {
            // Check composite declarations first
            Class<? extends Annotation> annotationType = constraintAnnotation.annotationType();
            for( ConstraintDeclaration constraint : constraints )
            {
                if( constraint.appliesTo( annotationType, valueType ) )
                {
                    constraintModels.add( new ConstraintModel( constraintAnnotation, constraint.constraintClass() ) );
                    continue nextConstraint;
                }
            }

            // Check the annotation itself
            Constraints constraints = annotationType.getAnnotation( Constraints.class );
            if( constraints != null )
            {
                for( Class<? extends Constraint<?, ?>> constraintClass : constraints.value() )
                {
                    ConstraintDeclaration declaration = new ConstraintDeclaration( constraintClass, constraintAnnotation
                        .annotationType() );
                    if( declaration.appliesTo( annotationType, valueType ) )
                    {
                        constraintModels.add( new ConstraintModel( constraintAnnotation, declaration.constraintClass() ) );
                        continue nextConstraint;
                    }
                }
            }

            // No implementation found!

            // Check if if it's a composite constraints
            if( Iterables.matchesAny( translate( type(), hasAnnotation( org.qi4j.api.constraint.ConstraintDeclaration.class ) ), asList( constraintAnnotation
                    .annotationType()
                    .getAnnotations() ) ) )
            {
                ValueConstraintsModel valueConstraintsModel = constraintsFor( asList( constraintAnnotation.annotationType()
                                                                                          .getAnnotations() ), valueType, name, optional );
                CompositeConstraintModel compositeConstraintModel = new CompositeConstraintModel( constraintAnnotation, valueConstraintsModel );
                constraintModels.add( compositeConstraintModel );
                continue nextConstraint;
            }

            throw new ConstraintImplementationNotFoundException( declaringType, constraintAnnotation.annotationType(), valueType );
        }

        return new ValueConstraintsModel( constraintModels, name, optional );
    }

    private void addConstraintDeclarations( Type type )
    {
        if( type instanceof Class )
        {
            Class<?> clazz = (Class<?>) type;
            try
            {
                Constraints annotation = clazz.getAnnotation( Constraints.class );

                if( annotation != null )
                {
                    Class<? extends Constraint<?, ?>>[] constraintClasses = annotation.value();
                    for( Class<? extends Constraint<?, ?>> constraintClass : constraintClasses )
                    {
                        constraints.add( new ConstraintDeclaration( constraintClass, type ) );
                    }
                }
            }
            catch( Exception e )
            {
                throw new InvalidApplicationException( "Could not get Constraints for type " + clazz.getName(), e );
            }
        }
    }
}
