/*
 * Copyright (c) 2008, Rickard Öberg. All Rights Reserved.
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

package org.qi4j.runtime.entity.association;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.qi4j.entity.association.AbstractAssociation;
import org.qi4j.runtime.composite.ConstraintsModel;
import org.qi4j.runtime.composite.ValueConstraintsInstance;
import org.qi4j.runtime.composite.ValueConstraintsModel;
import org.qi4j.runtime.entity.UnitOfWorkInstance;
import org.qi4j.spi.entity.EntityState;
import org.qi4j.spi.entity.association.AssociationDescriptor;
import org.qi4j.util.MetaInfo;

/**
 * TODO
 */
public class AssociationsModel
{
    private Set<Class> mixinTypes = new HashSet<Class>();
    private List<AssociationModel> associationModels = new ArrayList<AssociationModel>();
    private Map<Method, AssociationModel> mapMethodAssociationModel = new HashMap<Method, AssociationModel>();
    private Map<String, Method> accessors = new HashMap<String, Method>();
    private ConstraintsModel constraints;

    public AssociationsModel( ConstraintsModel constraints )
    {
        this.constraints = constraints;
    }

    public void addAssociationsFor( Class mixinType )
    {
        if( !mixinTypes.contains( mixinType ) )
        {
            for( Method method : mixinType.getMethods() )
            {
                if( AbstractAssociation.class.isAssignableFrom( method.getReturnType() ) )
                {
                    ValueConstraintsModel valueConstraintsModel = constraints.constraintsFor( method.getAnnotations(), AbstractAssociationInstance.getAssociationType( method ) );
                    ValueConstraintsInstance valueConstraintsInstance = null;
                    if( valueConstraintsModel.isConstrained() )
                    {
                        valueConstraintsInstance = valueConstraintsModel.newInstance( method );
                    }
                    AssociationModel associationModel = new AssociationModel( method, valueConstraintsInstance, new MetaInfo() ); //TODO Take default value from assembly
                    associationModels.add( associationModel );
                    mapMethodAssociationModel.put( method, associationModel );
                    accessors.put( associationModel.qualifiedName(), associationModel.accessor() );
                }
            }
        }
    }

    public List<AssociationDescriptor> associations()
    {
        return new ArrayList<AssociationDescriptor>( associationModels );
    }

    public AssociationsInstance newInstance( UnitOfWorkInstance uow, EntityState state )
    {
        Map<Method, AbstractAssociation> associations = new HashMap<Method, AbstractAssociation>();
        for( AssociationModel associationModel : associationModels )
        {
            AbstractAssociation association = associationModel.newInstance( uow, state );
            associations.put( associationModel.accessor(), association );
        }

        return new AssociationsInstance( this, uow, state );
    }

    public Method accessorFor( String qualifiedName )
    {
        return accessors.get( qualifiedName );
    }

    public AssociationsInstance newDefaultInstance()
    {
        return new AssociationsInstance( this, null, null );
    }

    public AbstractAssociation newDefaultInstance( Method accessor )
    {
        return mapMethodAssociationModel.get( accessor ).newDefaultInstance();
    }

    public AbstractAssociation newInstance( Method accessor, EntityState entityState, UnitOfWorkInstance uow )
    {
        return mapMethodAssociationModel.get( accessor ).newInstance( uow, entityState );
    }

    public AssociationDescriptor getAssociationByName( String name )
    {
        for( AssociationModel associationModel : associationModels )
        {
            if( associationModel.name().equals( name ) )
            {
                return associationModel;
            }
        }

        return null;
    }

    public AssociationDescriptor getAssociationByQualifiedName( String name )
    {
        for( AssociationModel associationModel : associationModels )
        {
            if( associationModel.qualifiedName().equals( name ) )
            {
                return associationModel;
            }
        }

        return null;
    }

    public void setState( AssociationsInstance associations, EntityState entityState )
    {
        for( Map.Entry<Method, AssociationModel> methodAssociationModelEntry : mapMethodAssociationModel.entrySet() )
        {
            AbstractAssociation association = associations.associationFor( methodAssociationModelEntry.getKey() );
            methodAssociationModelEntry.getValue().setState( association, entityState );
        }
    }

}
