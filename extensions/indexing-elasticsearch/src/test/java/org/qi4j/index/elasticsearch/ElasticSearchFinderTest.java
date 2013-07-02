/*
 * Copyright 2012 Paul Merlin.
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
package org.qi4j.index.elasticsearch;

import java.io.*;
import org.junit.*;
import org.qi4j.api.common.*;
import org.qi4j.bootstrap.*;
import org.qi4j.index.elasticsearch.assembly.*;
import org.qi4j.library.fileconfig.*;
import org.qi4j.test.*;
import org.qi4j.test.indexing.*;

import static org.junit.Assume.*;

public class ElasticSearchFinderTest
        extends AbstractEntityFinderTest {

    @BeforeClass
    public static void beforeClass_IBMJDK() {
        // Ignore this test on IBM JDK
        assumeTrue(!(System.getProperty("java.vendor").contains("IBM")));
    }

    @Override
    public void assemble(ModuleAssembly module)
            throws AssemblyException {
        super.assemble(module);

        // Config module
        ModuleAssembly config = module.layer().module("config");
        new EntityTestAssembler().assemble(config);

        // Index/Query
        new ESFilesystemIndexQueryAssembler().withConfigModule(config).withConfigVisibility(Visibility.layer).assemble(module);
        ElasticSearchConfiguration esConfig = config.forMixin(ElasticSearchConfiguration.class).declareDefaults();
        esConfig.indexNonAggregatedAssociations().set(Boolean.TRUE);

        // FileConfig
        FileConfigurationOverride override = new FileConfigurationOverride().withData(new File("build/qi4j-data")).
                withLog(new File("build/qi4j-logs")).withTemporary(new File("build/qi4j-temp"));
        module.services(FileConfigurationService.class).
                setMetaInfo(override);
    }

    @Override
    public void showNetwork() {
        // IndexExporter not supported by ElasticSearch
    }


}
