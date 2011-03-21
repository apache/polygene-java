package org.qi4j.bootstrap;

import org.junit.Assert;
import org.junit.Test;
import org.qi4j.api.common.Visibility;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.specification.Specifications;
import org.qi4j.spi.entity.EntityDescriptor;
import org.qi4j.spi.service.ServiceDescriptor;
import org.qi4j.spi.structure.ApplicationModelSPI;
import org.qi4j.spi.structure.DescriptorVisitor;

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

        model.visitDescriptor( new DescriptorVisitor<RuntimeException>()
        {
            @Override
            public void visit( ServiceDescriptor serviceDescriptor )
            {
                Assert.assertTrue( serviceDescriptor.isInstantiateOnStartup() );
                Assert.assertTrue( serviceDescriptor.visibility() == Visibility.layer );

                super.visit( serviceDescriptor );    //To change body of overridden methods use File | Settings | File Templates.
            }

            @Override
            public void visit( EntityDescriptor entityDescriptor )
                throws RuntimeException
            {
                Assert.assertTrue( entityDescriptor.visibility() == Visibility.application );

                super.visit( entityDescriptor );    //To change body of overridden methods use File | Settings | File Templates.
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
