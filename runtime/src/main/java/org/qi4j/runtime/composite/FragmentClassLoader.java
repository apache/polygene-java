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

package org.qi4j.runtime.composite;

import org.objectweb.asm.*;
import org.qi4j.api.entity.Lifecycle;
import org.qi4j.api.mixin.Initializable;
import org.qi4j.api.service.Activatable;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.objectweb.asm.Opcodes.*;
import static org.objectweb.asm.Type.getInternalName;
import static org.qi4j.api.util.Classes.interfacesOf;
import static org.qi4j.api.util.Classes.interfacesWithMethods;

/**
 * Generate subclasses of mixins/modifiers that implement all interfaces not in the class itself
 * and which delegates those calls to a given composite invoker.
 */
public class FragmentClassLoader
        extends ClassLoader
{
    private static int jdkVersion = Opcodes.V1_5;

    static
    {
        String jdkString = System.getProperty( "java.specification.version" );

        if( jdkString.equals( "1.6" ) )
            jdkVersion = Opcodes.V1_6;
        else if( jdkString.equals( "1.7" ) )
            jdkVersion = Opcodes.V1_7;
    }

    public FragmentClassLoader( ClassLoader parent )
    {
        super( parent );
    }

    protected Class findClass( String name )
            throws ClassNotFoundException
    {
        if( name.endsWith( "_Stub" ) )
        {
            Class baseClass = null;
            String baseName = name.substring( 0, name.length() - 5 );
            try
            {
                baseClass = loadClass( baseName );
            } catch (ClassNotFoundException e)
            {
                // Try replacing the last _ with $
                while (true)
                {
                    int idx = baseName.lastIndexOf( "_" );
                    if( idx != -1 )
                    {
                        baseName = baseName.substring( 0, idx ) + "$" + baseName.substring( idx + 1 );
                        try
                        {
                            baseClass = loadClass( baseName );
                            break;
                        } catch (ClassNotFoundException e1)
                        {
                            // Try again
                        }
                    } else
                        throw e;
                }
            }

            byte[] b = generateClass( name, baseClass );
            return defineClass( name, b, 0, b.length, baseClass.getProtectionDomain() );
        }

        // Try the classloader of this classloader -> get classes in Qi4j such as CompositeInvoker
        return getClass().getClassLoader().loadClass( name );
    }

    public static byte[] generateClass( String name, Class baseClass ) throws ClassNotFoundException
    {
        String classSlash = name.replace( '.', '/' );
        String baseClassSlash = getInternalName( baseClass );

        ClassWriter cw = new ClassWriter( ClassWriter.COMPUTE_MAXS );
        FieldVisitor fv;
        MethodVisitor mv;
        AnnotationVisitor av0;

        // Class definition start
        cw.visit( jdkVersion, ACC_PUBLIC + ACC_SUPER, classSlash, null, baseClassSlash, null );

        // Composite reference
        {
            fv = cw.visitField( ACC_PUBLIC, "_instance", "Lorg/qi4j/spi/composite/CompositeInvoker;", null, null );
            fv.visitEnd();
        }

        // Static Method references
        boolean hasProxyMethods = false;
        {
            int idx = 1;
            for (Method method : baseClass.getMethods())
            {
                if( isOverloaded( method, baseClass ) )
                {
                    fv = cw.visitField( ACC_PRIVATE + ACC_STATIC, "m" + idx++, "Ljava/lang/reflect/Method;", null, null );
                    fv.visitEnd();
                    hasProxyMethods = true;
                }
            }
        }

        // Constructors
        for (Constructor constructor : baseClass.getDeclaredConstructors())
        {
            if( Modifier.isPublic( constructor.getModifiers() ) || Modifier.isProtected( constructor.getModifiers() ) )
            {
                String desc = org.objectweb.asm.commons.Method.getMethod( constructor ).getDescriptor();
                mv = cw.visitMethod( ACC_PUBLIC, "<init>", desc, null, null );
                mv.visitCode();
                mv.visitVarInsn( ALOAD, 0 );

                int idx = 1;
                for (Class aClass : constructor.getParameterTypes())
                {
                    // TODO Handle other types than objects (?)
                    mv.visitVarInsn( ALOAD, idx++ );
                }

                mv.visitMethodInsn( INVOKESPECIAL, baseClassSlash, "<init>", desc );
                mv.visitInsn( RETURN );
                mv.visitMaxs( idx, idx );
                mv.visitEnd();
            }
        }

        // Overloaded and unimplemented methods
        if( hasProxyMethods )
        {
            Method[] methods = baseClass.getMethods();
            int idx = 0;
            List<Label> exceptionLabels = new ArrayList<Label>();
            for (Method method : methods)
            {
                if( isOverloaded( method, baseClass ) )
                {
                    idx++;
                    String methodName = method.getName();
                    String desc = org.objectweb.asm.commons.Method.getMethod( method ).getDescriptor();

                    String[] exceptions = null;
                    {
                        Label endLabel = null; // Use this if return type is void

                        if( method.getExceptionTypes().length > 0 )
                        {
                            exceptions = new String[method.getExceptionTypes().length];
                            for (int i = 0; i < method.getExceptionTypes().length; i++)
                            {
                                Class<?> aClass = method.getExceptionTypes()[i];
                                exceptions[i] = getInternalName( aClass );
                            }
                        }

                        mv = cw.visitMethod( ACC_PUBLIC, methodName, desc, null, exceptions );
                        mv.visitCode();
                        Label l0 = new Label();
                        Label l1 = new Label();

                        exceptionLabels.clear();
                        for (Class<?> declaredException : method.getExceptionTypes())
                        {
                            Label ld = new Label();
                            mv.visitTryCatchBlock( l0, l1, ld, getInternalName( declaredException ) );
                            exceptionLabels.add( ld ); // Reuse this further down for the catch
                        }

                        Label lruntime = new Label();
                        mv.visitTryCatchBlock( l0, l1, lruntime, "java/lang/RuntimeException" );
                        Label lerror = new Label();
                        mv.visitTryCatchBlock( l0, l1, lerror, "java/lang/Throwable" );

                        mv.visitLabel( l0 );
                        mv.visitVarInsn( ALOAD, 0 );
                        mv.visitFieldInsn( GETFIELD, classSlash, "_instance", "Lorg/qi4j/spi/composite/CompositeInvoker;" );
                        mv.visitFieldInsn( GETSTATIC, classSlash, "m" + idx, "Ljava/lang/reflect/Method;" );

                        int paramCount = method.getParameterTypes().length;
                        int stackIdx = 0;
                        if( paramCount == 0 )
                        {
                            // Send in null as parameter
                            mv.visitInsn( ACONST_NULL );
                        } else
                        {
                            insn( mv, paramCount );
                            mv.visitTypeInsn( ANEWARRAY, "java/lang/Object" );
                            int pidx = 0;
                            for (Class<?> aClass : method.getParameterTypes())
                            {
                                mv.visitInsn( DUP );
                                insn( mv, pidx++ );
                                stackIdx = wrapParameter( mv, aClass, stackIdx + 1 );
                                mv.visitInsn( AASTORE );
                            }
                        }

                        // Call method
                        mv.visitMethodInsn( INVOKEINTERFACE, "org/qi4j/spi/composite/CompositeInvoker", "invokeComposite", "(Ljava/lang/reflect/Method;[Ljava/lang/Object;)Ljava/lang/Object;" );

                        // Return value
                        if( !method.getReturnType().equals( Void.TYPE ) )
                        {
                            unwrapResult( mv, method.getReturnType(), l1 );
                        } else
                        {
                            mv.visitInsn( POP );
                            mv.visitLabel( l1 );
                            endLabel = new Label();
                            mv.visitJumpInsn( GOTO, endLabel );
                        }

                        // Increase stack to beyond method args
                        stackIdx++;

                        // Declared exceptions
                        int exceptionIdx = 0;
                        for (Class<?> aClass : method.getExceptionTypes())
                        {
                            mv.visitLabel( exceptionLabels.get( exceptionIdx++ ) );
                            mv.visitFrame( Opcodes.F_SAME1, 0, null, 1, new Object[]{getInternalName( aClass )} );
                            mv.visitVarInsn( ASTORE, stackIdx );
                            mv.visitVarInsn( ALOAD, stackIdx );
                            mv.visitInsn( ATHROW );
                        }

                        // RuntimeException and Error catch-all
                        mv.visitLabel( lruntime );
                        mv.visitFrame( Opcodes.F_SAME1, 0, null, 1, new Object[]{"java/lang/RuntimeException"} );
                        mv.visitVarInsn( ASTORE, stackIdx );
                        mv.visitVarInsn( ALOAD, stackIdx );
                        mv.visitInsn( ATHROW );

                        mv.visitLabel( lerror );
                        mv.visitFrame( Opcodes.F_SAME1, 0, null, 1, new Object[]{"java/lang/Throwable"} );
                        mv.visitVarInsn( ASTORE, stackIdx );
                        mv.visitVarInsn( ALOAD, stackIdx );
                        mv.visitTypeInsn( CHECKCAST, "java/lang/Error" );
                        mv.visitInsn( ATHROW );

                        // Return type = void
                        if( endLabel != null )
                        {
                            mv.visitLabel( endLabel );
                            mv.visitFrame( Opcodes.F_SAME, 0, null, 0, null );
                            mv.visitInsn( RETURN );
                        }

                        mv.visitMaxs( 0, 0 );
                        mv.visitEnd();
                    }

                    if( !Modifier.isAbstract( method.getModifiers() ) )
                    {
                        // Add method with _ as prefix
                        mv = cw.visitMethod( ACC_PUBLIC, "_" + method.getName(), desc, null, exceptions );
                        Label l1 = new Label();
                        mv.visitCode();
                        mv.visitVarInsn( ALOAD, 0 );

                        // Parameters
                        int stackIdx = 1;
                        for (Class<?> aClass : method.getParameterTypes())
                        {
                            stackIdx = loadParameter( mv, aClass, stackIdx ) + 1;
                        }

                        // Call method
                        mv.visitMethodInsn( INVOKESPECIAL, baseClassSlash, method.getName(), desc );

                        // Return value
                        if( !method.getReturnType().equals( Void.TYPE ) )
                        {
                            returnResult( mv, method.getReturnType(), l1 );
                        } else
                        {
                            mv.visitInsn( RETURN );
                        }

                        mv.visitMaxs( 1, 1 );
                        mv.visitEnd();
                    }
                }
            }

            // Class initializer
            {
                mv = cw.visitMethod( ACC_STATIC, "<clinit>", "()V", null, null );
                mv.visitCode();
                Label l0 = new Label();
                Label l1 = new Label();
                Label l2 = new Label();
                mv.visitTryCatchBlock( l0, l1, l2, "java/lang/NoSuchMethodException" );
                mv.visitLabel( l0 );

                // Lookup methods and store in static variables
                int midx = 0;
                for (Method method : methods)
                {
                    if( isOverloaded( method, baseClass ) )
                    {
                        method.setAccessible( true );
                        Class methodClass;
                        if( Modifier.isAbstract( method.getModifiers() ) )
                            methodClass = method.getDeclaringClass();
                        else
                        {
                            try
                            {
                                methodClass = getInterfaceMethodDeclaration( method, baseClass ); // Overridden method lookup
                            } catch (NoSuchMethodException e)
                            {
                                throw new ClassNotFoundException( name, e );
                            }
                        }

                        midx++;

                        mv.visitLdcInsn( Type.getType( methodClass ) );
                        mv.visitLdcInsn( method.getName() );
                        insn( mv, method.getParameterTypes().length );
                        mv.visitTypeInsn( ANEWARRAY, "java/lang/Class" );

                        int pidx = 0;
                        for (Class<?> aClass : method.getParameterTypes())
                        {
                            mv.visitInsn( DUP );
                            insn( mv, pidx++ );
                            type( mv, aClass );
                            mv.visitInsn( AASTORE );
                        }

                        mv.visitMethodInsn( INVOKEVIRTUAL, "java/lang/Class", "getMethod", "(Ljava/lang/String;[Ljava/lang/Class;)Ljava/lang/reflect/Method;" );
                        mv.visitFieldInsn( PUTSTATIC, classSlash, "m" + midx, "Ljava/lang/reflect/Method;" );
                    }
                }

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
        }

        cw.visitEnd();

        return cw.toByteArray();
    }

    private static boolean isOverloaded( Method method, Class baseClass )
    {
        if( Modifier.isAbstract( method.getModifiers() ) )
            return true; // Implement all abstract methods

        if( Modifier.isFinal( method.getModifiers() ) )
            return false; // Cannot override final methods

        if( isInterfaceMethod( method, baseClass ) )
        {
            if( isDeclaredIn( method, Activatable.class, baseClass ) || isDeclaredIn( method, Initializable.class, baseClass ) || isDeclaredIn( method, Lifecycle.class, baseClass ) )
                return false; // Skip methods in Qi4j-internal interfaces
            else
                return true;
        } else
        {
            return false;
        }
    }

    private static boolean isDeclaredIn( Method method, Class clazz, Class baseClass )
    {
        if( !clazz.isAssignableFrom( baseClass ) )
            return false;

        try
        {
            clazz.getMethod( method.getName(), method.getParameterTypes() );
            return true;
        } catch (NoSuchMethodException e)
        {
            return false;
        }
    }

    private static Class getInterfaceMethodDeclaration( Method method, Class clazz ) throws NoSuchMethodException
    {
        Set<Class> interfaces = interfacesOf( clazz );
        for (Class anInterface : interfaces)
        {
            try
            {
                anInterface.getMethod( method.getName(), method.getParameterTypes() );
                return anInterface;
            } catch (NoSuchMethodException e)
            {
                // Try next
            }
        }

        throw new NoSuchMethodException( method.getName() );
    }

    private static boolean isInterfaceMethod( Method method, Class baseClass )
    {
        for (Class aClass : interfacesWithMethods( interfacesOf( baseClass ) ))
        {
            try
            {
                Method m = aClass.getMethod( method.getName(), method.getParameterTypes() );
                m.setAccessible( true );
                return true;
            } catch (NoSuchMethodException e)
            {
                // Ignore
            }
        }
        return false;
    }

    private static void type( MethodVisitor mv, Class<?> aClass )
    {
        if( aClass.equals( Integer.TYPE ) )
        {
            mv.visitFieldInsn( GETSTATIC, "java/lang/Integer", "TYPE", "Ljava/lang/Class;" );
        } else if( aClass.equals( Long.TYPE ) )
        {
            mv.visitFieldInsn( GETSTATIC, "java/lang/Long", "TYPE", "Ljava/lang/Class;" );
        } else if( aClass.equals( Short.TYPE ) )
        {
            mv.visitFieldInsn( GETSTATIC, "java/lang/Short", "TYPE", "Ljava/lang/Class;" );
        } else if( aClass.equals( Byte.TYPE ) )
        {
            mv.visitFieldInsn( GETSTATIC, "java/lang/Byte", "TYPE", "Ljava/lang/Class;" );
        } else if( aClass.equals( Double.TYPE ) )
        {
            mv.visitFieldInsn( GETSTATIC, "java/lang/Double", "TYPE", "Ljava/lang/Class;" );
        } else if( aClass.equals( Float.TYPE ) )
        {
            mv.visitFieldInsn( GETSTATIC, "java/lang/Float", "TYPE", "Ljava/lang/Class;" );
        } else if( aClass.equals( Boolean.TYPE ) )
        {
            mv.visitFieldInsn( GETSTATIC, "java/lang/Boolean", "TYPE", "Ljava/lang/Class;" );
        } else if( aClass.equals( Character.TYPE ) )
        {
            mv.visitFieldInsn( GETSTATIC, "java/lang/Character", "TYPE", "Ljava/lang/Class;" );
        } else
        {
            mv.visitLdcInsn( Type.getType( aClass ) );
        }
    }

    private static int wrapParameter( MethodVisitor mv, Class<?> aClass, int idx )
    {
        if( aClass.equals( Integer.TYPE ) )
        {
            mv.visitVarInsn( ILOAD, idx );
            mv.visitMethodInsn( INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;" );
        } else if( aClass.equals( Long.TYPE ) )
        {
            mv.visitVarInsn( LLOAD, idx );
            mv.visitMethodInsn( INVOKESTATIC, "java/lang/Long", "valueOf", "(J)Ljava/lang/Long;" );
            idx++; // Extra jump
        } else if( aClass.equals( Short.TYPE ) )
        {
            mv.visitVarInsn( ILOAD, idx );
            mv.visitMethodInsn( INVOKESTATIC, "java/lang/Short", "valueOf", "(S)Ljava/lang/Short;" );
        } else if( aClass.equals( Byte.TYPE ) )
        {
            mv.visitVarInsn( ILOAD, idx );
            mv.visitMethodInsn( INVOKESTATIC, "java/lang/Byte", "valueOf", "(B)Ljava/lang/Byte;" );
        } else if( aClass.equals( Double.TYPE ) )
        {
            mv.visitVarInsn( DLOAD, idx );
            idx++; // Extra jump
            mv.visitMethodInsn( INVOKESTATIC, "java/lang/Double", "valueOf", "(D)Ljava/lang/Double;" );
        } else if( aClass.equals( Float.TYPE ) )
        {
            mv.visitVarInsn( FLOAD, idx );
            mv.visitMethodInsn( INVOKESTATIC, "java/lang/Float", "valueOf", "(F)Ljava/lang/Float;" );
        } else if( aClass.equals( Boolean.TYPE ) )
        {
            mv.visitVarInsn( ILOAD, idx );
            mv.visitMethodInsn( INVOKESTATIC, "java/lang/Boolean", "valueOf", "(Z)Ljava/lang/Boolean;" );
        } else if( aClass.equals( Character.TYPE ) )
        {
            mv.visitVarInsn( ILOAD, idx );
            mv.visitMethodInsn( INVOKESTATIC, "java/lang/Character", "valueOf", "(C)Ljava/lang/Character;" );
        } else
        {
            mv.visitVarInsn( ALOAD, idx );
        }

        return idx;
    }

    private static void unwrapResult( MethodVisitor mv, Class<?> aClass, Label label )
    {
        if( aClass.equals( Integer.TYPE ) )
        {
            mv.visitTypeInsn( CHECKCAST, "java/lang/Integer" );
            mv.visitMethodInsn( INVOKEVIRTUAL, "java/lang/Integer", "intValue", "()I" );
            mv.visitLabel( label );
            mv.visitInsn( IRETURN );
        } else if( aClass.equals( Long.TYPE ) )
        {
            mv.visitTypeInsn( CHECKCAST, "java/lang/Long" );
            mv.visitMethodInsn( INVOKEVIRTUAL, "java/lang/Long", "longValue", "()J" );
            mv.visitLabel( label );
            mv.visitInsn( LRETURN );
        } else if( aClass.equals( Short.TYPE ) )
        {
            mv.visitTypeInsn( CHECKCAST, "java/lang/Short" );
            mv.visitMethodInsn( INVOKEVIRTUAL, "java/lang/Short", "shortValue", "()S" );
            mv.visitLabel( label );
            mv.visitInsn( IRETURN );
        } else if( aClass.equals( Byte.TYPE ) )
        {
            mv.visitTypeInsn( CHECKCAST, "java/lang/Byte" );
            mv.visitMethodInsn( INVOKEVIRTUAL, "java/lang/Byte", "byteValue", "()B" );
            mv.visitLabel( label );
            mv.visitInsn( IRETURN );
        } else if( aClass.equals( Double.TYPE ) )
        {
            mv.visitTypeInsn( CHECKCAST, "java/lang/Double" );
            mv.visitMethodInsn( INVOKEVIRTUAL, "java/lang/Double", "doubleValue", "()D" );
            mv.visitLabel( label );
            mv.visitInsn( DRETURN );
        } else if( aClass.equals( Float.TYPE ) )
        {
            mv.visitTypeInsn( CHECKCAST, "java/lang/Float" );
            mv.visitMethodInsn( INVOKEVIRTUAL, "java/lang/Float", "floatValue", "()F" );
            mv.visitLabel( label );
            mv.visitInsn( FRETURN );
        } else if( aClass.equals( Boolean.TYPE ) )
        {
            mv.visitTypeInsn( CHECKCAST, "java/lang/Boolean" );
            mv.visitMethodInsn( INVOKEVIRTUAL, "java/lang/Boolean", "booleanValue", "()Z" );
            mv.visitLabel( label );
            mv.visitInsn( IRETURN );
        } else if( aClass.equals( Character.TYPE ) )
        {
            mv.visitTypeInsn( CHECKCAST, "java/lang/Character" );
            mv.visitMethodInsn( INVOKEVIRTUAL, "java/lang/Character", "charValue", "()C" );
            mv.visitLabel( label );
            mv.visitInsn( IRETURN );
        } else
        {
            mv.visitTypeInsn( CHECKCAST, getInternalName( aClass ) );
            mv.visitLabel( label );
            mv.visitInsn( ARETURN );
        }
    }

    private static int loadParameter( MethodVisitor mv, Class<?> aClass, int idx )
    {
        if( aClass.equals( Integer.TYPE ) )
        {
            mv.visitVarInsn( ILOAD, idx );
        } else if( aClass.equals( Long.TYPE ) )
        {
            mv.visitVarInsn( LLOAD, idx );
            idx++; // Extra jump
        } else if( aClass.equals( Short.TYPE ) )
        {
            mv.visitVarInsn( ILOAD, idx );
        } else if( aClass.equals( Byte.TYPE ) )
        {
            mv.visitVarInsn( ILOAD, idx );
        } else if( aClass.equals( Double.TYPE ) )
        {
            mv.visitVarInsn( DLOAD, idx );
            idx++; // Extra jump
        } else if( aClass.equals( Float.TYPE ) )
        {
            mv.visitVarInsn( FLOAD, idx );
        } else if( aClass.equals( Boolean.TYPE ) )
        {
            mv.visitVarInsn( ILOAD, idx );
        } else if( aClass.equals( Character.TYPE ) )
        {
            mv.visitVarInsn( ILOAD, idx );
        } else
        {
            mv.visitVarInsn( ALOAD, idx );
        }

        return idx;
    }

    private static void returnResult( MethodVisitor mv, Class<?> aClass, Label label )
    {
        if( aClass.equals( Integer.TYPE ) )
        {
            mv.visitInsn( IRETURN );
        } else if( aClass.equals( Long.TYPE ) )
        {
            mv.visitInsn( LRETURN );
        } else if( aClass.equals( Short.TYPE ) )
        {
            mv.visitInsn( IRETURN );
        } else if( aClass.equals( Byte.TYPE ) )
        {
            mv.visitInsn( IRETURN );
        } else if( aClass.equals( Double.TYPE ) )
        {
            mv.visitInsn( DRETURN );
        } else if( aClass.equals( Float.TYPE ) )
        {
            mv.visitInsn( FRETURN );
        } else if( aClass.equals( Boolean.TYPE ) )
        {
            mv.visitInsn( IRETURN );
        } else if( aClass.equals( Character.TYPE ) )
        {
            mv.visitInsn( IRETURN );
        } else
        {
            mv.visitTypeInsn( CHECKCAST, getInternalName( aClass ) );
            mv.visitInsn( ARETURN );
        }
    }

    private static void insn( MethodVisitor mv, int length )
    {
        switch (length)
        {
            case 0:
                mv.visitInsn( ICONST_0 );
                return;
            case 1:
                mv.visitInsn( ICONST_1 );
                return;
            case 2:
                mv.visitInsn( ICONST_2 );
                return;
            case 3:
                mv.visitInsn( ICONST_3 );
                return;
            case 4:
                mv.visitInsn( ICONST_4 );
                return;
            case 5:
                mv.visitInsn( ICONST_5 );
                return;
            default:
                mv.visitIntInsn( BIPUSH, length );
        }
    }
}
