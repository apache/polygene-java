/*
 * Copyright 1996-2011 Niclas Hedhman.
 *
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.qi4j.library.alarm;

import java.util.Date;
import java.util.Locale;
import java.util.ResourceBundle;
import org.qi4j.api.common.Optional;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Initializable;
import org.qi4j.api.mixin.InitializationException;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;
import org.qi4j.api.value.ValueComposite;

/**
 * Status of an AlarmPoint.
 *
 * @author Niclas Hedhman
 */
@Mixins( AlarmStatus.AlarmStatusMixin.class )
public interface AlarmStatus extends ValueComposite
{

    /**
     * Returns the Name of the AlarmStatus in a locale.
     * This is the technical name of the AlarmStatus, such as Normal,
     * Activated and so forth in a locale specific form.
     *
     * @param locale the locale to return the name in.
     *
     * @return the name of the AlarmStatus in the given locale.
     *
     * @see State#name()
     */
    String name( @Optional Locale locale );

    interface State
    {

        /**
         * Returns the Date/Time of when this state was created.
         *
         * @return the timestamp of when the state was created.
         */
        @Optional
        Property<Date> creationDate();

        /**
         * Returns the Name of the AlarmStatus.
         * This is the technical name of the AlarmStatus, such as Normal,
         * Activated and so forth in non-locale specific form.
         *
         * @return the name of the AlarmStatus in the default locale.
         *
         * @see #name(Locale)
         */
        Property<String> name();
    }

    abstract class AlarmStatusMixin
        implements AlarmStatus, Initializable
    {
        @This
        private State state;

        @Override
        public String name( Locale locale )
        {
            if( locale == null )
            {
                return state.name().get();
            }
            ResourceBundle bundle = ResourceBundle.getBundle( "AlarmResources", locale );
            if( bundle == null )
            {
                return state.name().get();
            }
            return bundle.getString( state.name().get() );
        }

        @Override
        public void initialize()
            throws InitializationException
        {
            if( state.creationDate().get() == null )
            {
                state.creationDate().set( new Date() );
            }
        }
    }
}
