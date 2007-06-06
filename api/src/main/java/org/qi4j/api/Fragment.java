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
package org.qi4j.api;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Iterator;
import java.io.StringWriter;
import java.io.PrintWriter;
import org.qi4j.api.annotation.Uses;
import org.qi4j.api.annotation.Dependency;

/**
 * Base class for fragments. Fragments are composed into objects.
 *
 * @author rickard
 * @version $Revision: 1.0 $
 * @see Mixin
 * @see Modifier
 */
public abstract class Fragment
{
    // Attributes ----------------------------------------------------
    Class<? extends Object> fragmentClass;
    List<Field> usesFields;
    List<Field> dependencyFields;

    // Constructors --------------------------------------------------
    public Fragment( Class<? extends Object> modifierClass )
    {
        fragmentClass = modifierClass;

        this.usesFields = new ArrayList<Field>();
        findUses( modifierClass, usesFields );

        this.dependencyFields = new ArrayList<Field>();
        findDependency( modifierClass, dependencyFields );
    }

    // Public -------------------------------------------------------
    public Class<? extends Object> getFragmentClass()
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
    private void findUses( Class<? extends Object> aModifierClass, List<Field> aUsesFields )
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
        if( parent != Object.class )
        {
            findUses( parent, aUsesFields );
        }
    }

    private void findDependency( Class<? extends Object> aModifierClass, List<Field> aDependencyFields )
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
        if( parent != Object.class )
        {
            findDependency( parent, aDependencyFields );
        }
    }
}
