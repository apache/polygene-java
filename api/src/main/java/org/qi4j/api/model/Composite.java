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
import java.util.ArrayList;
import java.util.List;
import org.qi4j.api.annotation.ImplementedBy;
import org.qi4j.api.annotation.ModifiedBy;

/**
 * Composites are descriptors of what an interface represent. <TODO better docs needed here>
 *
 * @author rickard
 * @version $Revision: 1.7 $
 */
public final class Composite
{
    // Attributes ----------------------------------------------------
    Class composite;
    List<Mixin> implementations = new ArrayList<Mixin>();
    List<Modifier> modifiers = new ArrayList<Modifier>();

    // Constructors --------------------------------------------------
    public Composite( Class aCompositeClass )
    {
        this.composite = aCompositeClass;

        findImplementations( composite );
        findModifiers( composite );
    }

    // Public --------------------------------------------------------
    public Class getCompositeClass()
    {
        return composite;
    }

    public List<Mixin> getImplementations()
    {
        return implementations;
    }

    public List<Modifier> getModifiers()
    {
        return modifiers;
    }

    public List<Mixin> getImplementations( Class aType )
    {
        List<Mixin> impls = new ArrayList<Mixin>();

        // Check non-generic impls first
        for( Mixin implementation : implementations )
        {
            if( !implementation.isGeneric() )
            {
                if( aType.isAssignableFrom( implementation.getFragmentClass() ) )
                {
                    impls.add( implementation );
                }
            }
        }

        // Check generic impls
        for( Mixin implementation : implementations )
        {
            if( implementation.isGeneric() )
            {
                // Check AppliesTo
                Class appliesTo = implementation.getAppliesTo();
                if( appliesTo == null || appliesTo.isAssignableFrom( aType ) )
                {
                    impls.add( implementation ); // This generic mixin can handle the given type
                }
            }
        }

        return impls;
    }

    // Object overrides ---------------------------------------------
    public String toString()
    {
        StringWriter str = new StringWriter();
        PrintWriter out = new PrintWriter( str );
        out.println( composite.getName() );

        out.println( "  implemented by" );
        for( Mixin implementation : implementations )
        {
            out.println( "    " + implementation.getFragmentClass().getName() );
        }

        out.println( "  modified by" );
        for( Modifier modifier : modifiers )
        {
            out.println( "    " + modifier.getFragmentClass().getName() );
        }
        out.close();
        return str.toString();
    }


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

        Composite composite1 = (Composite) o;

        return composite.equals( composite1.composite );

    }

    public int hashCode()
    {
        return composite.hashCode();
    }

    // Private -------------------------------------------------------
    private void findImplementations( Class aType )
    {
        ImplementedBy impls = (ImplementedBy) aType.getAnnotation( ImplementedBy.class );
        if( impls != null )
        {
            for( Class impl : impls.value() )
            {
                implementations.add( new Mixin( impl ) );
            }
        }

        // Check subinterfaces
        Class[] subTypes = aType.getInterfaces();
        for( Class subType : subTypes )
        {
            findImplementations( subType );
        }
    }

    private void findModifiers( Class aClass )
    {
        ModifiedBy modifiedBy = (ModifiedBy) aClass.getAnnotation( ModifiedBy.class );
        if( modifiedBy != null )
        {
            for( Class<? extends Object> modifier : modifiedBy.value() )
            {
                modifiers.add( new Modifier( modifier ) );
            }
        }

        // Check subinterfaces
        Class[] subTypes = aClass.getInterfaces();
        for( Class subType : subTypes )
        {
            findModifiers( subType );
        }
    }
}
