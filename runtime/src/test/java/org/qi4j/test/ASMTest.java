/*
 * Copyright (c) 2010, Rickard Öberg. All Rights Reserved.
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

package org.qi4j.test;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.util.ASMifierClassVisitor;
import org.qi4j.runtime.composite.FragmentClassLoader;
import org.qi4j.spi.composite.CompositeInvoker;

import java.io.PrintWriter;
import java.lang.reflect.Method;

/**
 * JAVADOC
 */
public class ASMTest
{
    @Test
    public void generateASM()
            throws Exception
    {
        ASMifierClassVisitor.main( new String[]{SomeMixin_Stubx.class.getName()} );
    }

    @Test
//    @Ignore
public void createClass()
            throws Exception
    {
        byte[] asm = FragmentClassLoader.generateClass();

        byte[] cl = FragmentClassLoader.generateClass( SomeMixin.class.getName() + "_Stub", SomeMixin.class );

        ClassReader cr = new ClassReader( cl );
        cr.accept( new ASMifierClassVisitor( new PrintWriter( System.out, true ) ),
                ASMifierClassVisitor.getDefaultAttributes(),
                0 );


        //       Assert.assertArrayEquals( asm, cl );

        FragmentClassLoader classLoader = new FragmentClassLoader( SomeMixin.class.getClassLoader() );

        Class clazz = classLoader.loadClass( SomeMixin.class.getName() + "_Stub" );

//        Class clazz = SomeMixin_Stubx.class;

        final Other other = new Other()
        {
            public String other()
            {
                return "other";
            }

            public String foo( String bar, int x )
            {
                return "bar:" + bar;
            }

            public void bar( double doub, boolean bool, float fl, char ch, int integer, long lg, short sh, byte b, Double doubObj, Object[] objArr, int[] intArr )
            {
            }

            public void multiEx( String bar ) throws Exception1, Exception2
            {
            }

            public int unwrapResult()
            {
                return 0;
            }
        };

        final Some instance = (Some) clazz.getConstructor().newInstance();

        CompositeInvoker invoker = new CompositeInvoker()
        {
            public Object invokeComposite( Method method, Object[] args ) throws Throwable
            {
                if( method.getDeclaringClass().isInstance( instance ) )
                {
                    Method fakeMethod = instance.getClass().getMethod( "_" + method.getName(), method.getParameterTypes() );

                    return fakeMethod.invoke( instance, args );
                } else
                {
                    return method.invoke( other, args );
                }
            }
        };

        clazz.getField( "_instance" ).set( instance, invoker );

//        System.out.println( instance.some() );

        System.out.println( instance.testConcern() );
    }


}
