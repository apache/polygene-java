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
import org.qi4j.api.annotation.Modifies;

/**
 * Modifiers provide stateless modifications of method invocation behaviour.
 * <p/>
 * Modifiers can either be classes implementing the interfaces of the modified
 * methods, or they can be generic InvocationHandler mixins.
 *
 */
public final class ModifierModel
    extends FragmentModel
{
    private Field modifiesField;

    // Constructors --------------------------------------------------
    public ModifierModel( Class modifierClass )
    {
        super( modifierClass );
        if( modifierClass.isInterface() )
        {
            throw new InvalidModifierException( "Interfaces can not be modifiers: " + modifierClass.getName(), modifierClass );
        }
        this.modifiesField = findModifies( modifierClass );
    }

    public Field getModifiesField()
    {
        return modifiesField;
    }

    // Object overrides ---------------------------------------------

    public String toString()
    {
        String string = super.toString();

        StringWriter str = new StringWriter();
        PrintWriter out = new PrintWriter( str );
        out.println( "  @Modifies" );
        out.println( "    " + modifiesField.getType().getName() );

        if( appliesTo != null )
        {
            out.println( "  @AppliesTo" );
            out.println( "    " + appliesTo.getName() );
        }
        out.close();
        return string + str.toString();
    }

    // Private ------------------------------------------------------
    private Field findModifies( Class<? extends Object> aModifierClass )
    {
        Field[] fields = aModifierClass.getDeclaredFields();
        Field modifiesField = null;
        for( Field field : fields )
        {
            if( field.getAnnotation( Modifies.class ) != null )
            {
                if( modifiesField != null )
                {
                    throw new InvalidModifierException( "Modifier " + aModifierClass + " has more than one @Modifies field: " + modifiesField.getName() + ", " + field.getName(), aModifierClass );
                }
                field.setAccessible( true );
                modifiesField = field;
            }
        }
        
        if( modifiesField != null )
        {
            return modifiesField;
        }

        Class<?> parent = aModifierClass.getSuperclass();
        if( parent != Object.class )
        {
            return findModifies( parent );
        }
        else
        {
            throw new InvalidModifierException( aModifierClass.getName() + " does not specify a @Modifies field.", aModifierClass );
        }
    }

}
