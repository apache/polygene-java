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
import static junit.framework.Assert.fail;
import org.jmock.Mockery;
import org.junit.Test;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.composite.Composite;
import org.qi4j.entity.EntitySession;
import org.qi4j.entity.ibatis.AccountComposite;
import org.qi4j.entity.ibatis.PersonComposite;
import org.qi4j.runtime.composite.CompositeContext;
import org.qi4j.runtime.structure.ModuleContext;
import org.qi4j.spi.composite.CompositeBinding;
import org.qi4j.spi.property.AssociationBinding;
import org.qi4j.test.Qi4jTestSetup;

/**
 * @author edward.yakop@gmail.com
 * @since 0.1.0
 */
public final class IBatisAssociationTest extends Qi4jTestSetup
{
    @Test
    public void testConstructor()
    {
        // ==========================
        // Test with invalid argument
        // ==========================
        ModuleContext moduleContext = moduleInstance.getModuleContext();
        Map<Class<? extends Composite>, CompositeContext> compositeContexts = moduleContext.getCompositeContexts();
        CompositeContext accountContext = compositeContexts.get( AccountComposite.class );
        CompositeBinding accountBinding = accountContext.getCompositeBinding();
        Iterable<AssociationBinding> associationBindings = accountBinding.getAssociationBindings();
        AssociationBinding associationBinding = associationBindings.iterator().next();

        Mockery mockery = new Mockery();
        EntitySession entitySession = mockery.mock( EntitySession.class );

        Object[] invalidArgumentss = new Object[]{
            new Object[]{ null, null, null },
            new Object[]{ null, null, entitySession },
            new Object[]{ null, associationBinding, null },
            new Object[]{ "1", null, null },
            new Object[]{ "1", null, entitySession },
            new Object[]{ "1", associationBinding, null },
        };

        String failMsg = "Construct with invalid argument must throw [IllegalArgumentException].";
        for( Object invalidArguments : invalidArgumentss )
        {
            Object[] arrArgument = (Object[]) invalidArguments;
            String identity = (String) arrArgument[ 0 ];
            AssociationBinding binding = (AssociationBinding) arrArgument[ 1 ];
            EntitySession session = (EntitySession) arrArgument[ 2 ];
            try
            {
                new IBatisAssociation( identity, binding, session );
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
            new Object[]{ "1", associationBinding, entitySession },
            new Object[]{ null, associationBinding, entitySession }
        };
        for( Object validArguments : validArgumentss )
        {
            Object[] arrArgument = (Object[]) validArguments;
            String identity = (String) arrArgument[ 0 ];
            AssociationBinding binding = (AssociationBinding) arrArgument[ 1 ];
            EntitySession session = (EntitySession) arrArgument[ 2 ];
            try
            {
                new IBatisAssociation( identity, binding, session );
            }
            catch( Exception e )
            {
                fail( "Construct with valid argument must not fail." );
            }
        }
    }

    public final void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.addComposites( PersonComposite.class, AccountComposite.class );
    }
}
