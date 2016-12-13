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

import java.util.logging.Level;
import org.apache.polygene.api.composite.TransientBuilderFactory;
import org.apache.polygene.api.identity.HasIdentity;
import org.apache.polygene.api.object.ObjectFactory;
import org.apache.polygene.api.service.ServiceFinder;
import org.apache.polygene.api.structure.Application;
import org.apache.polygene.api.structure.Module;
import org.apache.polygene.api.unitofwork.UnitOfWorkFactory;
import org.apache.polygene.api.value.ValueBuilderFactory;
import org.apache.polygene.bootstrap.AssemblyException;
import org.apache.polygene.bootstrap.layered.LayeredApplicationAssembler;
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
public abstract class ZrestApplication extends org.restlet.Application
{
    protected org.apache.polygene.api.structure.Application polygeneApplication;
    protected ServiceFinder serviceFinder;
    protected ObjectFactory objectFactory;
    protected TransientBuilderFactory transientBuilderFactory;
    protected UnitOfWorkFactory unitOfWorkFactory;
    protected ValueBuilderFactory valueBuilderFactory;

    private Router router;

    public ZrestApplication( Context context )
        throws AssemblyException
    {
        super( context );
    }

    protected void printRoutes()
    {
        router.getRoutes().stream().forEach(
            route -> System.out.println( route.toString() ) );
    }

    protected abstract LayeredApplicationAssembler createApplicationAssembler( String mode )
        throws AssemblyException;

    @Override
    public synchronized void start()
        throws Exception
    {
        Series<Parameter> parameters = getContext().getParameters();
        String mode = parameters.getFirstValue( "org.apache.polygene.runtime.mode" );
        createApplication( mode );
        polygeneApplication.activate();
        Module module = polygeneApplication.findModule( getConnectivityLayer(), getConnectivityModule() );
        serviceFinder = module;
        objectFactory = module;
        transientBuilderFactory = module;
        unitOfWorkFactory = module.unitOfWorkFactory();
        valueBuilderFactory = module;
        super.start();
    }

    private void createApplication( String mode )
    {
        try
        {
            LayeredApplicationAssembler assembler = createApplicationAssembler(mode);
            assembler.initialize();
            polygeneApplication = assembler.application();
            setName( polygeneApplication.name() );
        }
        catch( Throwable e )
        {
            e.printStackTrace();
            getLogger().log( Level.SEVERE, "Unable to start Polygene application.", e );
            throw new InternalError( "Unable to start Polygene application.", e );
        }
        getLogger().info( "RestApplication successfully created." );
    }

    @Override
    public synchronized void stop()
        throws Exception
    {
        super.stop();
        polygeneApplication.passivate();
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

        ChallengeAuthenticator guard = new ChallengeAuthenticator( context, ChallengeScheme.HTTP_BASIC, "Storm Clouds" );

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

        // In the future, look into JAAS approach.
//        Configuration jaasConfig = Configuration.getConfiguration();
//        JaasVerifier verifier = new JaasVerifier( "BasicJaasAuthenticationApplication");
//        verifier.setConfiguration( jaasConfig);
//        verifier.setUserPrincipalClassName("com.sun.security.auth.UserPrincipal");
//        guard.setVerifier(verifier);

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

    protected abstract String getConnectivityLayer();

    protected abstract String getConnectivityModule();

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

    private <K extends HasIdentity, T extends ServerResource<K>> Restlet newPolygeneRestlet(Class<T> resourceClass,
                                                                                        Class<K> entityClass
    )
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
