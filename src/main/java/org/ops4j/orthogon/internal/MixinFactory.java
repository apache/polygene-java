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
import org.ops4j.orthogon.mixin.QiMixin;

public class MixinFactory
{
    private HashMap<Class, Class> m_introductionMapping;
    private HashSet<Class> m_introductionImplementations;

    public MixinFactory()
    {
        m_introductionMapping = new HashMap<Class, Class>();
        m_introductionImplementations = new HashSet<Class>();
        registerIntroduction( IdentityMixin.class );
    }

    public void registerIntroduction( Class introductionImplementationClass )
    {
        synchronized( this )
        {
            if( m_introductionImplementations.contains( introductionImplementationClass ) )
            {
                return;
            }
            Class[] classes = introductionImplementationClass.getInterfaces();
            for( Class<?> cls : classes )
            {
                Annotation[] annots = cls.getAnnotations();
                for( Annotation annot : annots )
                {
                    if( annot instanceof QiMixin )
                    {
                        m_introductionMapping.put( cls, introductionImplementationClass );
                    }
                }
            }
            m_introductionImplementations.add( introductionImplementationClass );
        }
    }

    public void unregisterIntroduction( Class introductionImplementationClass )
    {
        synchronized( this )
        {
            Class[] classes = introductionImplementationClass.getInterfaces();
            for( Class<?> cls : classes )
            {
                Annotation[] annots = cls.getAnnotations();
                for( Annotation annot : annots )
                {
                    if( annot instanceof QiMixin )
                    {
                        m_introductionMapping.remove( cls );
                    }
                }
            }
            m_introductionImplementations.remove( introductionImplementationClass );
        }
    }

    public Object create( Class aspectInterface )
    {
        Class aspectImplClass;
        synchronized( this )
        {
            aspectImplClass = m_introductionMapping.get( aspectInterface );
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
        return m_introductionMapping.containsKey( invokedOn );
    }
}
