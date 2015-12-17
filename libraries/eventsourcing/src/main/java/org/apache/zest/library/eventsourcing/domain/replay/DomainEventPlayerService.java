/**
 *
 * Copyright 2009-2010 Rickard Öberg AB
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

package org.apache.zest.library.eventsourcing.domain.replay;

import java.lang.reflect.Method;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.apache.zest.api.entity.EntityComposite;
import org.apache.zest.api.injection.scope.Structure;
import org.apache.zest.api.mixin.Mixins;
import org.apache.zest.api.service.ServiceComposite;
import org.apache.zest.api.structure.Module;
import org.apache.zest.api.unitofwork.NoSuchEntityException;
import org.apache.zest.api.unitofwork.UnitOfWork;
import org.apache.zest.api.unitofwork.UnitOfWorkFactory;
import org.apache.zest.api.usecase.UsecaseBuilder;
import org.apache.zest.api.value.ValueComposite;
import org.apache.zest.library.eventsourcing.domain.api.DomainEventValue;
import org.apache.zest.library.eventsourcing.domain.api.UnitOfWorkDomainEventsValue;
import org.apache.zest.spi.ZestSPI;
import org.apache.zest.spi.entity.EntityState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DomainEventValue player
 */
@Mixins(DomainEventPlayerService.Mixin.class)
public interface DomainEventPlayerService
        extends DomainEventPlayer, ServiceComposite
{
    class Mixin
            implements DomainEventPlayer
    {
        final Logger logger = LoggerFactory.getLogger( DomainEventPlayer.class );

        @Structure
        UnitOfWorkFactory uowf;

        @Structure
        Module module;

        @Structure
        ZestSPI spi;

        SimpleDateFormat dateFormat = new SimpleDateFormat( "EEE MMM dd HH:mm:ss zzz yyyy" );

        @Override
        public void playTransaction( UnitOfWorkDomainEventsValue unitOfWorkDomainValue )
                throws EventReplayException
        {
            UnitOfWork uow = uowf.newUnitOfWork( UsecaseBuilder.newUsecase( "Event replay" ) );
            DomainEventValue currentEventValue = null;
            try
            {
                for (DomainEventValue domainEventValue : unitOfWorkDomainValue.events().get())
                {
                    currentEventValue = domainEventValue;
                    // Get the entity
                    Class entityType = module.descriptor().classLoader().loadClass( domainEventValue.entityType().get() );
                    String id = domainEventValue.entityId().get();
                    Object entity = null;
                    try
                    {
                        entity = uow.get( entityType, id );
                    } catch( NoSuchEntityException e )
                    {
                        // Event to play for an entity that doesn't yet exist - create a default instance
                        entity = uow.newEntity( entityType, id );
                    }

                    // check if the event has already occured
                    EntityState state = spi.entityStateOf( (EntityComposite) entity );
                    if (state.lastModified() > unitOfWorkDomainValue.timestamp().get())
                    {
                        break; // don't rerun event in this unitOfWorkDomainValue
                    }

                    playEvent( domainEventValue, entity );
                }
                uow.complete();
            } catch (Exception e)
            {
                uow.discard();
                if (e instanceof EventReplayException)
                    throw ((EventReplayException) e);
                else
                    throw new EventReplayException( currentEventValue, e );
            }
        }

        @Override
        public void playEvent( DomainEventValue domainEventValue, Object object )
                throws EventReplayException
        {
            UnitOfWork uow = uowf.currentUnitOfWork();
            Class entityType = object.getClass();

            // Get method
            Method eventMethod = getEventMethod( entityType, domainEventValue.name().get() );

            if (eventMethod == null)
            {
                logger.warn( "Could not find event method " + domainEventValue.name().get() + " in entity of type " + entityType.getName() );
                return;
            }

            // Build parameters
            try
            {
                String jsonParameters = domainEventValue.parameters().get();
                JSONObject parameters = (JSONObject) new JSONTokener( jsonParameters ).nextValue();
                Object[] args = new Object[eventMethod.getParameterTypes().length];
                for (int i = 1; i < eventMethod.getParameterTypes().length; i++)
                {
                    Class<?> parameterType = eventMethod.getParameterTypes()[i];

                    String paramName = "param" + i;

                    Object value = parameters.get( paramName );

                    args[i] = getParameterArgument( parameterType, value, uow );
                }

                args[0] = domainEventValue;

                // Invoke method
                logger.debug( "Replay:" + domainEventValue + " on:" + object );

                eventMethod.invoke( object, args );
            } catch (Exception e)
            {
                throw new EventReplayException( domainEventValue, e );
            }
        }

        private Object getParameterArgument( Class<?> parameterType, Object value, UnitOfWork uow ) throws ParseException
        {
            if (value.equals( JSONObject.NULL ))
                return null;

            if (parameterType.equals( String.class ))
            {
                return (String) value;
            } else if (parameterType.equals( Boolean.class ) || parameterType.equals( Boolean.TYPE ))
            {
                return (Boolean) value;
            } else if (parameterType.equals( Long.class ) || parameterType.equals( Long.TYPE ))
            {
                return ((Number) value).longValue();
            } else if (parameterType.equals( Integer.class ) || parameterType.equals( Integer.TYPE ))
            {
                return ((Number) value).intValue();
            } else if (parameterType.equals( Date.class ))
            {
                return dateFormat.parse( (String) value );
            } else if (ValueComposite.class.isAssignableFrom( parameterType ))
            {
                return module.newValueFromSerializedState( parameterType, (String) value );
            } else if (parameterType.isInterface())
            {
                return uow.get( parameterType, (String) value );
            } else if (parameterType.isEnum())
            {
                return Enum.valueOf( (Class<? extends Enum>) parameterType, value.toString() );
            } else
            {
                throw new IllegalArgumentException( "Unknown parameter type:" + parameterType.getName() );
            }
        }

        private Method getEventMethod( Class<?> aClass, String eventName )
        {
            for (Method method : aClass.getMethods())
            {
                if (method.getName().equals( eventName ))
                {
                    Class[] parameterTypes = method.getParameterTypes();
                    if (parameterTypes.length > 0 && parameterTypes[0].equals( DomainEventValue.class ))
                        return method;
                }
            }
            return null;
        }
    }
}