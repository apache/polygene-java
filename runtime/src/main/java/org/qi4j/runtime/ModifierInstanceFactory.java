/*
 * Copyright (c) 2007, Rickard Öberg. All Rights Reserved.
 * Copyright (c) 2007, Niclas Hedhman. All Rights Reserved.
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
package org.qi4j.runtime;

import org.qi4j.api.MixinFactory;
import org.qi4j.api.ObjectFactory;
import org.qi4j.api.InvocationContext;
import org.qi4j.api.annotation.ModifiedBy;
import org.qi4j.api.annotation.Modifies;
import org.qi4j.api.annotation.Uses;
import org.qi4j.api.annotation.Dependency;
import org.qi4j.api.annotation.AppliesTo;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

public final class ModifierInstanceFactory
{
    ObjectFactory objectFactory;
    MixinFactory mixinFactory;

    public ModifierInstanceFactory( ObjectFactory anObjectFactory, MixinFactory aMixinFactory )
    {
        objectFactory = anObjectFactory;
        mixinFactory = aMixinFactory;
    }

    ModifierInstance newInstance( Class invocationType, Class modifierType, Class mixinClass, ProxyReferenceInvocationHandler proxyHandler )
    {
        try
        {
            // Instantiate modifier
            List<Class> modifierClasses = new ArrayList<Class>();
            findModifiers( invocationType, modifierType, modifierClasses );
            ModifierInstance instance = new ModifierInstance();
            Object previousModifier = instance;
            if( !Proxy.class.isAssignableFrom( mixinClass ) )
            {
                for( Class modifierClass : modifierClasses )
                {
                    AppliesTo appliesTo = (AppliesTo) modifierClass.getAnnotation( AppliesTo.class );
                    if( appliesTo != null )
                    {
                        // Check AppliesTo
                        boolean ok = false;
                        if( appliesTo.value().isAnnotation() )
                        {
                            if( mixinClass.getAnnotation( appliesTo.value() ) != null )
                            {
                                ok = true;
                            }
                        }
                        else
                        {
                            if( appliesTo.value().isAssignableFrom( mixinClass ) )
                            {
                                ok = true;
                            }
                        }

                        if( !ok )
                        {
                            continue;
                        }
                    }

                    Object modifier = modifierClass.newInstance();

                    // @Uses
                    List<Field> usesFields = new ArrayList<Field>();
                    findUses( modifier.getClass(), usesFields );

                    for( Field usesField : usesFields )
                    {
                        Object usesProxy = Proxy.newProxyInstance( usesField.getType().getClassLoader(), new Class[]{ usesField.getType() }, proxyHandler );
                        usesField.set( modifier, usesProxy );
                    }

                    // @Dependency
                    List<Field> dependencyFields = new ArrayList<Field>();
                    findDependency( modifier.getClass(), dependencyFields );

                    for( Field dependencyField : dependencyFields )
                    {
                        if( dependencyField.getType().equals( ObjectFactory.class ) )
                        {
                            dependencyField.set( modifier, objectFactory );
                        }
                        else if( dependencyField.getType().equals( MixinFactory.class ) )
                        {
                            dependencyField.set( modifier, mixinFactory );
                        }
                        else if( dependencyField.getType().equals( InvocationContext.class ) )
                        {
                            dependencyField.set( modifier, proxyHandler );
                        }
                    }

                    // @Modifies
                    Field modifiesField = findModifies( previousModifier.getClass() );
                    if( modifiesField == null )
                    {
                        throw new IllegalStateException( "No @Modifies field in modifier " + previousModifier.getClass() );
                    }
                    if( InvocationHandler.class.isAssignableFrom( modifierClass ) )
                    {
                        Object modifierProxy = Proxy.newProxyInstance( modifierClass.getClassLoader(), new Class[]{ invocationType }, (InvocationHandler) modifier );
                        modifiesField.set( previousModifier, modifierProxy );
                    }
                    else
                    {
                        modifiesField.setAccessible( true );
                        modifiesField.set( previousModifier, modifier );
                    }
                    previousModifier = modifier;
                }
                Field modifiesField = findModifies( previousModifier.getClass() );
                if( modifiesField == null )
                {
                    throw new IllegalStateException( "No @Modifies field in modifier " + previousModifier.getClass() );
                }
                instance.setLastModifies( modifiesField );
            }
            instance.setLastModifier( previousModifier );
            return instance;
        }
        catch( IllegalStateException e )
        {
            throw e;
        }
        catch( Exception e )
        {
            throw new IllegalStateException( e );
        }
    }

    private void findUses( Class<? extends Object> aClass, List<Field> aUsesFields )
    {
        Field[] fields = aClass.getDeclaredFields();
        for( Field field : fields )
        {
            if( field.getAnnotation( Uses.class ) != null )
            {
                field.setAccessible( true );
                aUsesFields.add( field );
            }
        }
        Class<?> parent = aClass.getSuperclass();
        if( parent != Object.class )
        {
            findUses( parent, aUsesFields );
        }
    }

    private void findDependency( Class<? extends Object> aClass, List<Field> aDependencyFields )
    {
        Field[] fields = aClass.getDeclaredFields();
        for( Field field : fields )
        {
            if( field.getAnnotation( Dependency.class ) != null )
            {
                field.setAccessible( true );
                aDependencyFields.add( field );
            }
        }
        Class<?> parent = aClass.getSuperclass();
        if( parent != Object.class )
        {
            findDependency( parent, aDependencyFields );
        }
    }

    private Field findModifies( Class<? extends Object> aClass )
    {
        Field[] fields = aClass.getDeclaredFields();
        for( Field field : fields )
        {
            if( field.getAnnotation( Modifies.class ) != null )
            {
                field.setAccessible( true );
                return field;
            }
        }
        Class<?> parent = aClass.getSuperclass();
        if( parent != Object.class )
        {
            return findModifies( parent );
        }
        return null;
    }

    private void findModifiers( Class invocationType, Class modifierType, List<Class> aModifierList )
    {
        ModifiedBy modifiedBy = (ModifiedBy) modifierType.getAnnotation( ModifiedBy.class );
        if( modifiedBy != null )
        {
            Class[] modificationClasses = modifiedBy.value();
            for( Class modificationClass : modificationClasses )
            {
                if( invocationType.isAssignableFrom( modificationClass ) || InvocationHandler.class.isAssignableFrom( modificationClass ) )
                {
                    aModifierList.add( modificationClass );
                }
            }
        }

        // Check subinterfaces
        Class[] subTypes = modifierType.getInterfaces();
        for( Class subType : subTypes )
        {
            findModifiers( invocationType, subType, aModifierList );
        }
        if( !modifierType.isInterface() && modifierType != Object.class )
        {
            findModifiers( invocationType, modifierType.getSuperclass(), aModifierList );
        }
    }

}