/*
 * Copyright 2010 Niclas Hedhman.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.qi4j.library.cxf;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import org.apache.cxf.aegis.type.AbstractTypeCreator;
import org.apache.cxf.aegis.type.AegisType;
import org.apache.cxf.aegis.type.TypeClassInfo;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.structure.Module;
import org.qi4j.api.util.Classes;
import org.qi4j.api.value.ValueComposite;

public class Qi4jTypeCreator extends AbstractTypeCreator
{
    @Structure
    private Module module;

    @Override
    public TypeClassInfo createClassInfo( PropertyDescriptor pd )
    {
        return null;
    }

    @Override
    public AegisType createCollectionType( TypeClassInfo info )
    {
        return null;
    }

    @Override
    public AegisType createDefaultType( TypeClassInfo info )
    {
        if( ValueComposite.class.isAssignableFrom( Classes.RAW_CLASS.map( info.getType() ) ) )
        {
            return module.newObject( ValueCompositeCxfType.class, info.getType(), getTypeMapping() );
        }
        return nextCreator.createDefaultType( info );
    }

    @Override
    public TypeClassInfo createClassInfo( Method m, int index )
    {
        return null;
    }
}
