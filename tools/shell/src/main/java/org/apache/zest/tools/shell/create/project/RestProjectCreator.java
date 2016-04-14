package org.apache.zest.tools.shell.create.project;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import org.apache.zest.tools.shell.create.project.restapp.ApplicationAssemblerWriter;
import org.apache.zest.tools.shell.create.project.restapp.ApplicationWriter;
import org.apache.zest.tools.shell.create.project.restapp.ConfigLayerWriter;
import org.apache.zest.tools.shell.create.project.restapp.ConfigModuleWriter;
import org.apache.zest.tools.shell.create.project.restapp.ConnectivityLayerWriter;
import org.apache.zest.tools.shell.create.project.restapp.CrudModuleWriter;
import org.apache.zest.tools.shell.create.project.restapp.CustomerWriter;
import org.apache.zest.tools.shell.create.project.restapp.DomainLayerWriter;
import org.apache.zest.tools.shell.create.project.restapp.FileConfigurationModuleWriter;
import org.apache.zest.tools.shell.create.project.restapp.HardCodedSecurityRepositoryMixinWriter;
import org.apache.zest.tools.shell.create.project.restapp.IndexingModuleWriter;
import org.apache.zest.tools.shell.create.project.restapp.InfrastructureLayerWriter;
import org.apache.zest.tools.shell.create.project.restapp.OrderItemWriter;
import org.apache.zest.tools.shell.create.project.restapp.OrderWriter;
import org.apache.zest.tools.shell.create.project.restapp.RestModuleWriter;
import org.apache.zest.tools.shell.create.project.restapp.OrderModuleWriter;
import org.apache.zest.tools.shell.create.project.restapp.SecurityModuleWriter;
import org.apache.zest.tools.shell.create.project.restapp.SecurityRepositoryWriter;
import org.apache.zest.tools.shell.create.project.restapp.SerializationModuleWriter;
import org.apache.zest.tools.shell.create.project.restapp.SettingsWriter;
import org.apache.zest.tools.shell.create.project.restapp.SimpleEnrolerWriter;
import org.apache.zest.tools.shell.create.project.restapp.SimpleVerifierWriter;
import org.apache.zest.tools.shell.create.project.restapp.StorageModuleWriter;
import org.apache.zest.tools.shell.create.project.restapp.WebXmlWriter;

public class RestProjectCreator extends AbstractProjectCreator
    implements ProjectCreator
{

    @Override
    public void create( String projectName,
                        File projectDir,
                        Map<String, String> properties
    )
        throws IOException
    {
        super.create( projectName, projectDir, properties );    // creates the directory structures.
        new ApplicationAssemblerWriter().writeClass( properties );
        new ConfigLayerWriter().writeClass( properties );
        new ConfigModuleWriter().writeClass( properties );
        new InfrastructureLayerWriter().writeClass( properties );
        new FileConfigurationModuleWriter().writeClass( properties );
        new StorageModuleWriter().writeClass( properties );
        new IndexingModuleWriter().writeClass( properties );
        new SerializationModuleWriter().writeClass( properties );
        new DomainLayerWriter().writeClass( properties );
        new OrderModuleWriter().writeClass( properties );
        new CrudModuleWriter().writeClass( properties );
        new ConnectivityLayerWriter().writeClass( properties );
        new RestModuleWriter().writeClass( properties );

        new ApplicationWriter().writeClass( properties );
        new SimpleEnrolerWriter().writeClass( properties );
        new SimpleVerifierWriter().writeClass( properties );

        new SecurityModuleWriter().writeClass( properties );
        new SecurityRepositoryWriter().writeClass( properties );
        new HardCodedSecurityRepositoryMixinWriter().writeClass( properties );
        new OrderWriter().writeClass( properties );
        new OrderItemWriter().writeClass( properties );
        new CustomerWriter().writeClass( properties );

        new SettingsWriter().writeClass( properties );
        new WebXmlWriter().writeClass( properties );
    }
}
