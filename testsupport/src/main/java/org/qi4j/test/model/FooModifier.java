/*
 * Copyright (C) Senselogic 2006, all rights reserved
 */
package org.qi4j.test.model;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.AnnotatedElement;
import org.qi4j.api.annotation.Modifies;
import org.qi4j.api.annotation.AppliesTo;
import org.qi4j.api.annotation.Dependency;

/**
 * Override only the methods that have the FooAnnotation, and print out the value of
 * the annotation on the mixin method.
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
    @Dependency Method foo;

    // InvocationHandler implementation -----------------------------
    public Object invoke( Object proxy, Method method, Object[] args ) throws Throwable
    {
        args[0] = args[0]+foo.getAnnotation( FooAnnotation.class).value();
        return next.invoke( proxy, method, args);
    }
}
