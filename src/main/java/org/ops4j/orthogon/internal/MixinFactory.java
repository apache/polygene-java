/*  Copyright 2007 Niclas Hedhman.
 *  Copyright 2007 Edward Yakop
 *  Copyright 2007 Rickard Oberg
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
import java.util.Map;
import java.util.Set;
import org.ops4j.lang.NullArgumentException;
import org.ops4j.orthogon.Identity;
import org.ops4j.orthogon.mixin.IdGenerator;
import org.ops4j.orthogon.mixin.QiIdGenerator;
import org.ops4j.orthogon.mixin.QiMixin;

public final class MixinFactory
{
    private final Map<Class, Class<Object>> m_mixinMapping;
    private final Set<Class> m_mixinImplementations;
    private final Map<Class, IdGenerator> m_generators;

    public MixinFactory()
    {
        m_mixinMapping = new HashMap<Class, Class<Object>>();
        m_generators = new HashMap<Class, IdGenerator>();
        m_mixinImplementations = new HashSet<Class>();
        registerMixin( IdentityMixin.class );
    }

    public final void registerMixin( Class... mixinImplementationClasses )
        throws IllegalArgumentException
    {
        NullArgumentException.validateNotNull( mixinImplementationClasses, "mixinImplementationClasses" );

        for( int i = 0; i < mixinImplementationClasses.length; i++ )
        {
            Class mixinImplementationClass = mixinImplementationClasses[ i ];

            if( mixinImplementationClass == null )
            {
                throw new IllegalArgumentException( "Mixin implementation index [" + i + "] is [null]." );
            }

            synchronized( this )
            {
                if( m_mixinImplementations.contains( mixinImplementationClasses ) )
                {
                    return;
                }

                Class[] classes = mixinImplementationClass.getInterfaces();
                for( Class cls : classes )
                {
                    Annotation[] annots = cls.getAnnotations();
                    for( Annotation annot : annots )
                    {
                        if( annot instanceof QiMixin )
                        {
                            m_mixinMapping.put( cls, mixinImplementationClass );
                        }
                    }
                }
                m_mixinImplementations.add( mixinImplementationClass );
            }
        }
    }

    public final boolean checkExistence( Class mixinInterface )
    {
        if( mixinInterface == null )
        {
            return false;
        }

        synchronized( this )
        {
            return m_mixinMapping.containsKey( mixinInterface );
        }
    }

    public final Object create( Class mixinInterface, Class primaryAspect )
        throws IllegalArgumentException
    {
        NullArgumentException.validateNotNull( primaryAspect, "primaryAspect" );

        if( mixinInterface == null )
        {
            return null;
        }

        if( Identity.class.equals( mixinInterface ) )
        {
            return createsIdentityMixin( primaryAspect );
        }
        else
        {
            return createsNonIdentityMixin( mixinInterface );
        }
    }

    /**
     * Creates identity mixin. Returns {@code null}
     *
     * @param primaryAspect The primary aspect used to identify the identity generator.
     *
     * @return An instance of identity mixin.
     */
    private Identity createsIdentityMixin( Class primaryAspect )
    {
        QiIdGenerator annotation = (QiIdGenerator) primaryAspect.getAnnotation( QiIdGenerator.class );
        if( annotation == null )
        {
            // TODO: Should we use standard id generator?
            return null;
        }

        Class<? extends IdGenerator> generatorClass = annotation.value();

        IdGenerator generator;
        synchronized( m_generators )
        {
            generator = m_generators.get( generatorClass );

            if( generator == null )
            {
                generator = newInstance( generatorClass );
                m_generators.put( generatorClass, generator );
            }
        }

        if( generator != null )
        {
            String identity = generator.generateId( primaryAspect );
            return new IdentityMixin( identity );
        }

        return null;
    }

    private Object createsNonIdentityMixin( Class mixinInterface )
    {
        Class<Object> mixinImplementation;
        synchronized( this )
        {
            mixinImplementation = m_mixinMapping.get( mixinInterface );
        }

        if( mixinImplementation == null )
        {
            return null;
        }

        return newInstance( mixinImplementation );
    }

    private <T> T newInstance( Class<T> aClass )
    {
        try
        {
            return aClass.newInstance();
        }
        catch( InstantiationException e )
        {
            // TODO: means the mixin implementation is an abstract or an interface. This should not happened.
            e.printStackTrace();
        }
        catch( IllegalAccessException e )
        {
            // TODO: means the default constructor is not public. This should not happened
            e.printStackTrace();
        }
        return null;
    }

    public void unregisterMixin( Class... mixinImplementationClasses )
        throws IllegalArgumentException
    {
        if( mixinImplementationClasses == null )
        {
            return;
        }

        for( Class mixinImplementationClass : mixinImplementationClasses )
        {
            if( mixinImplementationClass == null )
            {
                continue;
            }

            synchronized( this )
            {
                Class[] classes = mixinImplementationClass.getInterfaces();
                for( Class cls : classes )
                {
                    Annotation[] annots = cls.getAnnotations();
                    for( Annotation annot : annots )
                    {
                        if( annot instanceof QiMixin )
                        {
                            m_mixinMapping.remove( cls );
                        }
                    }
                }
                m_mixinImplementations.remove( mixinImplementationClasses );
            }
        }
    }
}
