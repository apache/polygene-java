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
package org.apache.zest.index.solr;

import java.io.File;
import org.junit.Ignore;
import org.junit.Rule;
import org.apache.zest.bootstrap.AssemblyException;
import org.apache.zest.bootstrap.ModuleAssembly;
import org.apache.zest.library.fileconfig.FileConfigurationOverride;
import org.apache.zest.library.fileconfig.FileConfigurationService;
import org.apache.zest.test.indexing.AbstractQueryTest;
import org.apache.zest.test.util.DelTreeAfter;

@Ignore( "SOLR Index/Query is not working at all" )
public class SolrQueryTest
    extends AbstractQueryTest
{

    private static final File DATA_DIR = new File( "build/tmp/solr-query-test" );
    @Rule
    public final DelTreeAfter delTreeAfter = new DelTreeAfter( DATA_DIR );

    @Override
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        super.assemble( module );
        FileConfigurationOverride override = new FileConfigurationOverride().withData( new File( DATA_DIR, "zest-data" ) ).
            withLog( new File( DATA_DIR, "zest-logs" ) ).withTemporary( new File( DATA_DIR, "zest-temp" ) );
        module.services( FileConfigurationService.class ).
            setMetaInfo( override );
        new SolrAssembler().assemble( module );
    }

}
