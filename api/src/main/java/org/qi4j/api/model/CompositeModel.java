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
 */
public final class CompositeModel
{
    // Attributes ----------------------------------------------------
    private Class composite;
    private List<MixinModel> implementations = new ArrayList<MixinModel>();
    private List<ModifierModel> modifierModels = new ArrayList<ModifierModel>();

    // Constructors --------------------------------------------------
    public CompositeModel( Class compositeClass )
    {
        if( compositeClass == null )
        {
            throw new NullArgumentException( "compositeClass is null" );
        }
        this.composite = compositeClass;

        findImplementations( composite );
        findModifiers( composite );
    }

    // Public --------------------------------------------------------
    public Class getCompositeClass()
    {
        return composite;
    }

    public List<MixinModel> getImplementations()
    {
        return implementations;
    }

    public List<ModifierModel> getModifiers()
    {
        return modifierModels;
    }

    public List<MixinModel> getImplementations( Class aType )
    {
        List<MixinModel> impls = new ArrayList<MixinModel>();

        // Check non-generic impls first
        for( MixinModel implementation : implementations )
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
        for( MixinModel implementation : implementations )
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
        for( MixinModel implementation : implementations )
        {
            out.println( "    " + implementation.getFragmentClass().getName() );
        }

        out.println( "  modified by" );
        for( ModifierModel modifierModel : modifierModels )
        {
            out.println( "    " + modifierModel.getFragmentClass().getName() );
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

        CompositeModel composite1 = (CompositeModel) o;

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
                implementations.add( new MixinModel( impl ) );
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
                modifierModels.add( new ModifierModel( modifier ) );
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
