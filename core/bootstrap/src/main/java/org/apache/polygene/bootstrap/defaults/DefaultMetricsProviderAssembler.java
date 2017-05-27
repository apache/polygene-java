package org.apache.polygene.bootstrap.defaults;

import org.apache.polygene.api.metrics.MetricsProvider;
import org.apache.polygene.bootstrap.Assembler;
import org.apache.polygene.bootstrap.ModuleAssembly;
import org.apache.polygene.spi.metrics.MetricsProviderAdapter;

public class DefaultMetricsProviderAssembler
    implements Assembler
{
    public static final String IDENTITY = "default-metrics-provider";

    @Override
    public void assemble( ModuleAssembly module )
    {
        module.services( MetricsProvider.class )
              .withMixins( MetricsProviderAdapter.class )
              .identifiedBy( IDENTITY );
    }
}
