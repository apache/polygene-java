/*
 * Copyright (c) 2009, Rickard Öberg. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.qi4j.migration;

import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * MigrationEvents implementation that logs the events.
 */
public class MigrationEventLogger
    implements MigrationEvents
{
    protected Logger logger = LoggerFactory.getLogger( getClass().getName() );

    public void propertyAdded( String entity, String name, Object value )
    {
        logger.info( "Added property " + name + " with value " + (value==null?"null":value.toString()) + " in " + entity );
    }

    public void propertyRemoved( String entity, String name )
    {
        logger.info( "Removed property " + name + " in " + entity );
    }

    public void propertyRenamed( String entity, String from, String to )
    {
        logger.info( "Renamed property from " + from + " to " + to + " in " + entity );
    }

    public void associationAdded( String entity, String name, String value )
    {
        logger.info( "Added association " + name + " with value " + value + " in " + entity );
    }

    public void associationRemoved( String entity, String name )
    {
        logger.info( "Removed association " + name + " in " + entity );
    }

    public void associationRenamed( String entity, String from, String to )
    {
        logger.info( "Renamed association from " + from + " to " + to + " in " + entity );
    }

    public void manyAssociationAdded( String entity, String name, String... value )
    {
        logger.info( "Added many-association " + name + " with values " + Arrays.asList( value ) + " in " + entity );
    }

    public void manyAssociationRemoved( String entity, String name )
    {
        logger.info( "Removed many-association " + name + " in " + entity );
    }

    public void manyAssociationRenamed( String entity, String from, String to )
    {
        logger.info( "Renamed many-association from " + from + " to " + to + " in " + entity );
    }

    public void entityTypeChanged( String entity, String newEntityType )
    {
        logger.info( "Changed entitytype to " + newEntityType + " in " + entity );
    }
}
