/*
 * Copyright 2008 Richard Wallace. All Rights Reserved.
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
package org.qi4j.regression.qi94;

import org.junit.Test;
import org.qi4j.api.association.Association;
import org.qi4j.api.entity.EntityBuilder;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.property.Property;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.test.AbstractQi4jTest;
import org.qi4j.test.EntityTestAssembler;

import static org.junit.Assert.assertEquals;

public class IssueTest
    extends AbstractQi4jTest
{
    @SuppressWarnings( "unchecked" )
    public void assemble( ModuleAssembly aModule )
        throws AssemblyException
    {
        aModule.entities( Item.class, ItemType.class );
        new EntityTestAssembler().assemble( aModule );
    }

    @Test
    public void entityBuilderAssociationTypeIsNotNull()
    {
        UnitOfWork uow = module.newUnitOfWork();
        try
        {
            EntityBuilder<Item> builder = uow.newEntityBuilder( Item.class );
            assertEquals( ItemType.class, qi4j.api()
                .entityDescriptorFor( builder.instance() )
                .state()
                .getAssociationByName( "typeOfItem" )
                .type() );
        }
        finally
        {
            uow.discard();
        }
    }

    interface Item
        extends EntityComposite
    {
        Association<ItemType> typeOfItem();
    }

    interface ItemType
        extends EntityComposite
    {
        Property<String> name();
    }
}
