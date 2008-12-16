/*
 * Copyright 2008 Niclas Hedhman.
 *
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.qi4j.entitystore.foreign;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.qi4j.api.injection.Name;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.query.grammar.OrderBy;
import org.qi4j.api.service.ServiceException;
import org.qi4j.spi.entity.ConcurrentEntityStateModificationException;
import org.qi4j.spi.entity.DefaultEntityState;
import org.qi4j.spi.entity.EntityState;
import org.qi4j.spi.entity.EntityStatus;
import org.qi4j.spi.entity.EntityStore;
import org.qi4j.spi.entity.EntityStoreException;
import org.qi4j.spi.entity.EntityType;
import org.qi4j.spi.entity.EntityTypeRegistryMixin;
import org.qi4j.spi.entity.QualifiedIdentity;
import org.qi4j.spi.entity.StateCommitter;
import org.qi4j.spi.entity.UuidIdentityGeneratorMixin;
import org.qi4j.spi.query.EntityFinderException;
import org.qi4j.spi.query.named.NamedEntityFinder;
import org.qi4j.spi.query.named.QueryMethod;
import org.qi4j.spi.query.named.QueryResult;

public class ForeignServiceEntityFinderMixin extends EntityTypeRegistryMixin
    implements NamedEntityFinder, EntityStore

{
    private UuidIdentityGeneratorMixin generator;
    private Object beanService;
    private HashMap<String, QueryMethodDescriptor> methods;
    private HashMap<QualifiedIdentity, EntityState> store;

    public ForeignServiceEntityFinderMixin( @Uses Object beanService, @Uses Class mapInterface )
        throws IncompatibleServiceException
    {
        this.beanService = beanService;
        this.methods = new HashMap<String, QueryMethodDescriptor>();
        generator = new UuidIdentityGeneratorMixin();
        store = new HashMap<QualifiedIdentity, EntityState>();
        Method[] availableMethods = mapInterface.getMethods();
        for( Method mapMethod : availableMethods )
        {
            QueryMethod queryMethod = mapMethod.getAnnotation( QueryMethod.class );
            if( queryMethod != null )
            {
                String name = queryMethod.value();
                String methodName = mapInterface.getName();
                if( name == null )
                {
                    name = methodName;
                }
                Class[] paramTypes = mapMethod.getParameterTypes();
                Method beanMethod;
                try
                {
                    beanMethod = beanService.getClass().getMethod( methodName, paramTypes );
                }
                catch( NoSuchMethodException e )
                {
                    throw new IncompatibleServiceException( "Query map method " + methodName + " does not exist in " + beanService.getClass() );
                }
                QueryResult result = mapMethod.getAnnotation( QueryResult.class );
                Class resultType;
                if( result != null )
                {
                    resultType = result.value();
                }
                else
                {
                    resultType = beanMethod.getReturnType();
                }
                Annotation[][] annotations = mapMethod.getParameterAnnotations();
                HashMap<String, Integer> params = new HashMap<String, Integer>();
                int pos = 0;
                for( Annotation[] parameterAnnotations : annotations )
                {
                    boolean found = false;
                    for( Annotation annotation : parameterAnnotations )
                    {
                        if( annotation instanceof Name )
                        {
                            String paramName = ( (Name) annotation ).value();
                            params.put( paramName, pos );
                            found = true;
                            break;
                        }
                    }
                    if( !found )
                    {
                        params.put( "$" + pos, pos );
                    }
                    pos++;
                }
                QueryMethodDescriptor descriptor = new QueryMethodDescriptor( beanMethod, resultType, params );
                this.methods.put( name, descriptor );
            }
        }
    }

    public Iterable<QualifiedIdentity> findEntities( String name, String resultType, Map<String, Object> variables,
                                                     OrderBy[] orderBySegments, Integer firstResult,
                                                     Integer maxResults )
        throws EntityFinderException
    {
        QueryMethodDescriptor descriptor = methods.get( name );
        HashMap<String, Integer> params = descriptor.parameters();
        Method method = descriptor.method();
        Object[] args = new Object[params.size()];
        for( Map.Entry<String, Integer> entry : params.entrySet() )
        {
            args[ entry.getValue() ] = entry.getKey();
        }
        try
        {
            Object result = method.invoke( beanService, args );
            if( result.getClass().isArray() )
            {
                return arrayResult( (Object[]) result );
            }
            else if( result instanceof List )
            {
                return listResult( (List) result );
            }
            else if( result instanceof Set )
            {
                return setResult( (Set) result );
            }
            else
            {
                return objectResult( result );
            }
        }
        catch( IllegalAccessException e )
        {
            throw new IncompatibleServiceException( "Method is not public: " + method );
        }
        catch( InvocationTargetException e )
        {
            throw new ServiceException( "Underlying service threw an Exception.", e );
        }
    }

    private Iterable<QualifiedIdentity> objectResult( Object result )
    {
        List<QualifiedIdentity> data = new ArrayList<QualifiedIdentity>();
        QualifiedIdentity id = processItem( result, null );
        data.add( id );
        return data;
    }

    private Iterable<QualifiedIdentity> setResult( Set set )
    {
        List<QualifiedIdentity> data = new ArrayList<QualifiedIdentity>();
        for( Object item : set )
        {
            QualifiedIdentity id = processItem( item, null );
            data.add( id );
        }
        return data;
    }

    private Iterable<QualifiedIdentity> listResult( List list )
    {
        List<QualifiedIdentity> data = new ArrayList<QualifiedIdentity>();
        for( Object item : list )
        {
            QualifiedIdentity id = processItem( item, null );
            data.add( id );
        }
        return data;
    }

    private Iterable<QualifiedIdentity> arrayResult( Object[] objects )
    {
        List<QualifiedIdentity> data = new ArrayList<QualifiedIdentity>();
        for( Object item : objects )
        {
            QualifiedIdentity id = processItem( item, null );
            data.add( id );
        }
        return data;
    }

    private QualifiedIdentity processItem( Object item, String clazz )
    {
        String id = generator.generate( null );  // That UUID generator doesn't use the type for anything.
        QualifiedIdentity qid = new QualifiedIdentity( id, clazz );
        EntityStatus status = EntityStatus.LOADED;
        EntityType type = getEntityType( qid.type() );
        Map<String, Collection<QualifiedIdentity>> manyAssociations = new HashMap<String, Collection<QualifiedIdentity>>();
        Map<String, QualifiedIdentity> associations = new HashMap<String, QualifiedIdentity>();
        Map<String, Object> properties = new HashMap<String, Object>();

        // TODO populate the maps!!!

        EntityState state = new DefaultEntityState( 1L, System.currentTimeMillis(), qid, status, type,
                                                    properties, associations, manyAssociations );
        store.put( qid, state );
        return qid;
    }

    public QualifiedIdentity findEntity( String name, String resultType, Map<String, Object> variables )
        throws EntityFinderException
    {
        return null;
    }

    public long countEntities( String name, String resultType, Map<String, Object> variables )
        throws EntityFinderException
    {
        return 0;
    }

    public EntityState newEntityState( QualifiedIdentity anIdentity ) throws EntityStoreException
    {
        throw new UnsupportedOperationException( "This EntityStore does not allow creation of new Entities." );
    }

    public EntityState getEntityState( QualifiedIdentity anIdentity ) throws EntityStoreException
    {
        return store.get( anIdentity );
    }

    public StateCommitter prepare( Iterable<EntityState> newStates, Iterable<EntityState> loadedStates, Iterable<QualifiedIdentity> removedStates ) throws EntityStoreException, ConcurrentEntityStateModificationException
    {
        return new StateCommitter()
        {

            public void commit()
            {
            }

            public void cancel()
            {
            }
        };
    }

    public Iterator<EntityState> iterator()
    {
        return store.values().iterator();
    }
}
