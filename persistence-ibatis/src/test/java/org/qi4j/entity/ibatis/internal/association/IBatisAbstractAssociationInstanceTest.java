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

import java.util.HashMap;
import java.util.Map;
import org.qi4j.bootstrap.AssemblerException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.composite.CompositeBuilderFactory;
import org.qi4j.entity.ibatis.AccountComposite;
import org.qi4j.entity.ibatis.PersonComposite;
import org.qi4j.spi.composite.AssociationModel;
import org.qi4j.spi.composite.AssociationResolution;
import org.qi4j.spi.composite.CompositeBinding;
import org.qi4j.spi.property.AssociationBinding;
import org.qi4j.test.AbstractQi4jTest;

/**
 * {@code IBatisAbstractAssociationInstanceTest} tests {@code IBatisAbstractAssociationInstance}.
 *
 * @author edward.yakop@gmail.com
 * @since 0.1.0
 */
public class IBatisAbstractAssociationInstanceTest extends AbstractQi4jTest
{

    private static final class StubAbstractAssociationInstance
        extends IBatisAbstractAssociationInstance
    {
        /**
         * Construct an instance of {@code IBatisAbstractAssociationInstance}.
         *
         * @param aBinding The binding. This argument must not be {@code null}.
         * @since 0.1.0
         */
        StubAbstractAssociationInstance( AssociationBinding aBinding )
        {
            super( aBinding );
        }
    }

    /**
     * Tests {@link IBatisAbstractAssociationInstance#IBatisAbstractAssociationInstance(AssociationBinding)}
     *
     * @since 0.1.0
     */
    public final void testConstructor()
    {
        // **************************
        // Test with invalid argument
        // **************************
        String failMsg = "Construct with invalid argument, must throw [IllegalArgumentException].";
        try
        {
            new StubAbstractAssociationInstance( null );
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

        // ************************
        // Test with valid argument
        // ************************
        Map<String, AssociationBinding> accountAssociationsBindings = getAllAccountAssociationBindings();

        AssociationBinding primaryContactPersonBinding = accountAssociationsBindings.get( "primaryContactPerson" );
        try
        {
            new StubAbstractAssociationInstance( primaryContactPersonBinding );
        }
        catch( Exception e )
        {
            fail( "Construct with valid argument must not throw any exception." );
        }
    }

    private Map<String, AssociationBinding> getAllAccountAssociationBindings()
    {
        CompositeBuilderFactory builderFactory = moduleInstance.getStructureContext().getCompositeBuilderFactory();
        AccountComposite accountComposite = builderFactory.newComposite( AccountComposite.class );
        CompositeBinding accountBinding = runtime.getCompositeBinding( accountComposite );
        Iterable<AssociationBinding> associationBindings = accountBinding.getAssociationBindings();
        Map<String, AssociationBinding> associations = new HashMap<String, AssociationBinding>();
        for( AssociationBinding associationBinding : associationBindings )
        {
            String associationName = associationBinding.getName();
            associations.put( associationName, associationBinding );
        }
        return associations;
    }

    /**
     * Test getters and internal state.
     *
     * @since 0.1.0
     */
    public final void testGettersAndInternalState()
    {
        Map<String, AssociationBinding> accountAssociationsBindings = getAllAccountAssociationBindings();
        AssociationBinding primaryContactPersonBinding = accountAssociationsBindings.get( "primaryContactPerson" );
        StubAbstractAssociationInstance instance = new StubAbstractAssociationInstance( primaryContactPersonBinding );

        // ************
        // Test getters
        // ************

        // ---------------------
        // Test get association info
        // ---------------------
        assertEquals( null, instance.getAssociationInfo( String.class ) );

        AssociationResolution resolution = primaryContactPersonBinding.getAssociationResolution();
        AssociationModel model = resolution.getAssociationModel();

        // -------------
        // Test get name
        // -------------
        assertEquals( model.getName(), instance.getName() );

        // -------------------
        // Test qualified name
        // -------------------
        assertEquals( model.getQualifiedName(), instance.getQualifiedName() );

        // *******************
        // Test internal state
        // *******************
        assertEquals( primaryContactPersonBinding, instance.associationBinding );
    }

    public final void assemble( ModuleAssembly aModule )
        throws AssemblerException
    {
        aModule.addComposites( AccountComposite.class );
        aModule.addComposites( PersonComposite.class );
    }
}