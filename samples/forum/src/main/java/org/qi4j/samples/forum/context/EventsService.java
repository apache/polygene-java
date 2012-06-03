package org.qi4j.samples.forum.context;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.structure.Application;
import org.qi4j.api.structure.Module;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkCallback;
import org.qi4j.api.unitofwork.UnitOfWorkCompletionException;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.functional.Function;
import org.qi4j.functional.Iterables;
import org.qi4j.library.rest.server.api.ObjectSelection;
import org.qi4j.samples.forum.domainevent.DomainEventValue;
import org.qi4j.samples.forum.domainevent.ParameterValue;
import org.restlet.Request;

/**
 * TODO
 */
@Mixins( EventsService.Mixin.class )
public interface EventsService
    extends Events, ServiceComposite
{
    public class Mixin
        implements InvocationHandler
    {
        @Structure
        Module module;
        @Structure
        Application application;

        @Override
        public Object invoke( Object proxy, Method method, Object[] args )
            throws Throwable
        {
            UnitOfWork unitOfWork = module.currentUnitOfWork();

            ValueBuilder<DomainEventValue> builder = module.newValueBuilder( DomainEventValue.class );
            DomainEventValue prototype = builder.prototype();
            prototype.version().set( application.version() );
            prototype.timestamp().set( unitOfWork.currentTime() );
            prototype.context().set( proxy.getClass().getSuperclass().getName().split( "\\$" )[ 0 ] );
            prototype.name().set( method.getName() );

            int idx = 0;
            for( Object arg : args )
            {
                idx++;
                String name = "param" + idx;
                ValueBuilder<ParameterValue> parameterBuilder = module.newValueBuilder( ParameterValue.class );
                parameterBuilder.prototype().name().set( name );
                parameterBuilder.prototype().value().set( arg );
                prototype.parameters().get().add( parameterBuilder.newInstance() );
            }

            Iterables.addAll( prototype.selection().get(), Iterables.map( new Function<Object, String>()
            {
                @Override
                public String map( Object o )
                {
                    return o.toString();
                }
            }, ObjectSelection.current().selection() ) );

            final DomainEventValue domainEvent = builder.newInstance();

            // Send event to all interested parties in the ObjectSelection
            for( Object selectedObject : ObjectSelection.current().selection() )
            {
                if( selectedObject instanceof Events )
                {
                    method.invoke( selectedObject, args );
                }
            }

            unitOfWork.addUnitOfWorkCallback( new UnitOfWorkCallback()
            {
                @Override
                public void beforeCompletion()
                    throws UnitOfWorkCompletionException
                {
                }

                @Override
                public void afterCompletion( UnitOfWorkStatus status )
                {
                    if( status.equals( UnitOfWorkStatus.COMPLETED ) )
                    {
                        Request.getCurrent().getAttributes().put( "event", domainEvent );
                    }
                }
            } );

            return null;
        }
    }
}
