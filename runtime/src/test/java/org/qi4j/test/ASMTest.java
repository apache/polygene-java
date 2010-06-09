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
import org.objectweb.asm.*;
import org.objectweb.asm.util.ASMifierClassVisitor;
import org.qi4j.runtime.composite.FragmentClassLoader;
import org.qi4j.spi.composite.CompositeInvoker;

import java.io.PrintWriter;
import java.lang.reflect.Method;

import static org.objectweb.asm.Opcodes.*;

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
        byte[] asm = generateClass();

        byte[] cl = FragmentClassLoader.generateClass( SomeMixin.class.getName() + "_Stub", SomeMixin.class );

        ClassReader cr = new ClassReader( cl );
        cr.accept( new ASMifierClassVisitor( new PrintWriter( System.out, true ) ),
                ASMifierClassVisitor.getDefaultAttributes(),
                0 );


        //       Assert.assertArrayEquals( asm, cl );

        FragmentClassLoader classLoader = new FragmentClassLoader( SomeMixin.class.getClassLoader() );

        Class clazz = classLoader.loadClass( SomeMixin.class.getName() + "_Stub" );
        clazz = classLoader.loadClass( SomeMixin.class.getName() + "_Stub" );

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

            public long unwrapResult()
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

    // This is the code generated from the manual stub

    public static byte[] generateClass()
    {
        ClassWriter cw = new ClassWriter( 0 );
        FieldVisitor fv;
        MethodVisitor mv;
        AnnotationVisitor av0;

        cw.visit( V1_6, ACC_PUBLIC + ACC_SUPER, "org/qi4j/test/SomeMixin_Stub", null, "org/qi4j/test/SomeMixin", null );

        {
            fv = cw.visitField( ACC_PUBLIC, "_instance", "Lorg/qi4j/spi/composite/CompositeInvoker;", null, null );
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
            mv.visitMethodInsn( INVOKESPECIAL, "org/qi4j/test/SomeMixin", "<init>", "()V" );
            mv.visitInsn( RETURN );
            mv.visitMaxs( 1, 1 );
            mv.visitEnd();
        }
        {
            mv = cw.visitMethod( ACC_PUBLIC, "<init>", "(Ljava/lang/String;)V", null, null );
            mv.visitCode();
            mv.visitVarInsn( ALOAD, 0 );
            mv.visitVarInsn( ALOAD, 1 );
            mv.visitMethodInsn( INVOKESPECIAL, "org/qi4j/test/SomeMixin", "<init>", "(Ljava/lang/String;)V" );
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
            mv.visitFieldInsn( GETFIELD, "org/qi4j/test/SomeMixin_Stub", "_instance", "Lorg/qi4j/spi/composite/CompositeInvoker;" );
            mv.visitFieldInsn( GETSTATIC, "org/qi4j/test/SomeMixin_Stub", "m1", "Ljava/lang/reflect/Method;" );
            mv.visitInsn( ACONST_NULL );
            mv.visitMethodInsn( INVOKEINTERFACE, "org/qi4j/spi/composite/CompositeInvoker", "invokeComposite", "(Ljava/lang/reflect/Method;[Ljava/lang/Object;)Ljava/lang/Object;" );
            mv.visitTypeInsn( CHECKCAST, "java/lang/String" );
            mv.visitLabel( l1 );
            mv.visitInsn( ARETURN );
            mv.visitLabel( l2 );
            mv.visitFrame( Opcodes.F_SAME1, 0, null, 1, new Object[]{"java/lang/Throwable"} );
            mv.visitVarInsn( ASTORE, 1 );
            mv.visitTypeInsn( NEW, "java/lang/reflect/UndeclaredThrowableException" );
            mv.visitInsn( DUP );
            mv.visitVarInsn( ALOAD, 1 );
            mv.visitMethodInsn( INVOKESPECIAL, "java/lang/reflect/UndeclaredThrowableException", "<init>", "(Ljava/lang/Throwable;)V" );
            mv.visitInsn( ATHROW );
            mv.visitMaxs( 3, 2 );
            mv.visitEnd();
        }
        {
            mv = cw.visitMethod( ACC_PUBLIC, "foo", "(Ljava/lang/String;I)Ljava/lang/String;", null, new String[]{"java/lang/IllegalArgumentException"} );
            mv.visitCode();
            Label l0 = new Label();
            Label l1 = new Label();
            Label l2 = new Label();
            mv.visitTryCatchBlock( l0, l1, l2, "java/lang/IllegalArgumentException" );
            Label l3 = new Label();
            mv.visitTryCatchBlock( l0, l1, l3, "java/lang/Throwable" );
            mv.visitLabel( l0 );
            mv.visitVarInsn( ALOAD, 0 );
            mv.visitFieldInsn( GETFIELD, "org/qi4j/test/SomeMixin_Stub", "_instance", "Lorg/qi4j/spi/composite/CompositeInvoker;" );
            mv.visitFieldInsn( GETSTATIC, "org/qi4j/test/SomeMixin_Stub", "m2", "Ljava/lang/reflect/Method;" );
            mv.visitInsn( ICONST_2 );
            mv.visitTypeInsn( ANEWARRAY, "java/lang/Object" );
            mv.visitInsn( DUP );
            mv.visitInsn( ICONST_0 );
            mv.visitVarInsn( ALOAD, 1 );
            mv.visitInsn( AASTORE );
            mv.visitInsn( DUP );
            mv.visitInsn( ICONST_1 );
            mv.visitVarInsn( ILOAD, 2 );
            mv.visitMethodInsn( INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;" );
            mv.visitInsn( AASTORE );
            mv.visitMethodInsn( INVOKEINTERFACE, "org/qi4j/spi/composite/CompositeInvoker", "invokeComposite", "(Ljava/lang/reflect/Method;[Ljava/lang/Object;)Ljava/lang/Object;" );
            mv.visitTypeInsn( CHECKCAST, "java/lang/String" );
            mv.visitLabel( l1 );
            mv.visitInsn( ARETURN );
            mv.visitLabel( l2 );
            mv.visitFrame( Opcodes.F_SAME1, 0, null, 1, new Object[]{"java/lang/IllegalArgumentException"} );
            mv.visitVarInsn( ASTORE, 3 );
            mv.visitVarInsn( ALOAD, 3 );
            mv.visitInsn( ATHROW );
            mv.visitLabel( l3 );
            mv.visitFrame( Opcodes.F_SAME1, 0, null, 1, new Object[]{"java/lang/Throwable"} );
            mv.visitVarInsn( ASTORE, 3 );
            mv.visitTypeInsn( NEW, "java/lang/reflect/UndeclaredThrowableException" );
            mv.visitInsn( DUP );
            mv.visitVarInsn( ALOAD, 3 );
            mv.visitMethodInsn( INVOKESPECIAL, "java/lang/reflect/UndeclaredThrowableException", "<init>", "(Ljava/lang/Throwable;)V" );
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
            mv.visitFieldInsn( GETFIELD, "org/qi4j/test/SomeMixin_Stub", "_instance", "Lorg/qi4j/spi/composite/CompositeInvoker;" );
            mv.visitFieldInsn( GETSTATIC, "org/qi4j/test/SomeMixin_Stub", "m3", "Ljava/lang/reflect/Method;" );
            mv.visitIntInsn( BIPUSH, 11 );
            mv.visitTypeInsn( ANEWARRAY, "java/lang/Object" );
            mv.visitInsn( DUP );
            mv.visitInsn( ICONST_0 );
            mv.visitVarInsn( DLOAD, 1 );
            mv.visitMethodInsn( INVOKESTATIC, "java/lang/Double", "valueOf", "(D)Ljava/lang/Double;" );
            mv.visitInsn( AASTORE );
            mv.visitInsn( DUP );
            mv.visitInsn( ICONST_1 );
            mv.visitVarInsn( ILOAD, 3 );
            mv.visitMethodInsn( INVOKESTATIC, "java/lang/Boolean", "valueOf", "(Z)Ljava/lang/Boolean;" );
            mv.visitInsn( AASTORE );
            mv.visitInsn( DUP );
            mv.visitInsn( ICONST_2 );
            mv.visitVarInsn( FLOAD, 4 );
            mv.visitMethodInsn( INVOKESTATIC, "java/lang/Float", "valueOf", "(F)Ljava/lang/Float;" );
            mv.visitInsn( AASTORE );
            mv.visitInsn( DUP );
            mv.visitInsn( ICONST_3 );
            mv.visitVarInsn( ILOAD, 5 );
            mv.visitMethodInsn( INVOKESTATIC, "java/lang/Character", "valueOf", "(C)Ljava/lang/Character;" );
            mv.visitInsn( AASTORE );
            mv.visitInsn( DUP );
            mv.visitInsn( ICONST_4 );
            mv.visitVarInsn( ILOAD, 6 );
            mv.visitMethodInsn( INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;" );
            mv.visitInsn( AASTORE );
            mv.visitInsn( DUP );
            mv.visitInsn( ICONST_5 );
            mv.visitVarInsn( LLOAD, 7 );
            mv.visitMethodInsn( INVOKESTATIC, "java/lang/Long", "valueOf", "(J)Ljava/lang/Long;" );
            mv.visitInsn( AASTORE );
            mv.visitInsn( DUP );
            mv.visitIntInsn( BIPUSH, 6 );
            mv.visitVarInsn( ILOAD, 9 );
            mv.visitMethodInsn( INVOKESTATIC, "java/lang/Short", "valueOf", "(S)Ljava/lang/Short;" );
            mv.visitInsn( AASTORE );
            mv.visitInsn( DUP );
            mv.visitIntInsn( BIPUSH, 7 );
            mv.visitVarInsn( ILOAD, 10 );
            mv.visitMethodInsn( INVOKESTATIC, "java/lang/Byte", "valueOf", "(B)Ljava/lang/Byte;" );
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
            mv.visitMethodInsn( INVOKEINTERFACE, "org/qi4j/spi/composite/CompositeInvoker", "invokeComposite", "(Ljava/lang/reflect/Method;[Ljava/lang/Object;)Ljava/lang/Object;" );
            mv.visitInsn( POP );
            mv.visitLabel( l1 );
            Label l3 = new Label();
            mv.visitJumpInsn( GOTO, l3 );
            mv.visitLabel( l2 );
            mv.visitFrame( Opcodes.F_SAME1, 0, null, 1, new Object[]{"java/lang/Throwable"} );
            mv.visitVarInsn( ASTORE, 14 );
            mv.visitTypeInsn( NEW, "java/lang/reflect/UndeclaredThrowableException" );
            mv.visitInsn( DUP );
            mv.visitVarInsn( ALOAD, 14 );
            mv.visitMethodInsn( INVOKESPECIAL, "java/lang/reflect/UndeclaredThrowableException", "<init>", "(Ljava/lang/Throwable;)V" );
            mv.visitInsn( ATHROW );
            mv.visitLabel( l3 );
            mv.visitFrame( Opcodes.F_SAME, 0, null, 0, null );
            mv.visitInsn( RETURN );
            mv.visitMaxs( 7, 15 );
            mv.visitEnd();
        }
        {
            mv = cw.visitMethod( ACC_PUBLIC, "multiEx", "(Ljava/lang/String;)V", null, new String[]{"org/qi4j/test/Exception1", "org/qi4j/test/Exception2"} );
            mv.visitCode();
            Label l0 = new Label();
            Label l1 = new Label();
            Label l2 = new Label();
            mv.visitTryCatchBlock( l0, l1, l2, "org/qi4j/test/Exception1" );
            Label l3 = new Label();
            mv.visitTryCatchBlock( l0, l1, l3, "org/qi4j/test/Exception2" );
            Label l4 = new Label();
            mv.visitTryCatchBlock( l0, l1, l4, "java/lang/Throwable" );
            mv.visitLabel( l0 );
            mv.visitVarInsn( ALOAD, 0 );
            mv.visitFieldInsn( GETFIELD, "org/qi4j/test/SomeMixin_Stub", "_instance", "Lorg/qi4j/spi/composite/CompositeInvoker;" );
            mv.visitFieldInsn( GETSTATIC, "org/qi4j/test/SomeMixin_Stub", "m4", "Ljava/lang/reflect/Method;" );
            mv.visitInsn( ICONST_1 );
            mv.visitTypeInsn( ANEWARRAY, "java/lang/Object" );
            mv.visitInsn( DUP );
            mv.visitInsn( ICONST_0 );
            mv.visitVarInsn( ALOAD, 1 );
            mv.visitInsn( AASTORE );
            mv.visitMethodInsn( INVOKEINTERFACE, "org/qi4j/spi/composite/CompositeInvoker", "invokeComposite", "(Ljava/lang/reflect/Method;[Ljava/lang/Object;)Ljava/lang/Object;" );
            mv.visitInsn( POP );
            mv.visitLabel( l1 );
            Label l5 = new Label();
            mv.visitJumpInsn( GOTO, l5 );
            mv.visitLabel( l2 );
            mv.visitFrame( Opcodes.F_SAME1, 0, null, 1, new Object[]{"org/qi4j/test/Exception1"} );
            mv.visitVarInsn( ASTORE, 2 );
            mv.visitVarInsn( ALOAD, 2 );
            mv.visitInsn( ATHROW );
            mv.visitLabel( l3 );
            mv.visitFrame( Opcodes.F_SAME1, 0, null, 1, new Object[]{"org/qi4j/test/Exception2"} );
            mv.visitVarInsn( ASTORE, 2 );
            mv.visitVarInsn( ALOAD, 2 );
            mv.visitInsn( ATHROW );
            mv.visitLabel( l4 );
            mv.visitFrame( Opcodes.F_SAME1, 0, null, 1, new Object[]{"java/lang/Throwable"} );
            mv.visitVarInsn( ASTORE, 2 );
            mv.visitTypeInsn( NEW, "java/lang/reflect/UndeclaredThrowableException" );
            mv.visitInsn( DUP );
            mv.visitVarInsn( ALOAD, 2 );
            mv.visitMethodInsn( INVOKESPECIAL, "java/lang/reflect/UndeclaredThrowableException", "<init>", "(Ljava/lang/Throwable;)V" );
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
            mv.visitFieldInsn( GETFIELD, "org/qi4j/test/SomeMixin_Stub", "_instance", "Lorg/qi4j/spi/composite/CompositeInvoker;" );
            mv.visitFieldInsn( GETSTATIC, "org/qi4j/test/SomeMixin_Stub", "m5", "Ljava/lang/reflect/Method;" );
            mv.visitInsn( ACONST_NULL );
            mv.visitMethodInsn( INVOKEINTERFACE, "org/qi4j/spi/composite/CompositeInvoker", "invokeComposite", "(Ljava/lang/reflect/Method;[Ljava/lang/Object;)Ljava/lang/Object;" );
            mv.visitTypeInsn( CHECKCAST, "java/lang/Integer" );
            mv.visitMethodInsn( INVOKEVIRTUAL, "java/lang/Integer", "intValue", "()I" );
            mv.visitLabel( l1 );
            mv.visitInsn( IRETURN );
            mv.visitLabel( l2 );
            mv.visitFrame( Opcodes.F_SAME1, 0, null, 1, new Object[]{"java/lang/Throwable"} );
            mv.visitVarInsn( ASTORE, 1 );
            mv.visitTypeInsn( NEW, "java/lang/reflect/UndeclaredThrowableException" );
            mv.visitInsn( DUP );
            mv.visitVarInsn( ALOAD, 1 );
            mv.visitMethodInsn( INVOKESPECIAL, "java/lang/reflect/UndeclaredThrowableException", "<init>", "(Ljava/lang/Throwable;)V" );
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
            mv.visitLdcInsn( Type.getType( "Lorg/qi4j/test/Other;" ) );
            mv.visitLdcInsn( "other" );
            mv.visitInsn( ICONST_0 );
            mv.visitTypeInsn( ANEWARRAY, "java/lang/Class" );
            mv.visitMethodInsn( INVOKEVIRTUAL, "java/lang/Class", "getMethod", "(Ljava/lang/String;[Ljava/lang/Class;)Ljava/lang/reflect/Method;" );
            mv.visitFieldInsn( PUTSTATIC, "org/qi4j/test/SomeMixin_Stub", "m1", "Ljava/lang/reflect/Method;" );
            mv.visitLdcInsn( Type.getType( "Lorg/qi4j/test/Other;" ) );
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
            mv.visitMethodInsn( INVOKEVIRTUAL, "java/lang/Class", "getMethod", "(Ljava/lang/String;[Ljava/lang/Class;)Ljava/lang/reflect/Method;" );
            mv.visitFieldInsn( PUTSTATIC, "org/qi4j/test/SomeMixin_Stub", "m2", "Ljava/lang/reflect/Method;" );
            mv.visitLdcInsn( Type.getType( "Lorg/qi4j/test/Other;" ) );
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
            mv.visitMethodInsn( INVOKEVIRTUAL, "java/lang/Class", "getMethod", "(Ljava/lang/String;[Ljava/lang/Class;)Ljava/lang/reflect/Method;" );
            mv.visitFieldInsn( PUTSTATIC, "org/qi4j/test/SomeMixin_Stub", "m3", "Ljava/lang/reflect/Method;" );
            mv.visitLdcInsn( Type.getType( "Lorg/qi4j/test/Other;" ) );
            mv.visitLdcInsn( "multiEx" );
            mv.visitInsn( ICONST_1 );
            mv.visitTypeInsn( ANEWARRAY, "java/lang/Class" );
            mv.visitInsn( DUP );
            mv.visitInsn( ICONST_0 );
            mv.visitLdcInsn( Type.getType( "Ljava/lang/String;" ) );
            mv.visitInsn( AASTORE );
            mv.visitMethodInsn( INVOKEVIRTUAL, "java/lang/Class", "getMethod", "(Ljava/lang/String;[Ljava/lang/Class;)Ljava/lang/reflect/Method;" );
            mv.visitFieldInsn( PUTSTATIC, "org/qi4j/test/SomeMixin_Stub", "m4", "Ljava/lang/reflect/Method;" );
            mv.visitLdcInsn( Type.getType( "Lorg/qi4j/test/Other;" ) );
            mv.visitLdcInsn( "unwrapResult" );
            mv.visitInsn( ICONST_0 );
            mv.visitTypeInsn( ANEWARRAY, "java/lang/Class" );
            mv.visitMethodInsn( INVOKEVIRTUAL, "java/lang/Class", "getMethod", "(Ljava/lang/String;[Ljava/lang/Class;)Ljava/lang/reflect/Method;" );
            mv.visitFieldInsn( PUTSTATIC, "org/qi4j/test/SomeMixin_Stub", "m5", "Ljava/lang/reflect/Method;" );
            mv.visitLabel( l1 );
            Label l3 = new Label();
            mv.visitJumpInsn( GOTO, l3 );
            mv.visitLabel( l2 );
            mv.visitFrame( Opcodes.F_SAME1, 0, null, 1, new Object[]{"java/lang/NoSuchMethodException"} );
            mv.visitVarInsn( ASTORE, 0 );
            mv.visitVarInsn( ALOAD, 0 );
            mv.visitMethodInsn( INVOKEVIRTUAL, "java/lang/NoSuchMethodException", "printStackTrace", "()V" );
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
