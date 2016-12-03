/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 */
package org.apache.zest.runtime.bootstrap;

import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Member;
import java.util.List;
import org.apache.zest.api.association.Association;
import org.apache.zest.api.association.GenericAssociationInfo;
import org.apache.zest.api.association.ManyAssociation;
import org.apache.zest.api.association.NamedAssociation;
import org.apache.zest.api.common.InvalidApplicationException;
import org.apache.zest.api.common.MetaInfo;
import org.apache.zest.api.common.Optional;
import org.apache.zest.api.common.QualifiedName;
import org.apache.zest.api.common.UseDefaults;
import org.apache.zest.api.property.GenericPropertyInfo;
import org.apache.zest.api.property.Immutable;
import org.apache.zest.api.property.Property;
import org.apache.zest.api.structure.ModuleDescriptor;
import org.apache.zest.api.util.Annotations;
import org.apache.zest.api.util.Classes;
import org.apache.zest.api.value.ValueComposite;
import org.apache.zest.bootstrap.StateDeclarations;
import org.apache.zest.bootstrap.ValueAssembly;
import org.apache.zest.runtime.association.AssociationModel;
import org.apache.zest.runtime.association.AssociationsModel;
import org.apache.zest.runtime.association.ManyAssociationModel;
import org.apache.zest.runtime.association.ManyAssociationsModel;
import org.apache.zest.runtime.association.NamedAssociationModel;
import org.apache.zest.runtime.association.NamedAssociationsModel;
import org.apache.zest.runtime.composite.StateModel;
import org.apache.zest.runtime.composite.ValueConstraintsInstance;
import org.apache.zest.runtime.composite.ValueConstraintsModel;
import org.apache.zest.runtime.property.PropertyModel;
import org.apache.zest.runtime.value.ValueModel;
import org.apache.zest.runtime.value.ValueStateModel;

import static org.apache.zest.api.util.Annotations.isType;
import static org.apache.zest.api.util.Classes.typeOf;

/**
 * Declaration of a ValueComposite.
 */
public final class ValueAssemblyImpl
    extends CompositeAssemblyImpl
    implements ValueAssembly
{
    private AssociationsModel associationsModel;
    private ManyAssociationsModel manyAssociationsModel;
    private NamedAssociationsModel namedAssociationsModel;

    public ValueAssemblyImpl( Class<?> compositeType )
    {
        super( compositeType );
        // The composite must always implement ValueComposite, as a marker interface
        if( !ValueComposite.class.isAssignableFrom( compositeType ) )
        {
            types.add( ValueComposite.class );
        }
    }

    @Override
    protected StateModel createStateModel()
    {
        return new ValueStateModel( propertiesModel, associationsModel, manyAssociationsModel, namedAssociationsModel );
    }

    ValueModel newValueModel( ModuleDescriptor module,
                              StateDeclarations stateDeclarations,
                              AssemblyHelper helper
    )
    {
        try
        {
            associationsModel = new AssociationsModel();
            manyAssociationsModel = new ManyAssociationsModel();
            namedAssociationsModel = new NamedAssociationsModel();
            buildComposite( helper, stateDeclarations );
            return new ValueModel(
                module, types, visibility, metaInfo, mixinsModel, (ValueStateModel) stateModel, compositeMethodsModel );
        }
        catch( Exception e )
        {
            throw new InvalidApplicationException( "Could not register " + types, e );
        }
    }

    protected AssociationsModel associationsModel()
    {
        return associationsModel;
    }

    protected ManyAssociationsModel manyAssociationsModel()
    {
        return manyAssociationsModel;
    }

    protected NamedAssociationsModel namedAssociationsModel()
    {
        return namedAssociationsModel;
    }
}
