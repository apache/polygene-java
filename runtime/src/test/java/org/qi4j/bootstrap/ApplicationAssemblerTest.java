package org.qi4j.bootstrap;

import org.junit.Assert;
import org.junit.Test;
import org.qi4j.api.common.Visibility;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.specification.Specifications;
import org.qi4j.api.util.HierarchicalVisitor;
import org.qi4j.spi.entity.EntityDescriptor;
import org.qi4j.spi.service.ServiceDescriptor;
import org.qi4j.spi.structure.ApplicationModelSPI;

/**
 * TODO
 */
public class ApplicationAssemblerTest
{
    @Test
    public void testApplicationAssembler()
        throws AssemblyException
    {
        Energy4Java is = new Energy4Java(  );

        ApplicationModelSPI model = is.newApplicationModel( new ApplicationAssembler()
        {
            @Override
            public ApplicationAssembly assemble( ApplicationAssemblyFactory applicationFactory )
                throws AssemblyException
            {
                ApplicationAssembly assembly = applicationFactory.newApplicationAssembly();

                LayerAssembly layer1 = assembly.layer( "Layer1" );

                ModuleAssembly module = layer1.module( "Module1" );

                module.services( TestService.class );

                module.entities( TestEntity.class );

                layer1.services( AssemblySpecifications.types( TestService.class ) ).instantiateOnStartup();

                layer1.services( Specifications.<Object>TRUE() ).visibleIn( Visibility.layer );

                layer1.entities( Specifications.<Object>TRUE() ).visibleIn( Visibility.application );

                return assembly;
            }
        } );

        model.accept( new HierarchicalVisitor<Object, Object, RuntimeException>()
        {
            @Override
            public boolean visitEnter( Object visited ) throws RuntimeException
            {
                if (visited instanceof ServiceDescriptor)
                {
                    ServiceDescriptor serviceDescriptor = (ServiceDescriptor) visited;
                    Assert.assertTrue( serviceDescriptor.isInstantiateOnStartup() );
                    Assert.assertTrue( serviceDescriptor.visibility() == Visibility.layer );
                    return false;
                } else if (visited instanceof EntityDescriptor)
                {
                    EntityDescriptor entityDescriptor = (EntityDescriptor) visited;
                    Assert.assertTrue( entityDescriptor.visibility() == Visibility.application );
                    return false;
                }

                return true;
            }

            @Override
            public boolean visitLeave( Object visited ) throws RuntimeException
            {
                return true;
            }

            @Override
            public boolean visit( Object visited ) throws RuntimeException
            {
                return true;
            }
        });

        model.newInstance( is.spi() );

    }

    interface TestService
        extends ServiceComposite
    {

    }

    interface TestEntity
        extends EntityComposite
    {

    }
}
