package org.qi4j.runtime;
/**
 *  TODO
 */

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.util.Iterator;
import junit.framework.TestCase;
import org.qi4j.api.model.MethodConstraint;
import org.qi4j.api.model.ParameterConstraint;
import org.qi4j.library.framework.constraint.annotation.Email;
import org.qi4j.library.framework.constraint.annotation.MaxLength;
import org.qi4j.library.framework.constraint.annotation.MinLength;
import org.qi4j.library.framework.constraint.annotation.Range;

public class ConstraintModelFactoryTest extends TestCase
{
    ConstraintModelFactory constraintModelFactory;


    @Override protected void setUp() throws Exception
    {
        constraintModelFactory = new ConstraintModelFactory();
    }

    public void testNewMethodConstraint() throws Exception
    {
        Method method = TestClass.class.getMethod( "doStuff" );
        MethodConstraint constraint = constraintModelFactory.newMethodConstraint( method );
        assertEquals( method, constraint.getMethod() );
        Iterator<ParameterConstraint> params = constraint.getParameterConstraints().iterator();

        ParameterConstraint param = params.next();
        assertEquals( String.class, param.getParameterType() );
        Iterator<? extends Annotation> constraints = param.getConstraints().iterator();
        Annotation annotation = constraints.next();
    }

    @Retention( RetentionPolicy.RUNTIME )
    @Target( { ElementType.PARAMETER, ElementType.ANNOTATION_TYPE } )
    public @interface NotAConstraint
    {
    }

    static class TestClass
    {
        public void doStuff( @NotAConstraint @MinLength( 5 ) @MaxLength( 10 )String foo, @Email String email, @Range( min = 3, max = 5 )int value )
        {
            // Ignore
        }
    }
}