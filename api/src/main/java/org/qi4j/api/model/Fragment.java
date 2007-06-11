/*
 * Copyright 2007 Rickard Öberg
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
*/
package org.qi4j.api.model;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.util.ArrayList;
import java.util.List;
import org.qi4j.api.annotation.AppliesTo;
import org.qi4j.api.annotation.Dependency;
import org.qi4j.api.annotation.Uses;

/**
 * Base class for fragments. Fragments are composed into objects.
 *
 * @see Mixin
 * @see Modifier
 */
public abstract class Fragment
{
    // Attributes ----------------------------------------------------
    private Class fragmentClass;
    private List<Field> usesFields;
    private List<Field> dependencyFields;
    protected Class appliesTo;

    // Constructors --------------------------------------------------
    public Fragment( Class modifierClass )
    {
        fragmentClass = modifierClass;

        this.usesFields = new ArrayList<Field>();
        findUses( modifierClass, usesFields );

        this.dependencyFields = new ArrayList<Field>();
        findDependency( modifierClass, dependencyFields );
        appliesTo = findAppliesTo( modifierClass );
    }

    // Public -------------------------------------------------------
    public Class getFragmentClass()
    {
        return fragmentClass;
    }

    public List<Field> getUsesFields()
    {
        return usesFields;
    }

    public List<Field> getDependencyFields()
    {
        return dependencyFields;
    }

    public Class getAppliesTo()
    {
        return appliesTo;
    }

    public boolean isAbstract()
    {
        return fragmentClass.isInterface();
    }

    public boolean isGeneric()
    {
        return InvocationHandler.class.isAssignableFrom( fragmentClass );
    }

    // Object overrides ---------------------------------------------
    public boolean equals( Object o )
    {
        if( this == o )
        {
            return true;
        }
        if( o == null || getClass() != o.getClass() )
        {
            return false;
        }

        Fragment fragment = (Fragment) o;

        return fragmentClass.equals( fragment.fragmentClass );
    }

    public int hashCode()
    {
        return fragmentClass.hashCode();
    }


    public String toString()
    {
        StringWriter str = new StringWriter();
        PrintWriter out = new PrintWriter( str );
        out.println( fragmentClass.getName() );
        if( usesFields.size() > 0 )
        {
            out.println( "  @Uses" );
            for( Field usesField : usesFields )
            {
                out.println( "    " + usesField.getType().getName() );
            }
        }

        if( dependencyFields.size() > 0 )
        {
            out.println( "  @Dependency" );
            for( Field dependencyField : dependencyFields )
            {
                out.println( "    " + dependencyField.getType().getName() );
            }
        }
        out.close();
        return str.toString();
    }

    // Private ------------------------------------------------------
    private void findUses( Class aModifierClass, List<Field> aUsesFields )
    {
        Field[] fields = aModifierClass.getDeclaredFields();
        for( Field field : fields )
        {
            if( field.getAnnotation( Uses.class ) != null )
            {
                field.setAccessible( true );
                aUsesFields.add( field );
            }
        }
        Class<?> parent = aModifierClass.getSuperclass();
        if( parent != null && parent != Object.class )
        {
            findUses( parent, aUsesFields );
        }
    }

    private void findDependency( Class aModifierClass, List<Field> aDependencyFields )
    {
        Field[] fields = aModifierClass.getDeclaredFields();
        for( Field field : fields )
        {
            if( field.getAnnotation( Dependency.class ) != null )
            {
                field.setAccessible( true );
                aDependencyFields.add( field );
            }
        }

        Class<?> parent = aModifierClass.getSuperclass();
        if( parent != null && parent != Object.class )
        {
            findDependency( parent, aDependencyFields );
        }
    }

    private Class findAppliesTo( Class aModifierClass )
    {
        AppliesTo appliesTo = (AppliesTo) aModifierClass.getAnnotation( AppliesTo.class );
        if( appliesTo != null )
        {
            return appliesTo.value();
        }

        Class<?> parent = aModifierClass.getSuperclass();
        if( parent != null && parent != Object.class )
        {
            return findAppliesTo( parent );
        }
        else
        {
            return null;
        }
    }
}
