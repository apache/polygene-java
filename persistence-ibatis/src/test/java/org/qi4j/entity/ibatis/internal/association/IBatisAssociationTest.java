/*  Copyright 2008 Edward Yakop.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.qi4j.entity.ibatis.internal.association;

import java.util.Map;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.junit.Test;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.composite.Composite;
import org.qi4j.entity.EntityComposite;
import org.qi4j.entity.EntitySession;
import org.qi4j.entity.ibatis.AccountComposite;
import org.qi4j.entity.ibatis.PersonComposite;
import org.qi4j.entity.ibatis.internal.common.Status;
import static org.qi4j.entity.ibatis.internal.common.Status.statusNew;
import org.qi4j.runtime.composite.CompositeContext;
import org.qi4j.runtime.structure.ModuleContext;
import org.qi4j.spi.composite.AssociationModel;
import org.qi4j.spi.composite.AssociationResolution;
import org.qi4j.spi.composite.CompositeBinding;
import org.qi4j.spi.property.AssociationBinding;
import org.qi4j.test.AbstractQi4jTest;
import org.qi4j.property.ImmutableProperty;

/**
 * @author edward.yakop@gmail.com
 * @since 0.1.0
 */
public final class IBatisAssociationTest extends AbstractQi4jTest
{
    /**
     * Test {@link IBatisAssociation#IBatisAssociation(String, AssociationBinding, Status, EntitySession)}
     */
    @Test
    public void testConstructor()
    {
        // Set up
        ModuleContext moduleContext = moduleInstance.getModuleContext();
        Map<Class<? extends Composite>, CompositeContext> compositeContexts = moduleContext.getCompositeContexts();
        CompositeContext accountContext = compositeContexts.get( AccountComposite.class );
        CompositeBinding accountBinding = accountContext.getCompositeBinding();
        Iterable<AssociationBinding> associationBindings = accountBinding.getAssociationBindings();
        AssociationBinding associationBinding = associationBindings.iterator().next();

        Mockery mockery = new Mockery();
        EntitySession entitySession = mockery.mock( EntitySession.class );

        // ==========================
        // Test with invalid argument
        // ==========================
        Object[] invalidArgumentss = new Object[]{
            new Object[]{ null, null, null, null },
            new Object[]{ null, null, null, entitySession },
            new Object[]{ null, null, statusNew, null },
            new Object[]{ null, null, statusNew, entitySession },
            new Object[]{ null, associationBinding, null, null },
            new Object[]{ null, associationBinding, null, entitySession },
            new Object[]{ null, associationBinding, statusNew, null },
            new Object[]{ "1", null, null, null },
            new Object[]{ "1", null, null, entitySession },
            new Object[]{ "1", null, statusNew, null },
            new Object[]{ "1", null, statusNew, entitySession },
            new Object[]{ "1", associationBinding, null, null },
            new Object[]{ "1", associationBinding, null, entitySession },
            new Object[]{ "1", associationBinding, statusNew, null }
        };

        String failMsg = "Construct with invalid argument must throw [IllegalArgumentException].";
        for( Object invalidArguments : invalidArgumentss )
        {
            Object[] arrArgument = (Object[]) invalidArguments;
            String identity = (String) arrArgument[ 0 ];
            AssociationBinding binding = (AssociationBinding) arrArgument[ 1 ];
            Status status = (Status) arrArgument[ 2 ];
            EntitySession session = (EntitySession) arrArgument[ 3 ];
            try
            {
                new IBatisAssociation( identity, binding, status, session );
                fail( failMsg );
            }
            catch( IllegalArgumentException e )
            {
                // Expected
            }
            catch( Exception e )
            {
                fail( failMsg );
            }
        }

        // ========================
        // Test with valid argument
        // ========================
        Object[] validArgumentss = new Object[]{
            new Object[]{ "1", associationBinding, statusNew, entitySession },
            new Object[]{ null, associationBinding, statusNew, entitySession }
        };
        for( Object validArguments : validArgumentss )
        {
            Object[] arrArgument = (Object[]) validArguments;
            String identity = (String) arrArgument[ 0 ];
            AssociationBinding binding = (AssociationBinding) arrArgument[ 1 ];
            Status status = (Status) arrArgument[ 2 ];
            EntitySession session = (EntitySession) arrArgument[ 3 ];
            try
            {
                new IBatisAssociation( identity, binding, status, session );
            }
            catch( Exception e )
            {
                fail( "Construct with valid argument must not fail." );
            }
        }
    }

    /**
     * Tests {@link IBatisAssociation#get()}
     */
    @Test
    @SuppressWarnings( "unchecked" )
    public final void testGet()
    {
        // Set up
        final AssociationBinding associationBinding = getPrimaryContactPersonAssociation();
        Mockery mockery = new Mockery();
        final EntitySession entitySession = mockery.mock( EntitySession.class );

        // ============================
        // Test with null initial value
        // ============================
        IBatisAssociation assoc1 = new IBatisAssociation( null, associationBinding, statusNew, entitySession );
        Object assoc1Value = assoc1.get();
        assertNull( assoc1Value );

        // ================================
        // Test with not null initial value
        // ================================
        IBatisAssociation<Object> assoc2 = new IBatisAssociation<Object>( "1", associationBinding, statusNew, entitySession );

        final PersonComposite personComposite = mockery.mock( PersonComposite.class );
        mockery.checking( new Expectations()
        {
            {
                AssociationResolution associationResolution = associationBinding.getAssociationResolution();
                AssociationModel model = associationResolution.getAssociationModel();
                Class<EntityComposite> compositeType = (Class<EntityComposite>) model.getType();

                // One invocation of get refefence and return person composite
                one( entitySession ).getReference( "1", compositeType );
                will( returnValue( personComposite ) );
            }
        }
        );
        Object assoc2Value = assoc2.get();
        assertNotNull( assoc2Value );
        assertEquals( personComposite, assoc2Value );

        // Ensure that it doesn't call entity session again
        Object assoc3Value = assoc2.get();
        assertNotNull( assoc3Value );
        assertEquals( personComposite, assoc3Value );
    }

    /**
     * Tests {@link IBatisAssociation#set(Object)}
     */
    @Test
    public final void testSet()
    {
        // Set up
        AssociationBinding primaryContactPersonAssoc = getPrimaryContactPersonAssociation();
        final Mockery mockery = new Mockery();
        EntitySession entitySession = mockery.mock( EntitySession.class );
        final PersonComposite personComposite = mockery.mock( PersonComposite.class );
        mockery.checking( new Expectations()
        {
            {
                one( personComposite ).identity();
                ImmutableProperty immutableProperty = mockery.mock( ImmutableProperty.class );
                will( returnValue( immutableProperty ) );

                one( immutableProperty ).get();
                will( returnValue( "1" ) );
            }
        } );

        // ========
        // Test set
        // ========
        IBatisAssociation<PersonComposite> assoc1 =
            new IBatisAssociation<PersonComposite>( null, primaryContactPersonAssoc, statusNew, entitySession );
        Object assoc1Value = assoc1.get();
        assertNull( assoc1Value );

        assoc1.set( personComposite );
        assertTrue( assoc1.isDirty() );

        // ------------------
        // Test set with null
        // ------------------
        assoc1.set( null );
        assertNull( assoc1.get() );
        assertTrue( assoc1.isDirty() );
    }

    private AssociationBinding getPrimaryContactPersonAssociation()
    {
        ModuleContext moduleContext = moduleInstance.getModuleContext();
        Map<Class<? extends Composite>, CompositeContext> compositeContexts = moduleContext.getCompositeContexts();
        CompositeContext accountContext = compositeContexts.get( AccountComposite.class );
        CompositeBinding accountBinding = accountContext.getCompositeBinding();
        Iterable<AssociationBinding> associationBindings = accountBinding.getAssociationBindings();

        for( AssociationBinding associationBinding : associationBindings )
        {
            String associationName = associationBinding.getName();
            if( "primaryContactPerson".equals( associationName ) )
            {
                return associationBinding;
            }
        }

        fail( "Association [primaryContactPerson] is not found." );
        return null;
    }

    public final void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.addComposites( PersonComposite.class, AccountComposite.class );
    }
}
