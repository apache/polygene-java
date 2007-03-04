/*  Copyright 2007 Niclas Hedhman.
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
package org.ops4j.orthogon.internal;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.HashSet;
import org.ops4j.orthogon.Mixin;
import org.ops4j.orthogon.MixinUnavailableException;

public class MixinFactory
{
    private HashMap<Class, Class> m_mixinMapping;
    private HashSet<Class> m_mixinImplementations;

    public MixinFactory()
    {
        m_mixinMapping = new HashMap<Class, Class>();
        m_mixinImplementations = new HashSet<Class>();
        registerMixin( IdentityMixin.class );
    }

    public void registerMixin( Class aspectImplementationClass )
    {
        synchronized( this )
        {
            if( m_mixinImplementations.contains( aspectImplementationClass ) )
            {
                return;
            }
            Class[] classes = aspectImplementationClass.getInterfaces();
            for( Class<?> cls : classes )
            {
                Annotation[] annots = cls.getAnnotations();
                for( Annotation annot : annots )
                {
                    if( annot instanceof Mixin )
                    {
                        m_mixinMapping.put( cls, aspectImplementationClass );
                    }
                }
            }
            m_mixinImplementations.add( aspectImplementationClass );
        }
    }

    public void unregisterMixin( Class aspectImplementationClass )
    {
        synchronized( this )
        {
            Class[] classes = aspectImplementationClass.getInterfaces();
            for( Class<?> cls : classes )
            {
                Annotation[] annots = cls.getAnnotations();
                for( Annotation annot : annots )
                {
                    if( annot instanceof Mixin )
                    {
                        m_mixinMapping.remove( cls );
                    }
                }
            }
            m_mixinImplementations.remove( aspectImplementationClass );
        }
    }

    public Object create( Class aspectInterface )
    {
        Class aspectImplClass;
        synchronized( this )
        {
            aspectImplClass = m_mixinMapping.get( aspectInterface );
        }
        if( aspectImplClass == null )
        {
            return null;
        }
        try
        {
            return aspectImplClass.newInstance();
        }
        catch( InstantiationException e )
        {
            e.printStackTrace(); // TODO
        }
        catch( IllegalAccessException e )
        {
            e.printStackTrace(); // TODO
        }
        return null;
    }

    public boolean checkExistence( Class invokedOn )
    {
        return m_mixinMapping.containsKey( invokedOn );
    }
}
