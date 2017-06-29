/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 */
package org.apache.polygene.library.restlet;

import java.io.PrintStream;
import org.apache.polygene.api.identity.HasIdentity;
import org.apache.polygene.api.injection.scope.Structure;
import org.apache.polygene.api.object.ObjectFactory;
import org.apache.polygene.api.structure.Application;
import org.apache.polygene.library.restlet.crud.EntityListResource;
import org.apache.polygene.library.restlet.crud.EntityResource;
import org.apache.polygene.library.restlet.resource.CreationResource;
import org.apache.polygene.library.restlet.resource.DefaultResourceFactoryImpl;
import org.apache.polygene.library.restlet.resource.EntryPoint;
import org.apache.polygene.library.restlet.resource.EntryPointResource;
import org.apache.polygene.library.restlet.resource.ResourceFactory;
import org.apache.polygene.library.restlet.resource.ServerResource;
import org.apache.polygene.library.restlet.serialization.PolygeneConverter;
import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Parameter;
import org.restlet.engine.Engine;
import org.restlet.routing.Filter;
import org.restlet.routing.Router;
import org.restlet.routing.TemplateRoute;
import org.restlet.security.ChallengeAuthenticator;
import org.restlet.security.Enroler;
import org.restlet.security.Verifier;
import org.restlet.util.Series;

/**
 * This class is generic enough to be promoted to Polygene's Restlet Library
 */
@SuppressWarnings( { "WeakerAccess", "unused" } )
public abstract class PolygeneRestApplication extends org.restlet.Application
{
    @Structure
    protected Application polygeneApplication;

    @Structure
    protected ObjectFactory objectFactory;

    protected Router router;

    protected PolygeneRestApplication( Context context )
    {
        super( context );
    }

    protected void printRoutes( PrintStream out )
    {
        router.getRoutes()
              .stream()
              .map( Object::toString )
              .forEach( out::println );
    }

    @Override
    public synchronized void start()
        throws Exception
    {
        setName( polygeneApplication.name() );
        Series<Parameter> parameters = getContext().getParameters();
        String mode = parameters.getFirstValue( "org.apache.polygene.runtime.mode" );
        super.start();
        getLogger().info( "RestApplication successfully started." );
    }

    @Override
    public synchronized void stop()
        throws Exception
    {
        super.stop();
        getLogger().info( "RestApplication successfully stopped." );
    }

    @Override
    public Restlet createInboundRoot()
    {
        Context context = getContext();
        Engine.getInstance().getRegisteredConverters().add( new PolygeneConverter( objectFactory ) );

        if( polygeneApplication.mode() == Application.Mode.development )
        {
            setDebugging( true );
        }
        router = new Router( context );

        addRoutes( router );
        router.attach( "/", newPolygeneRestlet( EntryPointResource.class, EntryPoint.class ) );

        ChallengeAuthenticator guard = new ChallengeAuthenticator( context, ChallengeScheme.HTTP_BASIC, getName() + " Realm" );

        Verifier verifier = createVerifier();
        if( verifier != null )
        {
            guard.setVerifier( verifier );
        }

        Enroler enroler = createEnroler();
        if( enroler != null )
        {
            guard.setEnroler( enroler );
        }
        return createInterceptors( guard );
    }

    private Restlet createInterceptors( ChallengeAuthenticator guard )
    {
        Filter inner = createInnerInterceptor();
        if( inner != null )
        {
            inner.setNext( router );
            guard.setNext( inner );             // guard -> interceptor -> router
        }
        else
        {
            guard.setNext( router );            // guard -> router
        }
        inner = guard;                      // inner = guard

        Filter outer = createOuterInterceptor();
        if( outer != null )
        {
            outer.setNext( inner );             // outer -> inner
            return outer;
        }
        return inner;
    }

    protected Filter createOuterInterceptor()
    {
        return null;
    }

    protected Filter createInnerInterceptor()
    {
        return null;
    }

    protected Verifier createVerifier()
    {
        return null;
    }

    protected Enroler createEnroler()
    {
        return null;
    }

    protected abstract void addRoutes( Router router );

    protected void addResourcePath( String name,
                                    Class<? extends HasIdentity> type,
                                    String basePath
                                  )
    {
        addResourcePath( name, type, basePath, true, true );
    }

    protected void addResourcePath( String name,
                                    Class<? extends HasIdentity> type,
                                    String basePath,
                                    boolean createLink,
                                    boolean rootRoute
                                  )
    {
        if( createLink )
        {
            router.attach( basePath + name + "/create", newPolygeneRestlet( CreationResource.class, type ) );
        }
        TemplateRoute route = router.attach( basePath + name + "/", newPolygeneRestlet( EntityListResource.class, type ) );
        if( rootRoute )
        {
            route.setName( name );
        }
        router.attach( basePath + name + "/{id}/", newPolygeneRestlet( EntityResource.class, type ) );
        router.attach( basePath + name + "/{id}/{invoke}", newPolygeneRestlet( EntityResource.class, type ) );
    }

    private <K extends HasIdentity, T extends ServerResource<K>> Restlet newPolygeneRestlet( Class<T> resourceClass, Class<K> entityClass )
    {

        @SuppressWarnings( "unchecked" )
        ResourceFactory<K, T> factory = objectFactory.newObject( DefaultResourceFactoryImpl.class,
                                                                 resourceClass, router
                                                               );
        PolygeneConverter converter = new PolygeneConverter( objectFactory );
        return objectFactory.newObject( PolygeneEntityRestlet.class,
                                        factory,
                                        router,
                                        entityClass,
                                        converter
                                      );
    }
}
