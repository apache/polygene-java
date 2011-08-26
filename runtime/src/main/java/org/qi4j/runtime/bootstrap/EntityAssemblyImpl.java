/*
 * Copyright (c) 2007, Rickard Öberg. All Rights Reserved.
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

package org.qi4j.runtime.bootstrap;

import org.qi4j.api.association.Association;
import org.qi4j.api.association.GenericAssociationInfo;
import org.qi4j.api.association.ManyAssociation;
import org.qi4j.api.common.*;
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
import org.qi4j.bootstrap.StateDeclarations;
import org.qi4j.functional.Iterables;
import org.qi4j.runtime.composite.*;
import org.qi4j.runtime.entity.EntityMixinsModel;
import org.qi4j.runtime.entity.EntityModel;
import org.qi4j.runtime.entity.EntityStateModel;
import org.qi4j.runtime.association.AssociationModel;
import org.qi4j.runtime.association.AssociationsModel;
import org.qi4j.runtime.association.ManyAssociationModel;
import org.qi4j.runtime.association.ManyAssociationsModel;
import org.qi4j.runtime.property.PropertiesModel;
import org.qi4j.runtime.property.PropertyModel;

import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Member;
import java.util.ArrayList;
import java.util.List;

import static org.qi4j.api.util.Annotations.isType;
import static org.qi4j.functional.Iterables.filter;
import static org.qi4j.functional.Iterables.first;

/**
 * Declaration of a EntityComposite.
 */
public final class EntityAssemblyImpl
        extends CompositeAssemblyImpl
        implements EntityAssembly
{
    MetaInfo metaInfo = new MetaInfo();
    Visibility visibility = Visibility.module;
    List<Class<?>> concerns = new ArrayList<Class<?>>();
    List<Class<?>> sideEffects = new ArrayList<Class<?>>();
    List<Class<?>> mixins = new ArrayList<Class<?>>();
    List<Class<?>> types = new ArrayList<Class<?>>();
    private AssociationDeclarations associationDeclarations;
    private ManyAssociationDeclarations manyAssociationDeclarations;
    protected AssociationsModel associationsModel;
    protected ManyAssociationsModel manyAssociationsModel;

    public EntityAssemblyImpl( Class<? extends EntityComposite> compositeType )
    {
        this.compositeType = compositeType;
    }

    @Override
    public Class<?> type()
    {
        return compositeType;
    }

    EntityModel newEntityModel(
            StateDeclarations stateDeclarations,
            AssociationDeclarations associationDecs,
            ManyAssociationDeclarations manyAssociationDecs,
            AssemblyHelper helper
    )
    {
        this.associationDeclarations = associationDecs;
        this.manyAssociationDeclarations = manyAssociationDecs;
        this.stateDeclarations = stateDeclarations;
        try
        {
            this.helper = helper;

            metaInfo = new MetaInfo( metaInfo ).withAnnotations( compositeType );
            addAnnotationsMetaInfo( compositeType, metaInfo );

            immutable = metaInfo.get( Immutable.class ) != null;
            propertiesModel = new PropertiesModel();
            stateModel = new StateModel( propertiesModel );
            mixinsModel = new MixinsModel();

            propertiesModel = new PropertiesModel();
            associationsModel = new AssociationsModel( );
            manyAssociationsModel = new ManyAssociationsModel();
            stateModel = new EntityStateModel( propertiesModel, associationsModel, manyAssociationsModel );
            mixinsModel = new EntityMixinsModel();

            compositeMethodsModel = new CompositeMethodsModel( mixinsModel );

            // The composite must always implement EntityComposite, as a marker interface
            if( !EntityComposite.class.isAssignableFrom( compositeType ) )
            {
                types.add( EntityComposite.class );
            }

            // Implement composite methods
            Iterable<Class<? extends Constraint<?, ?>>> constraintClasses = constraintDeclarations( compositeType );
            Iterable<Class<?>> concernClasses = Iterables.<Class<?>, Iterable<Class<?>>>flatten( concerns, concernDeclarations( compositeType ) );
            Iterable<Class<?>> sideEffectClasses = Iterables.<Class<?>, Iterable<Class<?>>>flatten( sideEffects, sideEffectDeclarations( compositeType ) );
            Iterable<Class<?>> mixinClasses = Iterables.<Class<?>, Iterable<Class<?>>>flatten( mixins, mixinDeclarations( compositeType ) );
            implementMixinType( compositeType,
                    constraintClasses, concernClasses, sideEffectClasses, mixinClasses );

            // Implement additional type methods
            for( Class<?> type : types )
            {
                Iterable<Class<? extends Constraint<?, ?>>> typeConstraintClasses = Iterables.<Class<? extends Constraint<?, ?>>, Iterable<Class<? extends Constraint<?, ?>>>>flatten( constraintClasses, constraintDeclarations( type ) );
                Iterable<Class<?>> typeConcernClasses = Iterables.<Class<?>, Iterable<Class<?>>>flatten( concernClasses, concernDeclarations( type ) );
                Iterable<Class<?>> typeSideEffectClasses = Iterables.<Class<?>, Iterable<Class<?>>>flatten( sideEffectClasses, sideEffectDeclarations( type ) );
                Iterable<Class<?>> typeMixinClasses = Iterables.<Class<?>, Iterable<Class<?>>>flatten( mixinClasses, mixinDeclarations( type ) );

                implementMixinType( type,
                        typeConstraintClasses, typeConcernClasses, typeSideEffectClasses, typeMixinClasses );
            }

            // Add state from methods and fields
            addState( constraintClasses );

            EntityModel entityModel = new EntityModel(
                    compositeType, Iterables.prepend( compositeType, types ), visibility, metaInfo, (EntityMixinsModel) mixinsModel, (EntityStateModel) stateModel, compositeMethodsModel );

            return entityModel;
        } catch( Exception e )
        {
            throw new InvalidApplicationException( "Could not register " + compositeType.getName(), e );
        }
    }

    protected void addStateFor( AccessibleObject accessor, Iterable<Class<? extends Constraint<?, ?>>> constraintClasses )
    {
        String stateName = QualifiedName.fromAccessor( accessor ).name();

        if (registeredStateNames.contains( stateName ))
            return; // Skip already registered names

        Class<?> accessorType = Classes.RAW_CLASS.map( Classes.TYPE_OF.map( accessor ) );
        if( Property.class.isAssignableFrom( accessorType ) )
        {
            propertiesModel.addProperty( newPropertyModel( accessor, constraintClasses ) );
            registeredStateNames.add( stateName );
        } else if( Association.class.isAssignableFrom( accessorType ) )
        {
            associationsModel.addAssociation( newAssociationModel( accessor, constraintClasses ) );
            registeredStateNames.add( stateName );
        } else if( ManyAssociation.class.isAssignableFrom( accessorType ) )
        {
            manyAssociationsModel.addManyAssociation( newManyAssociationModel( accessor, constraintClasses ) );
            registeredStateNames.add( stateName );
        }
    }

    @Override
    protected PropertyModel newPropertyModel( AccessibleObject accessor, Iterable<Class<? extends Constraint<?, ?>>> constraintClasses )
    {
        Iterable<Annotation> annotations = Annotations.getAccessorAndTypeAnnotations( accessor );
        boolean optional = first( filter( isType( Optional.class ), annotations ) ) != null;
        ValueConstraintsModel valueConstraintsModel = constraintsFor( annotations, GenericPropertyInfo.getPropertyType( accessor ), ((Member) accessor)
                .getName(), optional, constraintClasses, accessor );
        ValueConstraintsInstance valueConstraintsInstance = null;
        if( valueConstraintsModel.isConstrained() )
        {
            valueConstraintsInstance = valueConstraintsModel.newInstance();
        }
        MetaInfo metaInfo = stateDeclarations.getMetaInfo( accessor );
        Object defaultValue = stateDeclarations.getInitialValue( accessor );
        boolean useDefaults = metaInfo.get( UseDefaults.class ) != null || stateDeclarations.isUseDefaults( accessor );
        boolean immutable = this.immutable || metaInfo.get( Immutable.class ) != null;
        PropertyModel propertyModel = new PropertyModel( accessor, immutable, useDefaults, valueConstraintsInstance, metaInfo, defaultValue );
        return propertyModel;
    }

    public AssociationModel newAssociationModel( AccessibleObject accessor, Iterable<Class<? extends Constraint<?, ?>>> constraintClasses )
    {
        Iterable<Annotation> annotations = Annotations.getAccessorAndTypeAnnotations( accessor );
        boolean optional = first( filter( isType( Optional.class ), annotations ) ) != null;

        // Constraints for Association references
        ValueConstraintsModel valueConstraintsModel = constraintsFor( annotations, GenericAssociationInfo
                .getAssociationType( accessor ), ((Member) accessor).getName(), optional, constraintClasses, accessor );
        ValueConstraintsInstance valueConstraintsInstance = null;
        if( valueConstraintsModel.isConstrained() )
        {
            valueConstraintsInstance = valueConstraintsModel.newInstance();
        }

        // Constraints for the Association itself
        valueConstraintsModel = constraintsFor( annotations, Association.class, ((Member) accessor).getName(), optional, constraintClasses, accessor );
        ValueConstraintsInstance associationValueConstraintsInstance = null;
        if( valueConstraintsModel.isConstrained() )
        {
            associationValueConstraintsInstance = valueConstraintsModel.newInstance();
        }

        MetaInfo metaInfo = associationDeclarations.getMetaInfo( accessor );
        AssociationModel associationModel = new AssociationModel( accessor, valueConstraintsInstance, associationValueConstraintsInstance, metaInfo );
        return associationModel;
    }

    public ManyAssociationModel newManyAssociationModel( AccessibleObject accessor, Iterable<Class<? extends Constraint<?, ?>>> constraintClasses )
    {
        Iterable<Annotation> annotations = Annotations.getAccessorAndTypeAnnotations(  accessor );
        boolean optional = first( filter( isType( Optional.class ), annotations ) ) != null;

        // Constraints for entities in ManyAssociation
        ValueConstraintsModel valueConstraintsModel = constraintsFor( annotations, GenericAssociationInfo
                .getAssociationType( accessor ), ((Member) accessor).getName(), optional, constraintClasses, accessor );
        ValueConstraintsInstance valueConstraintsInstance = null;
        if( valueConstraintsModel.isConstrained() )
        {
            valueConstraintsInstance = valueConstraintsModel.newInstance();
        }

        // Constraints for the ManyAssociation itself
        valueConstraintsModel = constraintsFor( annotations, ManyAssociation.class, ((Member) accessor).getName(), optional, constraintClasses, accessor );
        ValueConstraintsInstance manyValueConstraintsInstance = null;
        if( valueConstraintsModel.isConstrained() )
        {
            manyValueConstraintsInstance = valueConstraintsModel.newInstance();
        }
        MetaInfo metaInfo = manyAssociationDeclarations.getMetaInfo( accessor );
        ManyAssociationModel associationModel = new ManyAssociationModel( accessor, valueConstraintsInstance, manyValueConstraintsInstance, metaInfo );
        return associationModel;
    }
}