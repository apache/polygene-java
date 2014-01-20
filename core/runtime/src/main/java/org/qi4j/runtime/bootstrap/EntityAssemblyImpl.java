/*
 * Copyright (c) 2007-2011, Rickard Öberg. All Rights Reserved.
 * Copyright (c) 2014, Paul Merlin. All Rights Reserved.
 *
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */
package org.qi4j.runtime.bootstrap;

import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Member;
import org.qi4j.api.association.Association;
import org.qi4j.api.association.GenericAssociationInfo;
import org.qi4j.api.association.ManyAssociation;
import org.qi4j.api.association.NamedAssociation;
import org.qi4j.api.common.InvalidApplicationException;
import org.qi4j.api.common.MetaInfo;
import org.qi4j.api.common.Optional;
import org.qi4j.api.common.QualifiedName;
import org.qi4j.api.common.UseDefaults;
import org.qi4j.api.constraint.Constraint;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.property.GenericPropertyInfo;
import org.qi4j.api.property.Immutable;
import org.qi4j.api.property.Property;
import org.qi4j.api.util.Annotations;
import org.qi4j.api.util.Classes;
import org.qi4j.bootstrap.AssociationDeclarations;
import org.qi4j.bootstrap.EntityAssembly;
import org.qi4j.bootstrap.ManyAssociationDeclarations;
import org.qi4j.bootstrap.NamedAssociationDeclarations;
import org.qi4j.bootstrap.StateDeclarations;
import org.qi4j.runtime.association.AssociationModel;
import org.qi4j.runtime.association.AssociationsModel;
import org.qi4j.runtime.association.ManyAssociationModel;
import org.qi4j.runtime.association.ManyAssociationsModel;
import org.qi4j.runtime.association.NamedAssociationModel;
import org.qi4j.runtime.association.NamedAssociationsModel;
import org.qi4j.runtime.composite.MixinsModel;
import org.qi4j.runtime.composite.StateModel;
import org.qi4j.runtime.composite.ValueConstraintsInstance;
import org.qi4j.runtime.composite.ValueConstraintsModel;
import org.qi4j.runtime.entity.EntityMixinsModel;
import org.qi4j.runtime.entity.EntityModel;
import org.qi4j.runtime.entity.EntityStateModel;
import org.qi4j.runtime.property.PropertyModel;

import static org.qi4j.api.util.Annotations.isType;
import static org.qi4j.api.util.Classes.typeOf;
import static org.qi4j.functional.Iterables.filter;
import static org.qi4j.functional.Iterables.first;

/**
 * Declaration of a EntityComposite.
 */
public final class EntityAssemblyImpl
    extends CompositeAssemblyImpl
    implements EntityAssembly
{
    private AssociationDeclarations associationDeclarations;
    private ManyAssociationDeclarations manyAssociationDeclarations;
    private NamedAssociationDeclarations namedAssociationDeclarations;
    private AssociationsModel associationsModel;
    private ManyAssociationsModel manyAssociationsModel;
    private NamedAssociationsModel namedAssociationsModel;

    public EntityAssemblyImpl( Class<?> entityType )
    {
        super( entityType );
        // The composite must always implement EntityComposite, as a marker interface
        if( !EntityComposite.class.isAssignableFrom( entityType ) )
        {
            types.add( EntityComposite.class );
        }
    }

    @Override
    protected MixinsModel createMixinsModel()
    {
        return new EntityMixinsModel();
    }

    @Override
    protected StateModel createStateModel()
    {
        return new EntityStateModel( propertiesModel, associationsModel, manyAssociationsModel, namedAssociationsModel );
    }

    EntityModel newEntityModel(
        StateDeclarations stateDeclarations,
        AssociationDeclarations associationDecs,
        ManyAssociationDeclarations manyAssociationDecs,
        NamedAssociationDeclarations namedAssociationDecs,
        AssemblyHelper helper
    )
    {
        this.associationDeclarations = associationDecs;
        this.manyAssociationDeclarations = manyAssociationDecs;
        this.namedAssociationDeclarations = namedAssociationDecs;
        try
        {
            associationsModel = new AssociationsModel();
            manyAssociationsModel = new ManyAssociationsModel();
            namedAssociationsModel = new NamedAssociationsModel();
            buildComposite( helper, stateDeclarations );

            EntityModel entityModel = new EntityModel(
                types, visibility, metaInfo, (EntityMixinsModel) mixinsModel, (EntityStateModel) stateModel, compositeMethodsModel );

            return entityModel;
        }
        catch( Exception e )
        {
            throw new InvalidApplicationException( "Could not register " + types, e );
        }
    }

    @Override
    protected void addStateFor( AccessibleObject accessor,
                                Iterable<Class<? extends Constraint<?, ?>>> constraintClasses
    )
    {
        String stateName = QualifiedName.fromAccessor( accessor ).name();

        if( registeredStateNames.contains( stateName ) )
        {
            return; // Skip already registered names
        }

        Class<?> accessorType = Classes.RAW_CLASS.map( typeOf( accessor ) );
        if( Property.class.isAssignableFrom( accessorType ) )
        {
            propertiesModel.addProperty( newPropertyModel( accessor, constraintClasses ) );
            registeredStateNames.add( stateName );
        }
        else if( Association.class.isAssignableFrom( accessorType ) )
        {
            associationsModel.addAssociation( newAssociationModel( accessor, constraintClasses ) );
            registeredStateNames.add( stateName );
        }
        else if( ManyAssociation.class.isAssignableFrom( accessorType ) )
        {
            manyAssociationsModel.addManyAssociation( newManyAssociationModel( accessor, constraintClasses ) );
            registeredStateNames.add( stateName );
        }
        else if( NamedAssociation.class.isAssignableFrom( accessorType ) )
        {
            namedAssociationsModel.addNamedAssociation( newNamedAssociationModel( accessor, constraintClasses ) );
            registeredStateNames.add( stateName );
        }
    }

    @Override
    protected PropertyModel newPropertyModel( AccessibleObject accessor,
                                              Iterable<Class<? extends Constraint<?, ?>>> constraintClasses
    )
    {
        Iterable<Annotation> annotations = Annotations.findAccessorAndTypeAnnotationsIn( accessor );
        boolean optional = first( filter( isType( Optional.class ), annotations ) ) != null;
        ValueConstraintsModel valueConstraintsModel = constraintsFor( annotations, GenericPropertyInfo.propertyTypeOf( accessor ), ( (Member) accessor )
            .getName(), optional, constraintClasses, accessor );
        ValueConstraintsInstance valueConstraintsInstance = null;
        if( valueConstraintsModel.isConstrained() )
        {
            valueConstraintsInstance = valueConstraintsModel.newInstance();
        }
        MetaInfo metaInfo = stateDeclarations.metaInfoFor( accessor );
        Object defaultValue = stateDeclarations.initialValueOf( accessor );
        boolean useDefaults = metaInfo.get( UseDefaults.class ) != null || stateDeclarations.useDefaults( accessor );
        boolean immutable = this.immutable || metaInfo.get( Immutable.class ) != null;
        PropertyModel propertyModel = new PropertyModel( accessor, immutable, useDefaults, valueConstraintsInstance, metaInfo, defaultValue );
        return propertyModel;
    }

    public AssociationModel newAssociationModel( AccessibleObject accessor,
                                                 Iterable<Class<? extends Constraint<?, ?>>> constraintClasses
    )
    {
        Iterable<Annotation> annotations = Annotations.findAccessorAndTypeAnnotationsIn( accessor );
        boolean optional = first( filter( isType( Optional.class ), annotations ) ) != null;

        // Constraints for Association references
        ValueConstraintsModel valueConstraintsModel = constraintsFor( annotations, GenericAssociationInfo
            .associationTypeOf( accessor ), ( (Member) accessor ).getName(), optional, constraintClasses, accessor );
        ValueConstraintsInstance valueConstraintsInstance = null;
        if( valueConstraintsModel.isConstrained() )
        {
            valueConstraintsInstance = valueConstraintsModel.newInstance();
        }

        // Constraints for the Association itself
        valueConstraintsModel = constraintsFor( annotations, Association.class, ( (Member) accessor ).getName(), optional, constraintClasses, accessor );
        ValueConstraintsInstance associationValueConstraintsInstance = null;
        if( valueConstraintsModel.isConstrained() )
        {
            associationValueConstraintsInstance = valueConstraintsModel.newInstance();
        }

        MetaInfo metaInfo = associationDeclarations.metaInfoFor( accessor );
        AssociationModel associationModel = new AssociationModel( accessor, valueConstraintsInstance, associationValueConstraintsInstance, metaInfo );
        return associationModel;
    }

    public ManyAssociationModel newManyAssociationModel( AccessibleObject accessor,
                                                         Iterable<Class<? extends Constraint<?, ?>>> constraintClasses
    )
    {
        Iterable<Annotation> annotations = Annotations.findAccessorAndTypeAnnotationsIn( accessor );
        boolean optional = first( filter( isType( Optional.class ), annotations ) ) != null;

        // Constraints for entities in ManyAssociation
        ValueConstraintsModel valueConstraintsModel = constraintsFor( annotations, GenericAssociationInfo
            .associationTypeOf( accessor ), ( (Member) accessor ).getName(), optional, constraintClasses, accessor );
        ValueConstraintsInstance valueConstraintsInstance = null;
        if( valueConstraintsModel.isConstrained() )
        {
            valueConstraintsInstance = valueConstraintsModel.newInstance();
        }

        // Constraints for the ManyAssociation itself
        valueConstraintsModel = constraintsFor( annotations, ManyAssociation.class, ( (Member) accessor ).getName(), optional, constraintClasses, accessor );
        ValueConstraintsInstance manyValueConstraintsInstance = null;
        if( valueConstraintsModel.isConstrained() )
        {
            manyValueConstraintsInstance = valueConstraintsModel.newInstance();
        }
        MetaInfo metaInfo = manyAssociationDeclarations.metaInfoFor( accessor );
        ManyAssociationModel associationModel = new ManyAssociationModel( accessor, valueConstraintsInstance, manyValueConstraintsInstance, metaInfo );
        return associationModel;
    }

    public NamedAssociationModel newNamedAssociationModel( AccessibleObject accessor,
                                                           Iterable<Class<? extends Constraint<?, ?>>> constraintClasses
    )
    {
        Iterable<Annotation> annotations = Annotations.findAccessorAndTypeAnnotationsIn( accessor );
        boolean optional = first( filter( isType( Optional.class ), annotations ) ) != null;

        // Constraints for entities in NamedAssociation
        ValueConstraintsModel valueConstraintsModel = constraintsFor( annotations, GenericAssociationInfo
            .associationTypeOf( accessor ), ( (Member) accessor ).getName(), optional, constraintClasses, accessor );
        ValueConstraintsInstance valueConstraintsInstance = null;
        if( valueConstraintsModel.isConstrained() )
        {
            valueConstraintsInstance = valueConstraintsModel.newInstance();
        }

        // Constraints for the NamedAssociation itself
        valueConstraintsModel = constraintsFor( annotations, NamedAssociation.class, ( (Member) accessor ).getName(), optional, constraintClasses, accessor );
        ValueConstraintsInstance namedValueConstraintsInstance = null;
        if( valueConstraintsModel.isConstrained() )
        {
            namedValueConstraintsInstance = valueConstraintsModel.newInstance();
        }
        MetaInfo metaInfo = namedAssociationDeclarations.metaInfoFor( accessor );
        NamedAssociationModel associationModel = new NamedAssociationModel( accessor, valueConstraintsInstance, namedValueConstraintsInstance, metaInfo );
        return associationModel;
    }
}
