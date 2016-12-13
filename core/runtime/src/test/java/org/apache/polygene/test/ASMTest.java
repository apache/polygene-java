/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 */

package org.apache.polygene.test;

import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.util.ASMifier;
import org.objectweb.asm.util.TraceClassVisitor;
import org.apache.polygene.api.composite.CompositeInvoker;
import org.apache.polygene.runtime.composite.FragmentClassLoader;
import org.apache.polygene.runtime.composite.QI256Test;

import static org.objectweb.asm.Opcodes.*;

public class ASMTest
{
    @Test
    public void generateASM()
        throws Exception
    {
        ASMifier.main( new String[]{ SomeMixin_Stubx.class.getName() } );
    }

    @Test
    public void createClass()
        throws Exception
    {

        FragmentClassLoader classLoader = new FragmentClassLoader( SomeMixin.class.getClassLoader() );

        Class clazz = classLoader.loadClass( SomeMixin.class.getName() + "_Stub" );

        //       Class clazz = SomeMixin_Stubx.class;

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

            public void bar( double doub,
                             boolean bool,
                             float fl,
                             char ch,
                             int integer,
                             long lg,
                             short sh,
                             byte b,
                             Double doubObj,
                             Object[] objArr,
                             int[] intArr
            )
            {
            }

            public void multiEx( String bar )
                throws Exception1, Exception2
            {
            }

            public long unwrapResult()
            {
                return 0;
            }

            public void generic( List<String> list )
            {
                list.add( "Hello World" );
            }
        };

        final Some instance = (Some) clazz.getConstructor().newInstance();

        CompositeInvoker invoker = new CompositeInvoker()
        {
            public Object invokeComposite( Method method, Object[] args )
                throws Throwable
            {
                Method fakeMethod = null;
                try
                {
                    fakeMethod = instance.getClass().getMethod( "_" + method.getName(), method.getParameterTypes() );

                    try
                    {
                        return fakeMethod.invoke( instance, args );
                    }
                    catch( InvocationTargetException e )
                    {
                        throw e.getCause();
                    }
                }
                catch( NoSuchMethodException e )
                {
                    try
                    {
                        return method.invoke( other, args );
                    }
                    catch( InvocationTargetException ex )
                    {
                        throw ex.getCause();
                    }
                }
            }
        };

        clazz.getField( "_instance" ).set( instance, invoker );

        System.out.println( instance.some() );

        System.out.println( instance.testConcern() );
    }

    @Test
    @Ignore( "Convenience to look at what code is generated in the Fragment Classloader, and is not really a test case." )
    public void fragmentClassLoaderGenerateClassTest()
        throws Exception
    {
        FragmentClassLoader classLoader = new FragmentClassLoader( getClass().getClassLoader() );
        byte[] asm = generateClass();
        byte[] cl = classLoader.generateClass(
            QI256Test.TestTransient.TestTransientMixin.class.getName() + "_Stub",
            QI256Test.TestTransient.TestTransientMixin.class );

        new ClassReader( cl ).accept( new TraceClassVisitor( new PrintWriter( System.out, true ) ), 0 );

        Assert.assertArrayEquals( asm, cl );
    }

    // This is the code generated from the manual stub
    private static byte[] generateClass()
    {
        ClassWriter cw = new ClassWriter( 0 );
        FieldVisitor fv;
        MethodVisitor mv;
        AnnotationVisitor av0;

        cw.visit( V1_6, ACC_PUBLIC + ACC_SUPER, "org/apache/polygene/satisfiedBy/SomeMixin_Stub", null,
                  "org/apache/polygene/satisfiedBy/SomeMixin", null );

        {
            fv = cw.visitField( ACC_PUBLIC, "_instance", "Lorg/apache/polygene/spi/composite/CompositeInvoker;", null, null );
            fv.visitEnd();
        }
        {
            fv = cw.visitField( ACC_PRIVATE + ACC_STATIC, "m1", "Ljava/lang/reflect/Method;", null, null );
            fv.visitEnd();
        }
        {
            fv = cw.visitField( ACC_PRIVATE + ACC_STATIC, "m2", "Ljava/lang/reflect/Method;", null, null );
            fv.visitEnd();
        }
        {
            fv = cw.visitField( ACC_PRIVATE + ACC_STATIC, "m3", "Ljava/lang/reflect/Method;", null, null );
            fv.visitEnd();
        }
        {
            fv = cw.visitField( ACC_PRIVATE + ACC_STATIC, "m4", "Ljava/lang/reflect/Method;", null, null );
            fv.visitEnd();
        }
        {
            fv = cw.visitField( ACC_PRIVATE + ACC_STATIC, "m5", "Ljava/lang/reflect/Method;", null, null );
            fv.visitEnd();
        }
        {
            mv = cw.visitMethod( ACC_PUBLIC, "<init>", "()V", null, null );
            mv.visitCode();
            mv.visitVarInsn( ALOAD, 0 );
            mv.visitMethodInsn( INVOKESPECIAL, "org/apache/polygene/satisfiedBy/SomeMixin", "<init>", "()V", false );
            mv.visitInsn( RETURN );
            mv.visitMaxs( 1, 1 );
            mv.visitEnd();
        }
        {
            mv = cw.visitMethod( ACC_PUBLIC, "<init>", "(Ljava/lang/String;)V", null, null );
            mv.visitCode();
            mv.visitVarInsn( ALOAD, 0 );
            mv.visitVarInsn( ALOAD, 1 );
            mv.visitMethodInsn( INVOKESPECIAL, "org/apache/polygene/satisfiedBy/SomeMixin", "<init>", "(Ljava/lang/String;)V", false );
            mv.visitInsn( RETURN );
            mv.visitMaxs( 2, 2 );
            mv.visitEnd();
        }
        {
            mv = cw.visitMethod( ACC_PUBLIC, "other", "()Ljava/lang/String;", null, null );
            mv.visitCode();
            Label l0 = new Label();
            Label l1 = new Label();
            Label l2 = new Label();
            mv.visitTryCatchBlock( l0, l1, l2, "java/lang/Throwable" );
            mv.visitLabel( l0 );
            mv.visitVarInsn( ALOAD, 0 );
            mv.visitFieldInsn( GETFIELD, "org/apache/polygene/satisfiedBy/SomeMixin_Stub", "_instance",
                               "Lorg/apache/polygene/spi/composite/CompositeInvoker;" );
            mv.visitFieldInsn( GETSTATIC, "org/apache/polygene/satisfiedBy/SomeMixin_Stub", "m1", "Ljava/lang/reflect/Method;" );
            mv.visitInsn( ACONST_NULL );
            mv.visitMethodInsn( INVOKEINTERFACE, "org/apache/polygene/spi/composite/CompositeInvoker", "invokeComposite",
                                "(Ljava/lang/reflect/Method;[Ljava/lang/Object;)Ljava/lang/Object;", true );
            mv.visitTypeInsn( CHECKCAST, "java/lang/String" );
            mv.visitLabel( l1 );
            mv.visitInsn( ARETURN );
            mv.visitLabel( l2 );
            mv.visitFrame( Opcodes.F_SAME1, 0, null, 1, new Object[]{ "java/lang/Throwable" } );
            mv.visitVarInsn( ASTORE, 1 );
            mv.visitTypeInsn( NEW, "java/lang/reflect/UndeclaredThrowableException" );
            mv.visitInsn( DUP );
            mv.visitVarInsn( ALOAD, 1 );
            mv.visitMethodInsn( INVOKESPECIAL, "java/lang/reflect/UndeclaredThrowableException", "<init>",
                                "(Ljava/lang/Throwable;)V", false );
            mv.visitInsn( ATHROW );
            mv.visitMaxs( 3, 2 );
            mv.visitEnd();
        }
        {
            mv = cw.visitMethod( ACC_PUBLIC, "foo", "(Ljava/lang/String;I)Ljava/lang/String;", null,
                                 new String[]{ "java/lang/IllegalArgumentException" } );
            mv.visitCode();
            Label l0 = new Label();
            Label l1 = new Label();
            Label l2 = new Label();
            mv.visitTryCatchBlock( l0, l1, l2, "java/lang/IllegalArgumentException" );
            Label l3 = new Label();
            mv.visitTryCatchBlock( l0, l1, l3, "java/lang/Throwable" );
            mv.visitLabel( l0 );
            mv.visitVarInsn( ALOAD, 0 );
            mv.visitFieldInsn( GETFIELD, "org/apache/polygene/satisfiedBy/SomeMixin_Stub", "_instance",
                               "Lorg/apache/polygene/spi/composite/CompositeInvoker;" );
            mv.visitFieldInsn( GETSTATIC, "org/apache/polygene/satisfiedBy/SomeMixin_Stub", "m2", "Ljava/lang/reflect/Method;" );
            mv.visitInsn( ICONST_2 );
            mv.visitTypeInsn( ANEWARRAY, "java/lang/Object" );
            mv.visitInsn( DUP );
            mv.visitInsn( ICONST_0 );
            mv.visitVarInsn( ALOAD, 1 );
            mv.visitInsn( AASTORE );
            mv.visitInsn( DUP );
            mv.visitInsn( ICONST_1 );
            mv.visitVarInsn( ILOAD, 2 );
            mv.visitMethodInsn( INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false );
            mv.visitInsn( AASTORE );
            mv.visitMethodInsn( INVOKEINTERFACE, "org/apache/polygene/spi/composite/CompositeInvoker", "invokeComposite",
                                "(Ljava/lang/reflect/Method;[Ljava/lang/Object;)Ljava/lang/Object;", true );
            mv.visitTypeInsn( CHECKCAST, "java/lang/String" );
            mv.visitLabel( l1 );
            mv.visitInsn( ARETURN );
            mv.visitLabel( l2 );
            mv.visitFrame( Opcodes.F_SAME1, 0, null, 1, new Object[]{ "java/lang/IllegalArgumentException" } );
            mv.visitVarInsn( ASTORE, 3 );
            mv.visitVarInsn( ALOAD, 3 );
            mv.visitInsn( ATHROW );
            mv.visitLabel( l3 );
            mv.visitFrame( Opcodes.F_SAME1, 0, null, 1, new Object[]{ "java/lang/Throwable" } );
            mv.visitVarInsn( ASTORE, 3 );
            mv.visitTypeInsn( NEW, "java/lang/reflect/UndeclaredThrowableException" );
            mv.visitInsn( DUP );
            mv.visitVarInsn( ALOAD, 3 );
            mv.visitMethodInsn( INVOKESPECIAL, "java/lang/reflect/UndeclaredThrowableException", "<init>",
                                "(Ljava/lang/Throwable;)V", false );
            mv.visitInsn( ATHROW );
            mv.visitMaxs( 6, 4 );
            mv.visitEnd();
        }
        {
            mv = cw.visitMethod( ACC_PUBLIC, "bar", "(DZFCIJSBLjava/lang/Double;[Ljava/lang/Object;[I)V", null, null );
            mv.visitCode();
            Label l0 = new Label();
            Label l1 = new Label();
            Label l2 = new Label();
            mv.visitTryCatchBlock( l0, l1, l2, "java/lang/Throwable" );
            mv.visitLabel( l0 );
            mv.visitVarInsn( ALOAD, 0 );
            mv.visitFieldInsn( GETFIELD, "org/apache/polygene/satisfiedBy/SomeMixin_Stub", "_instance",
                               "Lorg/apache/polygene/spi/composite/CompositeInvoker;" );
            mv.visitFieldInsn( GETSTATIC, "org/apache/polygene/satisfiedBy/SomeMixin_Stub", "m3", "Ljava/lang/reflect/Method;" );
            mv.visitIntInsn( BIPUSH, 11 );
            mv.visitTypeInsn( ANEWARRAY, "java/lang/Object" );
            mv.visitInsn( DUP );
            mv.visitInsn( ICONST_0 );
            mv.visitVarInsn( DLOAD, 1 );
            mv.visitMethodInsn( INVOKESTATIC, "java/lang/Double", "valueOf", "(D)Ljava/lang/Double;", false );
            mv.visitInsn( AASTORE );
            mv.visitInsn( DUP );
            mv.visitInsn( ICONST_1 );
            mv.visitVarInsn( ILOAD, 3 );
            mv.visitMethodInsn( INVOKESTATIC, "java/lang/Boolean", "valueOf", "(Z)Ljava/lang/Boolean;", false );
            mv.visitInsn( AASTORE );
            mv.visitInsn( DUP );
            mv.visitInsn( ICONST_2 );
            mv.visitVarInsn( FLOAD, 4 );
            mv.visitMethodInsn( INVOKESTATIC, "java/lang/Float", "valueOf", "(F)Ljava/lang/Float;", false );
            mv.visitInsn( AASTORE );
            mv.visitInsn( DUP );
            mv.visitInsn( ICONST_3 );
            mv.visitVarInsn( ILOAD, 5 );
            mv.visitMethodInsn( INVOKESTATIC, "java/lang/Character", "valueOf", "(C)Ljava/lang/Character;", false );
            mv.visitInsn( AASTORE );
            mv.visitInsn( DUP );
            mv.visitInsn( ICONST_4 );
            mv.visitVarInsn( ILOAD, 6 );
            mv.visitMethodInsn( INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false );
            mv.visitInsn( AASTORE );
            mv.visitInsn( DUP );
            mv.visitInsn( ICONST_5 );
            mv.visitVarInsn( LLOAD, 7 );
            mv.visitMethodInsn( INVOKESTATIC, "java/lang/Long", "valueOf", "(J)Ljava/lang/Long;", false );
            mv.visitInsn( AASTORE );
            mv.visitInsn( DUP );
            mv.visitIntInsn( BIPUSH, 6 );
            mv.visitVarInsn( ILOAD, 9 );
            mv.visitMethodInsn( INVOKESTATIC, "java/lang/Short", "valueOf", "(S)Ljava/lang/Short;", false );
            mv.visitInsn( AASTORE );
            mv.visitInsn( DUP );
            mv.visitIntInsn( BIPUSH, 7 );
            mv.visitVarInsn( ILOAD, 10 );
            mv.visitMethodInsn( INVOKESTATIC, "java/lang/Byte", "valueOf", "(B)Ljava/lang/Byte;", false );
            mv.visitInsn( AASTORE );
            mv.visitInsn( DUP );
            mv.visitIntInsn( BIPUSH, 8 );
            mv.visitVarInsn( ALOAD, 11 );
            mv.visitInsn( AASTORE );
            mv.visitInsn( DUP );
            mv.visitIntInsn( BIPUSH, 9 );
            mv.visitVarInsn( ALOAD, 12 );
            mv.visitInsn( AASTORE );
            mv.visitInsn( DUP );
            mv.visitIntInsn( BIPUSH, 10 );
            mv.visitVarInsn( ALOAD, 13 );
            mv.visitInsn( AASTORE );
            mv.visitMethodInsn( INVOKEINTERFACE, "org/apache/polygene/spi/composite/CompositeInvoker", "invokeComposite",
                                "(Ljava/lang/reflect/Method;[Ljava/lang/Object;)Ljava/lang/Object;", true );
            mv.visitInsn( POP );
            mv.visitLabel( l1 );
            Label l3 = new Label();
            mv.visitJumpInsn( GOTO, l3 );
            mv.visitLabel( l2 );
            mv.visitFrame( Opcodes.F_SAME1, 0, null, 1, new Object[]{ "java/lang/Throwable" } );
            mv.visitVarInsn( ASTORE, 14 );
            mv.visitTypeInsn( NEW, "java/lang/reflect/UndeclaredThrowableException" );
            mv.visitInsn( DUP );
            mv.visitVarInsn( ALOAD, 14 );
            mv.visitMethodInsn( INVOKESPECIAL, "java/lang/reflect/UndeclaredThrowableException", "<init>",
                                "(Ljava/lang/Throwable;)V", false );
            mv.visitInsn( ATHROW );
            mv.visitLabel( l3 );
            mv.visitFrame( Opcodes.F_SAME, 0, null, 0, null );
            mv.visitInsn( RETURN );
            mv.visitMaxs( 7, 15 );
            mv.visitEnd();
        }
        {
            mv = cw.visitMethod( ACC_PUBLIC, "multiEx", "(Ljava/lang/String;)V", null,
                                 new String[]{ "org/apache/polygene/satisfiedBy/Exception1", "org/apache/polygene/satisfiedBy/Exception2" } );
            mv.visitCode();
            Label l0 = new Label();
            Label l1 = new Label();
            Label l2 = new Label();
            mv.visitTryCatchBlock( l0, l1, l2, "org/apache/polygene/satisfiedBy/Exception1" );
            Label l3 = new Label();
            mv.visitTryCatchBlock( l0, l1, l3, "org/apache/polygene/satisfiedBy/Exception2" );
            Label l4 = new Label();
            mv.visitTryCatchBlock( l0, l1, l4, "java/lang/Throwable" );
            mv.visitLabel( l0 );
            mv.visitVarInsn( ALOAD, 0 );
            mv.visitFieldInsn( GETFIELD, "org/apache/polygene/satisfiedBy/SomeMixin_Stub", "_instance",
                               "Lorg/apache/polygene/spi/composite/CompositeInvoker;" );
            mv.visitFieldInsn( GETSTATIC, "org/apache/polygene/satisfiedBy/SomeMixin_Stub", "m4", "Ljava/lang/reflect/Method;" );
            mv.visitInsn( ICONST_1 );
            mv.visitTypeInsn( ANEWARRAY, "java/lang/Object" );
            mv.visitInsn( DUP );
            mv.visitInsn( ICONST_0 );
            mv.visitVarInsn( ALOAD, 1 );
            mv.visitInsn( AASTORE );
            mv.visitMethodInsn( INVOKEINTERFACE, "org/apache/polygene/spi/composite/CompositeInvoker", "invokeComposite",
                                "(Ljava/lang/reflect/Method;[Ljava/lang/Object;)Ljava/lang/Object;", true );
            mv.visitInsn( POP );
            mv.visitLabel( l1 );
            Label l5 = new Label();
            mv.visitJumpInsn( GOTO, l5 );
            mv.visitLabel( l2 );
            mv.visitFrame( Opcodes.F_SAME1, 0, null, 1, new Object[]{ "org/apache/polygene/satisfiedBy/Exception1" } );
            mv.visitVarInsn( ASTORE, 2 );
            mv.visitVarInsn( ALOAD, 2 );
            mv.visitInsn( ATHROW );
            mv.visitLabel( l3 );
            mv.visitFrame( Opcodes.F_SAME1, 0, null, 1, new Object[]{ "org/apache/polygene/satisfiedBy/Exception2" } );
            mv.visitVarInsn( ASTORE, 2 );
            mv.visitVarInsn( ALOAD, 2 );
            mv.visitInsn( ATHROW );
            mv.visitLabel( l4 );
            mv.visitFrame( Opcodes.F_SAME1, 0, null, 1, new Object[]{ "java/lang/Throwable" } );
            mv.visitVarInsn( ASTORE, 2 );
            mv.visitTypeInsn( NEW, "java/lang/reflect/UndeclaredThrowableException" );
            mv.visitInsn( DUP );
            mv.visitVarInsn( ALOAD, 2 );
            mv.visitMethodInsn( INVOKESPECIAL, "java/lang/reflect/UndeclaredThrowableException", "<init>",
                                "(Ljava/lang/Throwable;)V", false );
            mv.visitInsn( ATHROW );
            mv.visitLabel( l5 );
            mv.visitFrame( Opcodes.F_SAME, 0, null, 0, null );
            mv.visitInsn( RETURN );
            mv.visitMaxs( 6, 3 );
            mv.visitEnd();
        }
        {
            mv = cw.visitMethod( ACC_PUBLIC, "unwrapResult", "()I", null, null );
            mv.visitCode();
            Label l0 = new Label();
            Label l1 = new Label();
            Label l2 = new Label();
            mv.visitTryCatchBlock( l0, l1, l2, "java/lang/Throwable" );
            mv.visitLabel( l0 );
            mv.visitVarInsn( ALOAD, 0 );
            mv.visitFieldInsn( GETFIELD, "org/apache/polygene/satisfiedBy/SomeMixin_Stub", "_instance",
                               "Lorg/apache/polygene/spi/composite/CompositeInvoker;" );
            mv.visitFieldInsn( GETSTATIC, "org/apache/polygene/satisfiedBy/SomeMixin_Stub", "m5", "Ljava/lang/reflect/Method;" );
            mv.visitInsn( ACONST_NULL );
            mv.visitMethodInsn( INVOKEINTERFACE, "org/apache/polygene/spi/composite/CompositeInvoker", "invokeComposite",
                                "(Ljava/lang/reflect/Method;[Ljava/lang/Object;)Ljava/lang/Object;", true );
            mv.visitTypeInsn( CHECKCAST, "java/lang/Integer" );
            mv.visitMethodInsn( INVOKEVIRTUAL, "java/lang/Integer", "intValue", "()I", false );
            mv.visitLabel( l1 );
            mv.visitInsn( IRETURN );
            mv.visitLabel( l2 );
            mv.visitFrame( Opcodes.F_SAME1, 0, null, 1, new Object[]{ "java/lang/Throwable" } );
            mv.visitVarInsn( ASTORE, 1 );
            mv.visitTypeInsn( NEW, "java/lang/reflect/UndeclaredThrowableException" );
            mv.visitInsn( DUP );
            mv.visitVarInsn( ALOAD, 1 );
            mv.visitMethodInsn( INVOKESPECIAL, "java/lang/reflect/UndeclaredThrowableException", "<init>",
                                "(Ljava/lang/Throwable;)V", false );
            mv.visitInsn( ATHROW );
            mv.visitMaxs( 3, 2 );
            mv.visitEnd();
        }
        {
            mv = cw.visitMethod( ACC_STATIC, "<clinit>", "()V", null, null );
            mv.visitCode();
            Label l0 = new Label();
            Label l1 = new Label();
            Label l2 = new Label();
            mv.visitTryCatchBlock( l0, l1, l2, "java/lang/NoSuchMethodException" );
            mv.visitLabel( l0 );
            mv.visitLdcInsn( Type.getType( "Lorg/apache/polygene/satisfiedBy/Other;" ) );
            mv.visitLdcInsn( "other" );
            mv.visitInsn( ICONST_0 );
            mv.visitTypeInsn( ANEWARRAY, "java/lang/Class" );
            mv.visitMethodInsn( INVOKEVIRTUAL, "java/lang/Class", "getMethod",
                                "(Ljava/lang/String;[Ljava/lang/Class;)Ljava/lang/reflect/Method;", false );
            mv.visitFieldInsn( PUTSTATIC, "org/apache/polygene/satisfiedBy/SomeMixin_Stub", "m1", "Ljava/lang/reflect/Method;" );
            mv.visitLdcInsn( Type.getType( "Lorg/apache/polygene/satisfiedBy/Other;" ) );
            mv.visitLdcInsn( "foo" );
            mv.visitInsn( ICONST_2 );
            mv.visitTypeInsn( ANEWARRAY, "java/lang/Class" );
            mv.visitInsn( DUP );
            mv.visitInsn( ICONST_0 );
            mv.visitLdcInsn( Type.getType( "Ljava/lang/String;" ) );
            mv.visitInsn( AASTORE );
            mv.visitInsn( DUP );
            mv.visitInsn( ICONST_1 );
            mv.visitFieldInsn( GETSTATIC, "java/lang/Integer", "TYPE", "Ljava/lang/Class;" );
            mv.visitInsn( AASTORE );
            mv.visitMethodInsn( INVOKEVIRTUAL, "java/lang/Class", "getMethod",
                                "(Ljava/lang/String;[Ljava/lang/Class;)Ljava/lang/reflect/Method;", false );
            mv.visitFieldInsn( PUTSTATIC, "org/apache/polygene/satisfiedBy/SomeMixin_Stub", "m2", "Ljava/lang/reflect/Method;" );
            mv.visitLdcInsn( Type.getType( "Lorg/apache/polygene/satisfiedBy/Other;" ) );
            mv.visitLdcInsn( "bar" );
            mv.visitIntInsn( BIPUSH, 11 );
            mv.visitTypeInsn( ANEWARRAY, "java/lang/Class" );
            mv.visitInsn( DUP );
            mv.visitInsn( ICONST_0 );
            mv.visitFieldInsn( GETSTATIC, "java/lang/Double", "TYPE", "Ljava/lang/Class;" );
            mv.visitInsn( AASTORE );
            mv.visitInsn( DUP );
            mv.visitInsn( ICONST_1 );
            mv.visitFieldInsn( GETSTATIC, "java/lang/Boolean", "TYPE", "Ljava/lang/Class;" );
            mv.visitInsn( AASTORE );
            mv.visitInsn( DUP );
            mv.visitInsn( ICONST_2 );
            mv.visitFieldInsn( GETSTATIC, "java/lang/Float", "TYPE", "Ljava/lang/Class;" );
            mv.visitInsn( AASTORE );
            mv.visitInsn( DUP );
            mv.visitInsn( ICONST_3 );
            mv.visitFieldInsn( GETSTATIC, "java/lang/Character", "TYPE", "Ljava/lang/Class;" );
            mv.visitInsn( AASTORE );
            mv.visitInsn( DUP );
            mv.visitInsn( ICONST_4 );
            mv.visitFieldInsn( GETSTATIC, "java/lang/Integer", "TYPE", "Ljava/lang/Class;" );
            mv.visitInsn( AASTORE );
            mv.visitInsn( DUP );
            mv.visitInsn( ICONST_5 );
            mv.visitFieldInsn( GETSTATIC, "java/lang/Long", "TYPE", "Ljava/lang/Class;" );
            mv.visitInsn( AASTORE );
            mv.visitInsn( DUP );
            mv.visitIntInsn( BIPUSH, 6 );
            mv.visitFieldInsn( GETSTATIC, "java/lang/Short", "TYPE", "Ljava/lang/Class;" );
            mv.visitInsn( AASTORE );
            mv.visitInsn( DUP );
            mv.visitIntInsn( BIPUSH, 7 );
            mv.visitFieldInsn( GETSTATIC, "java/lang/Byte", "TYPE", "Ljava/lang/Class;" );
            mv.visitInsn( AASTORE );
            mv.visitInsn( DUP );
            mv.visitIntInsn( BIPUSH, 8 );
            mv.visitLdcInsn( Type.getType( "Ljava/lang/Double;" ) );
            mv.visitInsn( AASTORE );
            mv.visitInsn( DUP );
            mv.visitIntInsn( BIPUSH, 9 );
            mv.visitLdcInsn( Type.getType( "[Ljava/lang/Object;" ) );
            mv.visitInsn( AASTORE );
            mv.visitInsn( DUP );
            mv.visitIntInsn( BIPUSH, 10 );
            mv.visitLdcInsn( Type.getType( "[I" ) );
            mv.visitInsn( AASTORE );
            mv.visitMethodInsn( INVOKEVIRTUAL, "java/lang/Class", "getMethod",
                                "(Ljava/lang/String;[Ljava/lang/Class;)Ljava/lang/reflect/Method;", false );
            mv.visitFieldInsn( PUTSTATIC, "org/apache/polygene/satisfiedBy/SomeMixin_Stub", "m3", "Ljava/lang/reflect/Method;" );
            mv.visitLdcInsn( Type.getType( "Lorg/apache/polygene/satisfiedBy/Other;" ) );
            mv.visitLdcInsn( "multiEx" );
            mv.visitInsn( ICONST_1 );
            mv.visitTypeInsn( ANEWARRAY, "java/lang/Class" );
            mv.visitInsn( DUP );
            mv.visitInsn( ICONST_0 );
            mv.visitLdcInsn( Type.getType( "Ljava/lang/String;" ) );
            mv.visitInsn( AASTORE );
            mv.visitMethodInsn( INVOKEVIRTUAL, "java/lang/Class", "getMethod",
                                "(Ljava/lang/String;[Ljava/lang/Class;)Ljava/lang/reflect/Method;", false );
            mv.visitFieldInsn( PUTSTATIC, "org/apache/polygene/satisfiedBy/SomeMixin_Stub", "m4", "Ljava/lang/reflect/Method;" );
            mv.visitLdcInsn( Type.getType( "Lorg/apache/polygene/satisfiedBy/Other;" ) );
            mv.visitLdcInsn( "unwrapResult" );
            mv.visitInsn( ICONST_0 );
            mv.visitTypeInsn( ANEWARRAY, "java/lang/Class" );
            mv.visitMethodInsn( INVOKEVIRTUAL, "java/lang/Class", "getMethod",
                                "(Ljava/lang/String;[Ljava/lang/Class;)Ljava/lang/reflect/Method;", false );
            mv.visitFieldInsn( PUTSTATIC, "org/apache/polygene/satisfiedBy/SomeMixin_Stub", "m5", "Ljava/lang/reflect/Method;" );
            mv.visitLabel( l1 );
            Label l3 = new Label();
            mv.visitJumpInsn( GOTO, l3 );
            mv.visitLabel( l2 );
            mv.visitFrame( Opcodes.F_SAME1, 0, null, 1, new Object[]{ "java/lang/NoSuchMethodException" } );
            mv.visitVarInsn( ASTORE, 0 );
            mv.visitVarInsn( ALOAD, 0 );
            mv.visitMethodInsn( INVOKEVIRTUAL, "java/lang/NoSuchMethodException", "printStackTrace", "()V", false );
            mv.visitLabel( l3 );
            mv.visitFrame( Opcodes.F_SAME, 0, null, 0, null );
            mv.visitInsn( RETURN );
            mv.visitMaxs( 6, 1 );
            mv.visitEnd();
        }
        cw.visitEnd();

        return cw.toByteArray();
    }
}
