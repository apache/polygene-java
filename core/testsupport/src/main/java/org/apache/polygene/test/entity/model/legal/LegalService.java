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

import java.util.ArrayList;
import java.util.List;
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
import org.apache.polygene.test.entity.model.monetary.Currency;
import org.apache.polygene.test.entity.model.people.Person;

@Mixins( LegalService.Mixin.class )
public interface LegalService
{
    @UnitOfWorkPropagation
    Will findWillById( Identity willId );

    @UnitOfWorkPropagation
    Will createWill( Person principal, Map<Person, Currency> amounts, Map<Person, Float> percentages, Map<Person, String> specificItems );

    WillPercentage createPercentage( Person beneficiary, float percentage );

    WillItem createItem( Person beneficiary, String item );

    class Mixin
        implements LegalService
    {
        @Structure
        private ValueBuilderFactory vbf;

        @Structure
        private UnitOfWorkFactory uowf;

        @Override
        public Will findWillById( Identity willId )
        {
            UnitOfWork uow = uowf.currentUnitOfWork();
            return uow.get( Will.class, willId );
        }

        @Override
        public Will createWill( Person principal, Map<Person, Currency> amounts, Map<Person, Float> percentages, Map<Person, String> specificItems )
        {
            UnitOfWork uow = uowf.currentUnitOfWork();
            Identity identity = StringIdentity.identityOf( "will-" + principal.name().get() );
            EntityBuilder<Will> builder = uow.newEntityBuilder( Will.class, identity );
            List<WillAmount> amountsList = new ArrayList<>();
            for( Map.Entry<Person, Currency> entry : amounts.entrySet() )
            {
                WillAmount amount = createAmount( entry.getKey(), entry.getValue() );
                amountsList.add( amount );
            }
            List<WillPercentage> percentagesList = new ArrayList<>();
            for( Map.Entry<Person, Float> entry : percentages.entrySet() )
            {
                WillPercentage amount = createPercentage( entry.getKey(), entry.getValue() );
                percentagesList.add( amount );
            }
            List<WillItem> itemsList = new ArrayList<>();
            for( Map.Entry<Person, String> entry : specificItems.entrySet() )
            {
                String value = entry.getValue();
                WillItem amount = createItem( entry.getKey(), value );
                itemsList.add( amount );
            }
            Will instance = builder.instance();
            instance.principal().set(principal);
            instance.percentages().set( percentagesList );
            instance.amounts().set( amountsList );
            instance.items().set( itemsList );
            return builder.newInstance();
        }

        private WillAmount createAmount( Person beneficiary, Currency amount )
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
            builder.prototype().description().set( description );
            builder.prototype().beneficiary().set( beneficiary );
            return builder.newInstance();
        }

        public WillItem createItem( Person beneficiary, String value )
        {
            int pos = value.indexOf( '\n' );
            String item = value.substring( 0, pos );
            String description = value.substring( pos + 1 );
            return createItem( beneficiary, item, description );
        }

        public WillPercentage createPercentage( Person beneficiary, float percentage )
        {
            ValueBuilder<WillPercentage> builder = vbf.newValueBuilder( WillPercentage.class );
            builder.prototype().beneficiary().set( beneficiary );
            builder.prototype().percentage().set( percentage );
            return builder.newInstance();
        }

    }
}
