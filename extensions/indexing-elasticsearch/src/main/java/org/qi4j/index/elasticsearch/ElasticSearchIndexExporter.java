package org.qi4j.index.elasticsearch;

import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.spi.query.IndexExporter;

@Mixins( ElasticSearchIndexExporter.Mixin.class )
public interface ElasticSearchIndexExporter
        extends IndexExporter
{

    class Mixin
            implements ElasticSearchIndexExporter
    {

        @Override
        public void exportReadableToStream( PrintStream out )
                throws IOException, UnsupportedOperationException
        {
            exportFormalToWriter( new PrintWriter( out ) );
        }

        @Override
        public void exportFormalToWriter( PrintWriter out )
                throws IOException, UnsupportedOperationException
        {
            // TODO
        }

    }

}
