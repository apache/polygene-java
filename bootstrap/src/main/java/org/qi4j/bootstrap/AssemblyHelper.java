/*
 * Copyright (c) 2007, Rickard Öberg. All Rights Reserved.
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

package org.qi4j.bootstrap;

import java.lang.reflect.Method;
import org.qi4j.association.AbstractAssociation;
import org.qi4j.property.ImmutableProperty;

/**
 * TODO
 */
public final class AssemblyHelper
{
    private ModuleAssembly module;

    public AssemblyHelper( ModuleAssembly module )
    {
        this.module = module;
    }

    public AssemblyHelper add( Class interfaceClass )
    {
        try
        {
            for( Method method : interfaceClass.getMethods() )
            {
                if( ImmutableProperty.class.isAssignableFrom( method.getReturnType() ) )
                {
                    // Register property
                    PropertyDeclaration declaration = module.addProperty();
                    method.invoke( declaration.withAccessor( interfaceClass ) );
                    addProperty( declaration, method );
                }
                else if( method.getReturnType().equals( AbstractAssociation.class ) )
                {
                    // Register association
                    AssociationDeclaration declaration = module.addAssociation();
                    method.invoke( declaration.withAccessor( interfaceClass ) );
                    addAssociation( declaration, method );
                }

            }
        }
        catch( Exception e )
        {
            // Should never happen...
            e.printStackTrace();
        }

        return this;
    }

    protected void addProperty( PropertyDeclaration declaration, Method accessor )
    {
        // Override this method if you want to add custom initialization
    }

    protected void addAssociation( AssociationDeclaration declaration, Method accessor )
    {
        // Override this method if you want to add custom initialization
    }
}
