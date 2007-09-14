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
import java.util.Iterator;
import org.qi4j.api.annotation.Modifies;

/**
 * Modifiers provide stateless modifications of method invocation behaviour.
 * <p/>
 * Modifiers can either be classes implementing the interfaces of the modified
 * methods, or they can be generic InvocationHandler mixins.
 */
public final class ModifierModel<T>
    extends FragmentModel<T>
{
    private Dependency modifiesDependency;

    // Constructors --------------------------------------------------
    public ModifierModel( Class<T> fragmentClass, Iterable<ConstructorDependency> constructorDependencies, Iterable<FieldDependency> fieldDependencies, Iterable<MethodDependency> methodDependencies, Class appliesTo )
    {
        super( fragmentClass, constructorDependencies, fieldDependencies, methodDependencies, appliesTo );

        Iterator<Dependency> modifies = getDependenciesByScope( Modifies.class ).iterator();
        if( modifies.hasNext() )
        {
            this.modifiesDependency = modifies.next();
        }
    }

    public Dependency getModifiesDependency()
    {
        return modifiesDependency;
    }

    // Object overrides ---------------------------------------------

    public String toString()
    {
        String string = super.toString();

        StringWriter str = new StringWriter();
        PrintWriter out = new PrintWriter( str );
        out.println( "  @Modifies" );
        out.println( "    " + modifiesDependency.getKey().getRawClass().getSimpleName() );

        if( appliesTo != null )
        {
            out.println( "  @AppliesTo" );
            out.println( "    " + appliesTo.getName() );
        }
        out.close();
        return string + str.toString();
    }

    // Private ------------------------------------------------------

}
