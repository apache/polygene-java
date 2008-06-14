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

package org.qi4j.library.framework.executor;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.Executor;
import org.qi4j.composite.AppliesTo;
import org.qi4j.composite.GenericSideEffect;
import org.qi4j.injection.scope.Invocation;
import org.qi4j.injection.scope.Service;
import org.qi4j.injection.scope.Structure;
import org.qi4j.object.ObjectBuilderFactory;

/**
 * TODO
 */
@AppliesTo( ExecuteSideEffect.class )
public class ExecutorSideEffect extends GenericSideEffect
{
    @Structure ObjectBuilderFactory objectBuilderFactory;
    @Service Executor executor;

    @Invocation ExecuteSideEffect execute;

    @Override public Object invoke( final Object target, final Method method, final Object[] objects ) throws Throwable
    {
        executor.execute( new SideEffectRunnable( target, method, objects ) );

        return super.invoke( target, method, objects );
    }

    private class SideEffectRunnable implements Runnable
    {
        private final Object target;
        private final Method method;
        private final Object[] objects;

        public SideEffectRunnable( Object target, Method method, Object[] objects )
        {
            this.target = target;
            this.method = method;
            this.objects = objects;
        }

        public void run()
        {
            try
            {
                try
                {
                    Object executed = objectBuilderFactory.newObject( execute.value() );
                    runSideEffect( executed );
                }
                catch( InvocationTargetException e )
                {
                    throw e.getTargetException();
                }
            }
            catch( Throwable t )
            {
                t.printStackTrace();
            }
        }

        private void runSideEffect( Object executed )
            throws Throwable
        {
            if( executed instanceof InvocationHandler )
            {
                InvocationHandler handler = (InvocationHandler) executed;
                handler.invoke( target, method, objects );
            }
            else
            {
                method.invoke( executed, objects );
            }
        }
    }
}
