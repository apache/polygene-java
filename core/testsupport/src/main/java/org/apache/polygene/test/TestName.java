package org.apache.polygene.test;

import java.lang.reflect.UndeclaredThrowableException;
import java.util.Optional;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import static org.junit.platform.commons.util.ReflectionUtils.HierarchyTraversalMode.BOTTOM_UP;
import static org.junit.platform.commons.util.ReflectionUtils.findFields;

public class TestName
    implements BeforeAllCallback, BeforeEachCallback
{
    private String testName;
    private String methodName;

    @Override
    public void beforeAll( ExtensionContext context )
        throws Exception
    {
        Optional<Class<?>> testClass = context.getTestClass();
        testClass.ifPresent( cls -> testName = cls.getName() );
        context.getTestMethod().ifPresent( m -> methodName = m.getName() );
        inject( context );
    }

    @Override
    public void beforeEach( ExtensionContext context )
        throws Exception
    {
        Optional<Class<?>> testClass = context.getTestClass();
        testClass.ifPresent( cls -> testName = cls.getName() );
        context.getTestMethod().ifPresent( m -> methodName = m.getName() );
        inject( context );
    }

    private void inject( ExtensionContext context )
    {
        findFields( context.getRequiredTestClass(),
                    f -> f.getType().equals( TestName.class ), BOTTOM_UP )
            .forEach( f -> {
                try
                {
                    f.setAccessible( true );
                    f.set( context.getRequiredTestInstance(), this );
                }
                catch( IllegalAccessException e )
                {
                    throw new UndeclaredThrowableException( e );
                }
            } );
    }

    public String getTestName()
    {
        return testName;
    }

    public String getMethodName()
    {
        return methodName;
    }
}
