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
package org.apache.polygene.test.entity.model.legal;

import java.math.BigDecimal;
import java.util.Map;
import org.apache.polygene.api.entity.EntityBuilder;
import org.apache.polygene.api.identity.Identity;
import org.apache.polygene.api.identity.StringIdentity;
import org.apache.polygene.api.injection.scope.Structure;
import org.apache.polygene.api.mixin.Mixins;
import org.apache.polygene.api.unitofwork.UnitOfWork;
import org.apache.polygene.api.unitofwork.UnitOfWorkFactory;
import org.apache.polygene.api.unitofwork.concern.UnitOfWorkPropagation;
import org.apache.polygene.api.value.ValueBuilder;
import org.apache.polygene.api.value.ValueBuilderFactory;
import org.apache.polygene.test.entity.model.people.Person;

@Mixins( LegalService.Mixin.class )
public interface LegalService
{
    @UnitOfWorkPropagation
    Identity createWill( Person principal, Map<Person, BigDecimal> amounts, Map<Person, Float> percentages, Map<Person, String> specificItems );

    class Mixin
        implements LegalService
    {
        @Structure
        private ValueBuilderFactory vbf;

        @Structure
        private UnitOfWorkFactory uowf;

        @Override
        public Identity createWill( Person principal, Map<Person, BigDecimal> amounts, Map<Person, Float> percentages, Map<Person, String> specificItems )
        {
            UnitOfWork uow = uowf.currentUnitOfWork();
            Identity identity = StringIdentity.identityOf( "will-" + principal.name().get() );
            EntityBuilder<Will> builder = uow.newEntityBuilder( Will.class, identity );
            Will instance = builder.instance();
            for( Map.Entry<Person, BigDecimal> entry : amounts.entrySet() )
            {
                WillAmount amount = createAmount( entry.getKey(), entry.getValue() );
                instance.amounts().add( amount );
            }
            for( Map.Entry<Person, Float> entry : percentages.entrySet() )
            {
                WillPercentage amount = createPercentage( entry.getKey(), entry.getValue() );
                instance.percentages().add( amount );
            }
            for( Map.Entry<Person, String> entry : specificItems.entrySet() )
            {
                String value = entry.getValue();
                int pos = value.indexOf( '\n' );
                String item = value.substring( 0, pos );
                String description = value.substring( pos + 1 );
                WillItem amount = createItem( entry.getKey(), item, description );
                instance.items().add( amount );
            }
            builder.newInstance();
            return identity;
        }

        private WillAmount createAmount( Person beneficiary, BigDecimal amount )
        {
            ValueBuilder<WillAmount> builder = vbf.newValueBuilder( WillAmount.class );
            builder.prototype().amount().set( amount );
            builder.prototype().beneficiary().set( beneficiary );
            return builder.newInstance();
        }

        private WillPercentage createPercentage( Person beneficiary, Float percentage )
        {
            ValueBuilder<WillPercentage> builder = vbf.newValueBuilder( WillPercentage.class );
            builder.prototype().percentage().set( percentage );
            builder.prototype().beneficiary().set( beneficiary );
            return builder.newInstance();
        }

        private WillItem createItem( Person beneficiary, String item, String description )
        {
            ValueBuilder<WillItem> builder = vbf.newValueBuilder( WillItem.class );
            builder.prototype().item().set( item );
            builder.prototype().item().set( description );
            builder.prototype().beneficiary().set( beneficiary );
            return builder.newInstance();
        }
    }
}
