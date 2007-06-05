/*
 * Copyright (C) Senselogic 2006, all rights reserved
 */
package org.qi4j.runtime;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.AnnotatedElement;
import org.qi4j.api.annotation.Modifies;
import org.qi4j.api.annotation.AppliesTo;
import org.qi4j.api.annotation.Dependency;

/**
 * TODO
 *
 * @author rickard
 * @version $Revision: 1.7 $
 */
@AppliesTo(FooAnnotation.class)
public class FooModifier
    implements InvocationHandler
{
    // Attributes ----------------------------------------------------
    @Modifies InvocationHandler next;
    @Dependency AnnotatedElement foo;

    // Z implementation ----------------------------------------------
    public Object invoke( Object proxy, Method method, Object[] args ) throws Throwable
    {
        System.out.println("Foo:"+foo.getAnnotation( FooAnnotation.class).value());
        return next.invoke( proxy, method, args);
    }
}
