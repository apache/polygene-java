/*
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

package org.qi4j.library.eventsourcing.application.replay;

import java.lang.reflect.Method;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.qi4j.api.Qi4j;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.structure.Module;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.api.value.ValueComposite;
import org.qi4j.library.eventsourcing.application.api.ApplicationEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ApplicationEvent player
 */
@Mixins(ApplicationEventPlayerService.Mixin.class)
public interface ApplicationEventPlayerService
        extends ApplicationEventPlayer, ServiceComposite
{
    class Mixin
            implements ApplicationEventPlayer
    {
        final Logger logger = LoggerFactory.getLogger( ApplicationEventPlayer.class );
        @Structure
        UnitOfWorkFactory uowf;

        @Structure
        Module module;

        @Structure
        Qi4j api;

        SimpleDateFormat dateFormat = new SimpleDateFormat( "EEE MMM dd HH:mm:ss zzz yyyy" );

        @Override
        public void playEvent( ApplicationEvent applicationEvent, Object object )
                throws ApplicationEventReplayException
        {
            UnitOfWork uow = uowf.currentUnitOfWork();
            Class handlerType = object.getClass();

            // Get method
            Method eventMethod = getEventMethod( handlerType, applicationEvent.name().get() );

            if (eventMethod == null)
            {
                logger.warn( "Could not find event method " + applicationEvent.name().get() + " in entity of type " + handlerType.getName() );
                return;
            }

            // Build parameters
            try
            {
                String jsonParameters = applicationEvent.parameters().get();
                JSONObject parameters = (JSONObject) new JSONTokener( jsonParameters ).nextValue();
                Object[] args = new Object[eventMethod.getParameterTypes().length];
                for (int i = 1; i < eventMethod.getParameterTypes().length; i++)
                {
                    Class<?> parameterType = eventMethod.getParameterTypes()[i];

                    String paramName = "param" + i;

                    Object value = parameters.get( paramName );

                    args[i] = getParameterArgument( parameterType, value, uow );
                }

                args[0] = applicationEvent;

                // Invoke method
                logger.debug( "Replay:" + applicationEvent + " on:" + object );

                eventMethod.invoke( object, args );
            } catch (Exception e)
            {
                throw new ApplicationEventReplayException( applicationEvent, e );
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
                    if (parameterTypes.length > 0 && parameterTypes[0].equals( ApplicationEvent.class ))
                        return method;
                }
            }
            return null;
        }
    }
}