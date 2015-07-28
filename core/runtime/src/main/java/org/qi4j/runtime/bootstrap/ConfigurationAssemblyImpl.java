/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
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
import org.qi4j.bootstrap.ConfigurationAssembly;
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
public final class ConfigurationAssemblyImpl
    implements ConfigurationAssembly
{
    private ValueAssemblyImpl value;
    private EntityAssemblyImpl entity;

    public ConfigurationAssemblyImpl( Class<?> mainType )
    {
        value = new ValueAssemblyImpl( mainType );
        entity = new EntityAssemblyImpl( mainType );
    }

    @Override
    public Iterable<Class<?>> types()
    {
        return value.types();
    }
}
