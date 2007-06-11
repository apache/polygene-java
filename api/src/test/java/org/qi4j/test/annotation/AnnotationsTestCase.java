/*  Copyright 2007 Niclas Hedhman.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.
 * 
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.qi4j.test.annotation;

import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.util.Arrays;
import junit.framework.TestCase;
import org.qi4j.api.annotation.AppliesTo;
import org.qi4j.api.annotation.Dependency;
import org.qi4j.api.annotation.ImplementedBy;
import org.qi4j.api.annotation.ModifiedBy;
import org.qi4j.api.annotation.Modifies;
import org.qi4j.api.annotation.Uses;

public class AnnotationsTestCase extends TestCase
{

    public void testAppliesTo()
        throws Exception
    {
        validateClass( ClassAnnotatedWithAppliesTo.class, AppliesTo.class );
        Annotation annotation = ClassAnnotatedWithAppliesTo.class.getAnnotation( AppliesTo.class );
        validateTarget( annotation, AppliesTo.class, ElementType.TYPE );
    }

    public void testImplementedBy()
        throws Exception
    {
        validateClass( ClassAnnotatedWithImplementedBy.class, ImplementedBy.class );
        Annotation annotation = ClassAnnotatedWithImplementedBy.class.getAnnotation( ImplementedBy.class );
        validateTarget( annotation, ImplementedBy.class, ElementType.TYPE );
    }

    public void testModifiedBy()
        throws Exception
    {
        validateClass( ClassAnnotatedWithModifiedBy.class, ModifiedBy.class );
        Annotation annotation = ClassAnnotatedWithModifiedBy.class.getAnnotation( ModifiedBy.class );
        validateTarget( annotation, ModifiedBy.class, ElementType.TYPE );
    }

    public void testDependency()
        throws Exception
    {
        Field abc = ClassAnnotatedWithDependency.class.getField( "abc" );
        assertNotNull( abc );
        validateField( abc, Dependency.class );
        Annotation annotation = abc.getAnnotation( Dependency.class );
        validateTarget( annotation, Dependency.class, ElementType.FIELD );
    }

    public void testModifies()
        throws Exception
    {
        Field abc = ClassAnnotatedWithModifies.class.getField( "abc" );
        assertNotNull( abc );
        validateField( abc, Modifies.class );
        Annotation annotation = abc.getAnnotation( Modifies.class );
        validateTarget( annotation, Modifies.class, ElementType.FIELD );
    }

    public void testUses()
        throws Exception
    {
        Field abc = ClassAnnotatedWithUses.class.getField( "abc" );
        assertNotNull( abc );
        validateField( abc, Uses.class );
        Annotation annotation = abc.getAnnotation( Uses.class );
        validateTarget( annotation, Uses.class, ElementType.FIELD );
    }

    private void validateField( Field aField, Class annotationClass )
    {
        Annotation annotation = aField.getAnnotation( annotationClass );
        validateRetention( annotation, annotationClass );
        validateInherited( annotation, annotationClass );
        validateDocumented( annotation, annotationClass );
    }

    private void validateClass( Class aClass, Class annotationClass )
    {
        Annotation annotation = aClass.getAnnotation( annotationClass );
        validateRetention( annotation, annotationClass );
        validateInherited( annotation, annotationClass );
        validateDocumented( annotation, annotationClass );
    }

    private void validateRetention( Annotation annotation, Class annotationClass )
    {
        assertNotNull( annotationClass + " is not annotated with @Retention( RetentionPolicy.RUNTIME )", annotation );
    }

    private void validateTarget( Annotation annotation, Class annotationClass, ElementType... types )
    {
        Class<? extends Annotation> annotationType = annotation.annotationType();
        Target target = annotationType.getAnnotation( Target.class );
        assertNotNull( target );
        ElementType[] elementTypes = target.value();
        String message = "ElementTypes in @Target of " +
                         annotationClass + " is not as expected." +
                         " Expected:" + Arrays.toString( types ) + ", " +
                         " was: " + Arrays.toString( elementTypes );
        assertTrue( message, Arrays.equals( elementTypes, types ) );
    }

    private void validateInherited( Annotation annotation, Class annotationClass )
    {
        Class<? extends Annotation> annotationType = annotation.annotationType();
        Inherited target = annotationType.getAnnotation( Inherited.class );
        assertNotNull( annotationClass + " is not annotated with @Inherited.", target );
    }

    private void validateDocumented( Annotation annotation, Class annotationClass )
    {
        Class<? extends Annotation> annotationType = annotation.annotationType();
        Documented target = annotationType.getAnnotation( Documented.class );
        assertNotNull( annotationClass + " is not annotated with @Documented.", target );
    }
}
