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

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.qi4j.api.entity.Lifecycle;
import org.qi4j.api.mixin.Initializable;
import org.qi4j.api.util.Classes;
import org.qi4j.api.util.Methods;
import org.qi4j.functional.Iterables;

import static org.objectweb.asm.Opcodes.AASTORE;
import static org.objectweb.asm.Opcodes.ACC_PRIVATE;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_STATIC;
import static org.objectweb.asm.Opcodes.ACC_SUPER;
import static org.objectweb.asm.Opcodes.ACONST_NULL;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ANEWARRAY;
import static org.objectweb.asm.Opcodes.ARETURN;
import static org.objectweb.asm.Opcodes.ASTORE;
import static org.objectweb.asm.Opcodes.ATHROW;
import static org.objectweb.asm.Opcodes.BIPUSH;
import static org.objectweb.asm.Opcodes.CHECKCAST;
import static org.objectweb.asm.Opcodes.DLOAD;
import static org.objectweb.asm.Opcodes.DRETURN;
import static org.objectweb.asm.Opcodes.DUP;
import static org.objectweb.asm.Opcodes.FLOAD;
import static org.objectweb.asm.Opcodes.FRETURN;
import static org.objectweb.asm.Opcodes.GETFIELD;
import static org.objectweb.asm.Opcodes.GETSTATIC;
import static org.objectweb.asm.Opcodes.GOTO;
import static org.objectweb.asm.Opcodes.ICONST_0;
import static org.objectweb.asm.Opcodes.ICONST_1;
import static org.objectweb.asm.Opcodes.ICONST_2;
import static org.objectweb.asm.Opcodes.ICONST_3;
import static org.objectweb.asm.Opcodes.ICONST_4;
import static org.objectweb.asm.Opcodes.ICONST_5;
import static org.objectweb.asm.Opcodes.ILOAD;
import static org.objectweb.asm.Opcodes.INVOKEINTERFACE;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;
import static org.objectweb.asm.Opcodes.IRETURN;
import static org.objectweb.asm.Opcodes.LLOAD;
import static org.objectweb.asm.Opcodes.LRETURN;
import static org.objectweb.asm.Opcodes.POP;
import static org.objectweb.asm.Opcodes.PUTSTATIC;
import static org.objectweb.asm.Opcodes.RETURN;
import static org.objectweb.asm.Type.getInternalName;
import static org.qi4j.api.util.Classes.interfacesOf;

/**
 * Generate subclasses of mixins/modifiers that implement all interfaces not in the class itself
 * and which delegates those calls to a given composite invoker.
 */
@SuppressWarnings( "raw" )
public class FragmentClassLoader
    extends ClassLoader
{
    private static final int JDK_VERSION;
    public static final String GENERATED_POSTFIX = "_Stub";

    static
    {
        String jdkString = System.getProperty( "java.specification.version" );
        switch( jdkString )
        {
            case "1.8":
                JDK_VERSION = Opcodes.V1_8;
                break;
            case "1.7":
            default:
                JDK_VERSION = Opcodes.V1_7;
                break;
        }
    }

    public FragmentClassLoader( ClassLoader parent )
    {
        super( parent );
    }

    @Override
    protected Class findClass( String name )
        throws ClassNotFoundException
    {
        if( name.endsWith( GENERATED_POSTFIX ) )
        {
            Class baseClass;
            String baseName = name.substring( 0, name.length() - 5 );
            try
            {
                baseClass = loadClass( baseName );
            }
            catch( ClassNotFoundException e )
            {
                // Try replacing the last _ with $
                while( true )
                {
                    int idx = baseName.lastIndexOf( "_" );
                    if( idx != -1 )
                    {
                        baseName = baseName.substring( 0, idx ) + "$" + baseName.substring( idx + 1 );
                        try
                        {
                            baseClass = loadClass( baseName );
                            break;
                        }
                        catch( ClassNotFoundException e1 )
                        {
                            // Try again
                        }
                    }
                    else
                    {
                        throw e;
                    }
                }
            }
//  To Allow JDK classes to be composed.
            if( name.startsWith( "java." ))
                name = "qi4j." + name;

            byte[] b = generateClass( name, baseClass );
            return defineClass( name, b, 0, b.length, baseClass.getProtectionDomain() );
        }

        // Try the classloader of this classloader -> get classes in Qi4j such as CompositeInvoker
        return getClass().getClassLoader().loadClass( name );
    }

    public static byte[] generateClass( String name, Class baseClass )
        throws ClassNotFoundException
    {
        String classSlash = name.replace( '.', '/' );
        String baseClassSlash = getInternalName( baseClass );

        ClassWriter cw = new ClassWriter( ClassWriter.COMPUTE_MAXS );

        // Class definition start
        cw.visit( JDK_VERSION, ACC_PUBLIC + ACC_SUPER, classSlash, null, baseClassSlash, null );

        // Composite reference
        {
            cw.visitField( ACC_PUBLIC, "_instance", "Lorg/qi4j/api/composite/CompositeInvoker;", null, null ).visitEnd();
        }

        // Static Method references
        boolean hasProxyMethods = false;
        {
            int idx = 1;
            for( Method method : baseClass.getMethods() )
            {
                if( isOverridden(method, baseClass) )
                {
                    cw.visitField( ACC_PRIVATE + ACC_STATIC, "m" + idx++, "Ljava/lang/reflect/Method;", null,
                                        null ).visitEnd();
                    hasProxyMethods = true;
                }
            }
        }

        // Constructors
        for( Constructor constructor : baseClass.getDeclaredConstructors() )
        {
            if( Modifier.isPublic( constructor.getModifiers() ) || Modifier.isProtected( constructor.getModifiers() ) )
            {
                String desc = org.objectweb.asm.commons.Method.getMethod( constructor ).getDescriptor();
                MethodVisitor cmv = cw.visitMethod( ACC_PUBLIC, "<init>", desc, null, null );
                cmv.visitCode();
                cmv.visitVarInsn(ALOAD, 0);

                int idx = 1;
                for( Class aClass : constructor.getParameterTypes() )
                {
                    final int opcode;
                    if (aClass.equals(Integer.TYPE)) {
                        opcode = ILOAD;
                    } else if (aClass.equals(Long.TYPE)) {
                        opcode = LLOAD;
                    } else if (aClass.equals(Float.TYPE)) {
                        opcode = FLOAD;
                    } else if (aClass.equals(Double.TYPE)) {
                        opcode = DLOAD;
                    } else {
                        opcode = ALOAD;
                    }
                    cmv.visitVarInsn(opcode, idx++);
                }

                cmv.visitMethodInsn(INVOKESPECIAL, baseClassSlash, "<init>", desc);
                cmv.visitInsn(RETURN);
                cmv.visitMaxs(idx, idx);
                cmv.visitEnd();
            }
        }


        // Overloaded and unimplemented methods
        if( hasProxyMethods )
        {
            Method[] methods = baseClass.getMethods();
            int idx = 0;
            List<Label> exceptionLabels = new ArrayList<>();
            for( Method method : methods )
            {
                if( isOverridden(method, baseClass) )
                {
                    idx++;
                    String methodName = method.getName();
                    String desc = org.objectweb.asm.commons.Method.getMethod( method ).getDescriptor();

                    String[] exceptions = null;
                    {
                        MethodVisitor mv = cw.visitMethod( ACC_PUBLIC, methodName, desc, null, null );
                        if( isInternalQi4jMethod( method, baseClass ) )
                        {
                            // generate a NoOp method...
                            mv.visitInsn( RETURN );
                        }
                        else
                        {
                            Label endLabel = null; // Use this if return type is void
                            if( method.getExceptionTypes().length > 0 )
                            {
                                exceptions = new String[ method.getExceptionTypes().length ];
                                for( int i = 0; i < method.getExceptionTypes().length; i++ )
                                {
                                    Class<?> aClass = method.getExceptionTypes()[ i ];
                                    exceptions[ i ] = getInternalName( aClass );
                                }
                            }
                            mv.visitCode();
                            Label l0 = new Label();
                            Label l1 = new Label();

                            exceptionLabels.clear();
                            for( Class<?> declaredException : method.getExceptionTypes() )
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
                            mv.visitFieldInsn( GETFIELD, classSlash, "_instance",
                                               "Lorg/qi4j/api/composite/CompositeInvoker;" );
                            mv.visitFieldInsn( GETSTATIC, classSlash, "m" + idx, "Ljava/lang/reflect/Method;" );

                            int paramCount = method.getParameterTypes().length;
                            int stackIdx = 0;
                            if( paramCount == 0 )
                            {
                                // Send in null as parameter
                                mv.visitInsn( ACONST_NULL );
                            }
                            else
                            {
                                insn( mv, paramCount );
                                mv.visitTypeInsn( ANEWARRAY, "java/lang/Object" );
                                int pidx = 0;
                                for( Class<?> aClass : method.getParameterTypes() )
                                {
                                    mv.visitInsn( DUP );
                                    insn( mv, pidx++ );
                                    stackIdx = wrapParameter( mv, aClass, stackIdx + 1 );
                                    mv.visitInsn( AASTORE );
                                }
                            }

                            // Call method
                            mv.visitMethodInsn( INVOKEINTERFACE, "org/qi4j/api/composite/CompositeInvoker",
                                                "invokeComposite",
                                                "(Ljava/lang/reflect/Method;[Ljava/lang/Object;)Ljava/lang/Object;" );

                            // Return value
                            if( !method.getReturnType().equals( Void.TYPE ) )
                            {
                                unwrapResult( mv, method.getReturnType(), l1 );
                            }
                            else
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
                            for( Class<?> aClass : method.getExceptionTypes() )
                            {
                                mv.visitLabel( exceptionLabels.get( exceptionIdx++ ) );
                                mv.visitFrame( Opcodes.F_SAME1, 0, null, 1, new Object[]{ getInternalName( aClass ) } );
                                mv.visitVarInsn( ASTORE, stackIdx );
                                mv.visitVarInsn( ALOAD, stackIdx );
                                mv.visitInsn( ATHROW );
                            }

                            // RuntimeException and Error catch-all
                            mv.visitLabel( lruntime );
                            mv.visitFrame( Opcodes.F_SAME1, 0, null, 1, new Object[]{ "java/lang/RuntimeException" } );
                            mv.visitVarInsn( ASTORE, stackIdx );
                            mv.visitVarInsn( ALOAD, stackIdx );
                            mv.visitInsn( ATHROW );

                            mv.visitLabel( lerror );
                            mv.visitFrame( Opcodes.F_SAME1, 0, null, 1, new Object[]{ "java/lang/Throwable" } );
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
                    }

                    if( !Modifier.isAbstract( method.getModifiers() ) )
                    {
                        // Add method with _ as prefix
                        MethodVisitor mv;
                        mv = cw.visitMethod( ACC_PUBLIC, "_" + method.getName(), desc, null, exceptions );
                        mv.visitCode();
                        mv.visitVarInsn( ALOAD, 0 );

                        // Parameters
                        int stackIdx = 1;
                        for( Class<?> aClass : method.getParameterTypes() )
                        {
                            stackIdx = loadParameter( mv, aClass, stackIdx ) + 1;
                        }

                        // Call method
                        mv.visitMethodInsn( INVOKESPECIAL, baseClassSlash, method.getName(), desc );

                        // Return value
                        if( !method.getReturnType().equals( Void.TYPE ) )
                        {
                            returnResult( mv, method.getReturnType() );
                        }
                        else
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
                MethodVisitor mv;
                mv = cw.visitMethod( ACC_STATIC, "<clinit>", "()V", null, null );
                mv.visitCode();
                Label l0 = new Label();
                Label l1 = new Label();
                Label l2 = new Label();
                mv.visitTryCatchBlock( l0, l1, l2, "java/lang/NoSuchMethodException" );
                mv.visitLabel( l0 );

                // Lookup methods and store in static variables
                int midx = 0;
                for( Method method : methods )
                {
                    if( isOverridden(method, baseClass) )
                    {
                        method.setAccessible( true );
                        Class methodClass;
                        if( Modifier.isAbstract( method.getModifiers() ) )
                        {
                            methodClass = method.getDeclaringClass();
                        }
                        else
                        {
                            try
                            {
                                methodClass = getInterfaceMethodDeclaration( method,
                                                                             baseClass ); // Overridden method lookup
                            }
                            catch( NoSuchMethodException e )
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
                        for( Class<?> aClass : method.getParameterTypes() )
                        {
                            mv.visitInsn( DUP );
                            insn( mv, pidx++ );
                            type( mv, aClass );
                            mv.visitInsn( AASTORE );
                        }

                        mv.visitMethodInsn( INVOKEVIRTUAL, "java/lang/Class", "getMethod",
                                            "(Ljava/lang/String;[Ljava/lang/Class;)Ljava/lang/reflect/Method;" );
                        mv.visitFieldInsn( PUTSTATIC, classSlash, "m" + midx, "Ljava/lang/reflect/Method;" );
                    }
                }

                mv.visitLabel( l1 );
                Label l3 = new Label();
                mv.visitJumpInsn( GOTO, l3 );
                mv.visitLabel( l2 );
                mv.visitFrame( Opcodes.F_SAME1, 0, null, 1, new Object[]{ "java/lang/NoSuchMethodException" } );
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

    private static boolean isOverridden(Method method, Class baseClass)
    {
        if( Modifier.isAbstract( method.getModifiers() ) )
        {
            return true; // Implement all abstract methods
        }

        if( Modifier.isFinal( method.getModifiers() ) )
        {
            return false; // Cannot override final methods
        }

        if( isInterfaceMethod( method, baseClass ) )
        {
            // if() used for clarity.
            //noinspection RedundantIfStatement
            if( isInternalQi4jMethod( method, baseClass ) )
            {
                return false; // Skip methods in Qi4j-internal interfaces
            }
            else
            {
                return true;
            }
        }
        else
        {
            return false;
        }
    }

    private static boolean isInternalQi4jMethod( Method method, Class baseClass )
    {
        return isDeclaredIn( method, Initializable.class, baseClass )
               || isDeclaredIn( method, Lifecycle.class, baseClass );
    }

    private static boolean isDeclaredIn( Method method, Class<?> clazz, Class<?> baseClass )
    {
        if( !clazz.isAssignableFrom( baseClass ) )
        {
            return false;
        }

        try
        {
            clazz.getMethod( method.getName(), method.getParameterTypes() );
            return true;
        }
        catch( NoSuchMethodException e )
        {
            return false;
        }
    }

    private static Class getInterfaceMethodDeclaration( Method method, Class clazz )
        throws NoSuchMethodException
    {
        Iterable<Class<?>> interfaces = Iterables.map( Classes.RAW_CLASS, interfacesOf( clazz ) );
        for( Class<?> anInterface : interfaces )
        {
            try
            {
                anInterface.getMethod( method.getName(), method.getParameterTypes() );
                return anInterface;
            }
            catch( NoSuchMethodException e )
            {
                // Try next
            }
        }

        throw new NoSuchMethodException( method.getName() );
    }

    private static boolean isInterfaceMethod( Method method, Class<?> baseClass )
    {
        for( Class<?> aClass : Iterables.filter( Methods.HAS_METHODS, Iterables.map( Classes.RAW_CLASS, interfacesOf( baseClass ) ) ) )
        {
            try
            {
                Method m = aClass.getMethod( method.getName(), method.getParameterTypes() );
                m.setAccessible( true );
                return true;
            }
            catch( NoSuchMethodException e )
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
        }
        else if( aClass.equals( Long.TYPE ) )
        {
            mv.visitFieldInsn( GETSTATIC, "java/lang/Long", "TYPE", "Ljava/lang/Class;" );
        }
        else if( aClass.equals( Short.TYPE ) )
        {
            mv.visitFieldInsn( GETSTATIC, "java/lang/Short", "TYPE", "Ljava/lang/Class;" );
        }
        else if( aClass.equals( Byte.TYPE ) )
        {
            mv.visitFieldInsn( GETSTATIC, "java/lang/Byte", "TYPE", "Ljava/lang/Class;" );
        }
        else if( aClass.equals( Double.TYPE ) )
        {
            mv.visitFieldInsn( GETSTATIC, "java/lang/Double", "TYPE", "Ljava/lang/Class;" );
        }
        else if( aClass.equals( Float.TYPE ) )
        {
            mv.visitFieldInsn( GETSTATIC, "java/lang/Float", "TYPE", "Ljava/lang/Class;" );
        }
        else if( aClass.equals( Boolean.TYPE ) )
        {
            mv.visitFieldInsn( GETSTATIC, "java/lang/Boolean", "TYPE", "Ljava/lang/Class;" );
        }
        else if( aClass.equals( Character.TYPE ) )
        {
            mv.visitFieldInsn( GETSTATIC, "java/lang/Character", "TYPE", "Ljava/lang/Class;" );
        }
        else
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
        }
        else if( aClass.equals( Long.TYPE ) )
        {
            mv.visitVarInsn( LLOAD, idx );
            mv.visitMethodInsn( INVOKESTATIC, "java/lang/Long", "valueOf", "(J)Ljava/lang/Long;" );
            idx++; // Extra jump
        }
        else if( aClass.equals( Short.TYPE ) )
        {
            mv.visitVarInsn( ILOAD, idx );
            mv.visitMethodInsn( INVOKESTATIC, "java/lang/Short", "valueOf", "(S)Ljava/lang/Short;" );
        }
        else if( aClass.equals( Byte.TYPE ) )
        {
            mv.visitVarInsn( ILOAD, idx );
            mv.visitMethodInsn( INVOKESTATIC, "java/lang/Byte", "valueOf", "(B)Ljava/lang/Byte;" );
        }
        else if( aClass.equals( Double.TYPE ) )
        {
            mv.visitVarInsn( DLOAD, idx );
            idx++; // Extra jump
            mv.visitMethodInsn( INVOKESTATIC, "java/lang/Double", "valueOf", "(D)Ljava/lang/Double;" );
        }
        else if( aClass.equals( Float.TYPE ) )
        {
            mv.visitVarInsn( FLOAD, idx );
            mv.visitMethodInsn( INVOKESTATIC, "java/lang/Float", "valueOf", "(F)Ljava/lang/Float;" );
        }
        else if( aClass.equals( Boolean.TYPE ) )
        {
            mv.visitVarInsn( ILOAD, idx );
            mv.visitMethodInsn( INVOKESTATIC, "java/lang/Boolean", "valueOf", "(Z)Ljava/lang/Boolean;" );
        }
        else if( aClass.equals( Character.TYPE ) )
        {
            mv.visitVarInsn( ILOAD, idx );
            mv.visitMethodInsn( INVOKESTATIC, "java/lang/Character", "valueOf", "(C)Ljava/lang/Character;" );
        }
        else
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
        }
        else if( aClass.equals( Long.TYPE ) )
        {
            mv.visitTypeInsn( CHECKCAST, "java/lang/Long" );
            mv.visitMethodInsn( INVOKEVIRTUAL, "java/lang/Long", "longValue", "()J" );
            mv.visitLabel( label );
            mv.visitInsn( LRETURN );
        }
        else if( aClass.equals( Short.TYPE ) )
        {
            mv.visitTypeInsn( CHECKCAST, "java/lang/Short" );
            mv.visitMethodInsn( INVOKEVIRTUAL, "java/lang/Short", "shortValue", "()S" );
            mv.visitLabel( label );
            mv.visitInsn( IRETURN );
        }
        else if( aClass.equals( Byte.TYPE ) )
        {
            mv.visitTypeInsn( CHECKCAST, "java/lang/Byte" );
            mv.visitMethodInsn( INVOKEVIRTUAL, "java/lang/Byte", "byteValue", "()B" );
            mv.visitLabel( label );
            mv.visitInsn( IRETURN );
        }
        else if( aClass.equals( Double.TYPE ) )
        {
            mv.visitTypeInsn( CHECKCAST, "java/lang/Double" );
            mv.visitMethodInsn( INVOKEVIRTUAL, "java/lang/Double", "doubleValue", "()D" );
            mv.visitLabel( label );
            mv.visitInsn( DRETURN );
        }
        else if( aClass.equals( Float.TYPE ) )
        {
            mv.visitTypeInsn( CHECKCAST, "java/lang/Float" );
            mv.visitMethodInsn( INVOKEVIRTUAL, "java/lang/Float", "floatValue", "()F" );
            mv.visitLabel( label );
            mv.visitInsn( FRETURN );
        }
        else if( aClass.equals( Boolean.TYPE ) )
        {
            mv.visitTypeInsn( CHECKCAST, "java/lang/Boolean" );
            mv.visitMethodInsn( INVOKEVIRTUAL, "java/lang/Boolean", "booleanValue", "()Z" );
            mv.visitLabel( label );
            mv.visitInsn( IRETURN );
        }
        else if( aClass.equals( Character.TYPE ) )
        {
            mv.visitTypeInsn( CHECKCAST, "java/lang/Character" );
            mv.visitMethodInsn( INVOKEVIRTUAL, "java/lang/Character", "charValue", "()C" );
            mv.visitLabel( label );
            mv.visitInsn( IRETURN );
        }
        else
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
        }
        else if( aClass.equals( Long.TYPE ) )
        {
            mv.visitVarInsn( LLOAD, idx );
            idx++; // Extra jump
        }
        else if( aClass.equals( Short.TYPE ) )
        {
            mv.visitVarInsn( ILOAD, idx );
        }
        else if( aClass.equals( Byte.TYPE ) )
        {
            mv.visitVarInsn( ILOAD, idx );
        }
        else if( aClass.equals( Double.TYPE ) )
        {
            mv.visitVarInsn( DLOAD, idx );
            idx++; // Extra jump
        }
        else if( aClass.equals( Float.TYPE ) )
        {
            mv.visitVarInsn( FLOAD, idx );
        }
        else if( aClass.equals( Boolean.TYPE ) )
        {
            mv.visitVarInsn( ILOAD, idx );
        }
        else if( aClass.equals( Character.TYPE ) )
        {
            mv.visitVarInsn( ILOAD, idx );
        }
        else
        {
            mv.visitVarInsn( ALOAD, idx );
        }

        return idx;
    }

    private static void returnResult( MethodVisitor mv, Class<?> aClass )
    {
        if( aClass.equals( Integer.TYPE ) )
        {
            mv.visitInsn( IRETURN );
        }
        else if( aClass.equals( Long.TYPE ) )
        {
            mv.visitInsn( LRETURN );
        }
        else if( aClass.equals( Short.TYPE ) )
        {
            mv.visitInsn( IRETURN );
        }
        else if( aClass.equals( Byte.TYPE ) )
        {
            mv.visitInsn( IRETURN );
        }
        else if( aClass.equals( Double.TYPE ) )
        {
            mv.visitInsn( DRETURN );
        }
        else if( aClass.equals( Float.TYPE ) )
        {
            mv.visitInsn( FRETURN );
        }
        else if( aClass.equals( Boolean.TYPE ) )
        {
            mv.visitInsn( IRETURN );
        }
        else if( aClass.equals( Character.TYPE ) )
        {
            mv.visitInsn( IRETURN );
        }
        else
        {
            mv.visitTypeInsn( CHECKCAST, getInternalName( aClass ) );
            mv.visitInsn( ARETURN );
        }
    }

    private static void insn( MethodVisitor mv, int length )
    {
        switch( length )
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

    public static boolean isGenerated( Class clazz )
    {
        return clazz.getName().endsWith( GENERATED_POSTFIX );
    }

    public static boolean isGenerated( Object object )
    {
        return object.getClass().getName().endsWith( GENERATED_POSTFIX );
    }

    public Class loadFragmentClass( Class fragmentClass )
        throws ClassNotFoundException
    {
        return loadClass( fragmentClass.getName().replace( '$', '_' ) + GENERATED_POSTFIX );
    }

    public static Class getSourceClass( Class fragmentClass )
    {
        return fragmentClass.getName().endsWith( GENERATED_POSTFIX ) ? fragmentClass.getSuperclass() : fragmentClass;
    }
}
