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
package org.apache.polygene.api.unitofwork;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;
import org.apache.polygene.api.association.Association;
import org.apache.polygene.api.association.AssociationDescriptor;
import org.apache.polygene.api.association.ManyAssociation;
import org.apache.polygene.api.association.NamedAssociation;
import org.apache.polygene.api.common.Optional;
import org.apache.polygene.api.common.QualifiedName;
import org.apache.polygene.api.common.UseDefaults;
import org.apache.polygene.api.entity.EntityReference;
import org.apache.polygene.api.identity.HasIdentity;
import org.apache.polygene.api.identity.Identity;
import org.apache.polygene.api.identity.StringIdentity;
import org.apache.polygene.api.injection.scope.Structure;
import org.apache.polygene.api.property.Property;
import org.apache.polygene.api.property.PropertyDescriptor;
import org.apache.polygene.api.usecase.Usecase;
import org.apache.polygene.api.usecase.UsecaseBuilder;
import org.apache.polygene.api.value.ValueBuilder;
import org.apache.polygene.bootstrap.AssemblyException;
import org.apache.polygene.bootstrap.ModuleAssembly;
import org.apache.polygene.spi.PolygeneSPI;
import org.apache.polygene.test.AbstractPolygeneTest;
import org.apache.polygene.test.EntityTestAssembler;
import org.junit.Test;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

public class ToEntityConversionTest
    extends AbstractPolygeneTest
{
    @Override
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        new EntityTestAssembler().assemble( module );
        module.entities( SomeType.class );
        module.values( SomeType.class );
    }

    @Test
    public void testPropertyConversionToEntity()
        throws Exception
    {
        Identity identity = new StringIdentity( "Niclas" );
        ValueBuilder<SomeType> vb = valueBuilderFactory.newValueBuilder( SomeType.class );
        SomeType prototype = vb.prototype();
        prototype.identity().set( identity );
        prototype.name().set( "Niclas" );
        SomeType value = vb.newInstance();

        Usecase usecase = UsecaseBuilder.buildUsecase( "test case" )
                                        .withMetaInfo( new SomeEntityConverter() )
                                        .newUsecase();
        try( UnitOfWork uow = unitOfWorkFactory.newUnitOfWork(usecase) )
        {
            SomeType entity = uow.toEntity( SomeType.class, value );
            assertThat( entity.identity().get(), equalTo( identity ) );
            assertThat( entity.name().get(), equalTo( "[Niclas]" ) );
            uow.complete();
        }
    }

    interface SomeType extends HasIdentity
    {
        Property<String> name();

        @Optional
        Association<String> assoc();

        @UseDefaults
        ManyAssociation<String> many();

        @UseDefaults
        NamedAssociation<String> named();
    }

    private static class SomeEntityConverter
        implements ToEntityConverter
    {
        @Structure
        private PolygeneSPI spi;

        @Override
        public Function<PropertyDescriptor, Object> properties( Object entityComposite, Function<PropertyDescriptor, Object> defaultFn )
        {
            return descriptor ->
            {
                QualifiedName name = QualifiedName.fromClass( SomeType.class, "name" );
                Object value = defaultFn.apply( descriptor );
                if( descriptor.qualifiedName().equals( name ) )
                {
                    return "[" + value + "]";
                }
                return value;
            };
        }

        @Override
        public Function<AssociationDescriptor, EntityReference> associations( Object entityComposite, Function<AssociationDescriptor, EntityReference> defaultFn )
        {
            return defaultFn;
        }

        @Override
        public Function<AssociationDescriptor, Stream<EntityReference>> manyAssociations( Object entityComposite, Function<AssociationDescriptor, Stream<EntityReference>> defaultFn )
        {
            return defaultFn;
        }

        @Override
        public Function<AssociationDescriptor, Stream<Map.Entry<String, EntityReference>>> namedAssociations( Object entityComposite, Function<AssociationDescriptor, Stream<Map.Entry<String, EntityReference>>> defaultFn )
        {
            return defaultFn;
        }
    }
}
