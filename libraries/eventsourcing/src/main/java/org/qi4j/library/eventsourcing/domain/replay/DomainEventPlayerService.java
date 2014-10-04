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

package org.qi4j.library.eventsourcing.domain.replay;

import java.lang.reflect.Method;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.MonthDay;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.Period;
import java.time.Year;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.structure.Module;
import org.qi4j.api.unitofwork.NoSuchEntityException;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.api.usecase.UsecaseBuilder;
import org.qi4j.api.value.ValueComposite;
import org.qi4j.library.eventsourcing.domain.api.DomainEventValue;
import org.qi4j.library.eventsourcing.domain.api.UnitOfWorkDomainEventsValue;
import org.qi4j.spi.Qi4jSPI;
import org.qi4j.spi.entity.EntityState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DomainEventValue player
 */
@Mixins( DomainEventPlayerService.Mixin.class )
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
        Qi4jSPI spi;

        @Override
        public void playTransaction( UnitOfWorkDomainEventsValue unitOfWorkDomainValue )
            throws EventReplayException
        {
            UnitOfWork uow = uowf.newUnitOfWork( UsecaseBuilder.newUsecase( "Event replay" ) );
            DomainEventValue currentEventValue = null;
            try
            {
                for( DomainEventValue domainEventValue : unitOfWorkDomainValue.events().get() )
                {
                    currentEventValue = domainEventValue;
                    // Get the entity
                    Class entityType = module.classLoader().loadClass( domainEventValue.entityType().get() );
                    String id = domainEventValue.entityId().get();
                    Object entity = null;
                    try
                    {
                        entity = uow.get( entityType, id );
                    }
                    catch( NoSuchEntityException e )
                    {
                        // Event to play for an entity that doesn't yet exist - create a default instance
                        entity = uow.newEntity( entityType, id );
                    }

                    // check if the event has already occured
                    EntityState state = spi.entityStateOf( (EntityComposite) entity );
                    if( state.lastModified().isAfter( unitOfWorkDomainValue.timestamp().get() ) )
                    {
                        break; // don't rerun event in this unitOfWorkDomainValue
                    }

                    playEvent( domainEventValue, entity );
                }
                uow.complete();
            }
            catch( Exception e )
            {
                uow.discard();
                if( e instanceof EventReplayException )
                {
                    throw ( (EventReplayException) e );
                }
                else
                {
                    throw new EventReplayException( currentEventValue, e );
                }
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

            if( eventMethod == null )
            {
                logger.warn( "Could not find event method " + domainEventValue.name()
                    .get() + " in entity of type " + entityType.getName() );
                return;
            }

            // Build parameters
            try
            {
                String jsonParameters = domainEventValue.parameters().get();
                JSONObject parameters = (JSONObject) new JSONTokener( jsonParameters ).nextValue();
                Object[] args = new Object[ eventMethod.getParameterTypes().length ];
                for( int i = 1; i < eventMethod.getParameterTypes().length; i++ )
                {
                    Class<?> parameterType = eventMethod.getParameterTypes()[ i ];

                    String paramName = "param" + i;

                    Object value = parameters.get( paramName );

                    args[ i ] = getParameterArgument( parameterType, value, uow );
                }

                args[ 0 ] = domainEventValue;

                // Invoke method
                logger.debug( "Replay:" + domainEventValue + " on:" + object );

                eventMethod.invoke( object, args );
            }
            catch( Exception e )
            {
                throw new EventReplayException( domainEventValue, e );
            }
        }

        private Object getParameterArgument( Class<?> parameterType, Object value, UnitOfWork uow )
        {
            if( value.equals( JSONObject.NULL ) )
            {
                return null;
            }

            if( parameterType.equals( String.class ) )
            {
                return (String) value;
            }
            else if( parameterType.equals( Boolean.class ) || parameterType.equals( Boolean.TYPE ) )
            {
                return (Boolean) value;
            }
            else if( parameterType.equals( Long.class ) || parameterType.equals( Long.TYPE ) )
            {
                return ( (Number) value ).longValue();
            }
            else if( parameterType.equals( Integer.class ) || parameterType.equals( Integer.TYPE ) )
            {
                return ( (Number) value ).intValue();
            }
            else if( parameterType.equals( LocalDate.class ) )
            {
                return LocalDate.parse( (String) value );
            }
            else if( parameterType.equals( LocalDateTime.class ) )
            {
                return LocalDateTime.parse( (String) value );
            }
            else if( parameterType.equals( LocalTime.class ) )
            {
                return LocalTime.parse( (String) value );
            }
            else if( parameterType.equals( OffsetTime.class ) )
            {
                return OffsetTime.parse( (String) value );
            }
            else if( parameterType.equals( OffsetDateTime.class ) )
            {
                return OffsetDateTime.parse( (String) value );
            }
            else if( parameterType.equals( ZonedDateTime.class ) )
            {
                return ZonedDateTime.parse( (String) value );
            }
            else if( parameterType.equals( Instant.class ) )
            {
                return Instant.parse( (String) value );
            }
            else if( parameterType.equals( Period.class ) )
            {
                return Period.parse( (String) value );
            }
            else if( parameterType.equals( Duration.class ) )
            {
                return Duration.parse( (String) value );
            }
            else if( parameterType.equals( MonthDay.class ) )
            {
                return MonthDay.parse( (String) value );
            }
            else if( parameterType.equals( YearMonth.class ) )
            {
                return YearMonth.parse( (String) value );
            }
            else if( parameterType.equals( Year.class ) )
            {
                return Year.parse( (String) value );
            }
            else if( parameterType.equals( ZoneId.class ) )
            {
                return ZoneId.of( (String) value );
            }
            else if( parameterType.equals( ZoneOffset.class ) )
            {
                return ZoneOffset.of( (String) value );
            }
            else if( ValueComposite.class.isAssignableFrom( parameterType ) )
            {
                return module.newValueFromSerializedState( parameterType, (String) value );
            }
            else if( parameterType.isInterface() )
            {
                return uow.get( parameterType, (String) value );
            }
            else if( parameterType.isEnum() )
            {
                return Enum.valueOf( (Class<? extends Enum>) parameterType, value.toString() );
            }
            else
            {
                throw new IllegalArgumentException( "Unknown parameter type:" + parameterType.getName() );
            }
        }

        private Method getEventMethod( Class<?> aClass, String eventName )
        {
            for( Method method : aClass.getMethods() )
            {
                if( method.getName().equals( eventName ) )
                {
                    Class[] parameterTypes = method.getParameterTypes();
                    if( parameterTypes.length > 0 && parameterTypes[ 0 ].equals( DomainEventValue.class ) )
                    {
                        return method;
                    }
                }
            }
            return null;
        }
    }
}