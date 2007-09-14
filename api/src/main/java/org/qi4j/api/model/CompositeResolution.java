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
import java.lang.reflect.Method;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import org.qi4j.api.Composite;

/**
 * Composite model instances are resolutions in a runtime environment what a composite model will look like.
 */
public final class CompositeResolution<T extends Composite>
{
    private CompositeModel<T> compositeModel;
    private Set<MixinResolution> usedMixinModels; // List of used mixin models

    private Map<Class, MixinResolution> mixinsForInterfaces; // Interface -> mixin model
    private Map<Method, List<ModifierResolution>> modifiersForMethod; // Method -> modifier list

    public CompositeResolution( CompositeModel<T> compositeModel, Set<MixinResolution> usedMixinModels, Map<Class, MixinResolution> mixinsForInterfaces, Map<Method, List<ModifierResolution>> modifiersForMethod )
    {
        this.compositeModel = compositeModel;
        this.usedMixinModels = usedMixinModels;
        this.mixinsForInterfaces = mixinsForInterfaces;
        this.modifiersForMethod = modifiersForMethod;
    }

    public CompositeModel<T> getCompositeModel()
    {
        return compositeModel;
    }

    public Set<MixinResolution> getUsedMixinModels()
    {
        return usedMixinModels;
    }

    public MixinResolution getMixinForInterface( Class interfaceType )
    {
        return mixinsForInterfaces.get( interfaceType );
    }

    public Map<Class, MixinResolution> getInterfaceToMixinMappings()
    {
        return mixinsForInterfaces;
    }

    public List<ModifierResolution> getModifiersForMethod( Method method )
    {
        return modifiersForMethod.get( method );
    }

    public String toString()
    {
        StringWriter str = new StringWriter();
        PrintWriter out = new PrintWriter( str );
        out.println( compositeModel.getCompositeClass().getName() );

        for( Map.Entry<Class, MixinResolution> entry : mixinsForInterfaces.entrySet() )
        {
            Class interfaceClass = entry.getKey();
            MixinResolution mixinModel = entry.getValue();
            out.println( "    " + interfaceClass.getName() );
            out.println( "    implemented by " + mixinModel.getFragmentModel().getFragmentClass().getName() );
            Method[] methods = interfaceClass.getMethods();
            for( Method method : methods )
            {
                out.println( "      " + method.toGenericString() );
                ListIterator<ModifierResolution> methodModifierModels = getModifiersForMethod( method ).listIterator();
                while( methodModifierModels.hasNext() )
                {
                    ModifierResolution methodModifierModel = methodModifierModels.next();
                    out.println( "        " + methodModifierModel.getFragmentModel().getFragmentClass().getName() );
                }
            }
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

        CompositeResolution composite1 = (CompositeResolution) o;

        return compositeModel.equals( composite1.getCompositeModel() );

    }

    public int hashCode()
    {
        return compositeModel.hashCode();
    }
}