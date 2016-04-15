/*
 * Copyright 2015 Niclas Hedhman, niclas@hedhman.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.zest.library.restlet;

import java.util.logging.Level;
import org.apache.zest.api.composite.TransientBuilderFactory;
import org.apache.zest.api.entity.Identity;
import org.apache.zest.api.object.ObjectFactory;
import org.apache.zest.api.service.ServiceFinder;
import org.apache.zest.api.structure.Application;
import org.apache.zest.api.structure.Module;
import org.apache.zest.api.unitofwork.UnitOfWorkFactory;
import org.apache.zest.api.value.ValueBuilderFactory;
import org.apache.zest.bootstrap.AssemblyException;
import org.apache.zest.bootstrap.layered.LayeredApplicationAssembler;
import org.apache.zest.library.restlet.crud.EntityListResource;
import org.apache.zest.library.restlet.crud.EntityResource;
import org.apache.zest.library.restlet.resource.CreationResource;
import org.apache.zest.library.restlet.resource.DefaultResourceFactoryImpl;
import org.apache.zest.library.restlet.resource.EntryPoint;
import org.apache.zest.library.restlet.resource.EntryPointResource;
import org.apache.zest.library.restlet.resource.ResourceFactory;
import org.apache.zest.library.restlet.resource.ServerResource;
import org.apache.zest.library.restlet.serialization.ZestConverter;
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
 * This class is generic enough to be promoted to Zest's Restlet Library
 */
public abstract class ZrestApplication extends org.restlet.Application
{
    protected org.apache.zest.api.structure.Application zestApplication;
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
        System.err.println( "HABBA!!!!!" );
        Series<Parameter> parameters = getContext().getParameters();
        String mode = parameters.getFirstValue( "org.sensorsink.kooda.mode" );
        createApplication( mode );
        zestApplication.activate();
        Module module = zestApplication.findModule( getConnectivityLayer(), getConnectivityModule() );
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
            zestApplication = assembler.application();
            setName( zestApplication.name() );
        }
        catch( Throwable e )
        {
            e.printStackTrace();
            getLogger().log( Level.SEVERE, "Unable to start Zest application.", e );
            throw new InternalError( "Unable to start Zest application.", e );
        }
        getLogger().info( "RestApplication successfully created." );
    }

    @Override
    public synchronized void stop()
        throws Exception
    {
        super.stop();
        zestApplication.passivate();
    }

    @Override
    public Restlet createInboundRoot()
    {
        Context context = getContext();
        Engine.getInstance().getRegisteredConverters().add( new ZestConverter( objectFactory ) );

        if( zestApplication.mode() == Application.Mode.development )
        {
            setDebugging( true );
        }
        router = new Router( context );

        addRoutes( router );
        router.attach( "/", newZestRestlet( EntryPointResource.class, EntryPoint.class ) );

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
                                    Class<? extends Identity> type,
                                    String basePath
    )
    {
        addResourcePath( name, type, basePath, true, true );
    }

    protected void addResourcePath( String name,
                                    Class<? extends Identity> type,
                                    String basePath,
                                    boolean createLink,
                                    boolean rootRoute
    )
    {
        if( createLink )
        {
            router.attach( basePath + name + "/create", newZestRestlet( CreationResource.class, type ) );
        }
        TemplateRoute route = router.attach( basePath + name + "/", newZestRestlet( EntityListResource.class, type ) );
        if( rootRoute )
        {
            route.setName( name );
        }
        router.attach( basePath + name + "/{id}/", newZestRestlet( EntityResource.class, type ) );
        router.attach( basePath + name + "/{id}/{invoke}", newZestRestlet( EntityResource.class, type ) );
    }

    private <K extends Identity, T extends ServerResource<K>> Restlet newZestRestlet( Class<T> resourceClass,
                                                                                      Class<K> entityClass
    )
    {

        @SuppressWarnings( "unchecked" )
        ResourceFactory<K, T> factory = objectFactory.newObject( DefaultResourceFactoryImpl.class,
                                                                 resourceClass, router
        );
        ZestConverter converter = new ZestConverter( objectFactory );
        return objectFactory.newObject( ZestEntityRestlet.class,
                                        factory,
                                        router,
                                        entityClass,
                                        converter
        );
    }
}
