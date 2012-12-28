package org.qi4j.api.metrics;

/**
 * Thrown when the underlying MetricsProvider do not support a Metric type.
 */
public class MetricsNotSupportedException extends RuntimeException
{
    public MetricsNotSupportedException( Class<? extends MetricsFactory> factoryType,
                                         Class<? extends MetricsProvider> providerType
    )
    {
        super( "Metrics [" + factoryType.getName() + "] is not supported by MetricsProvider [" + providerType.getName() + "]." );
    }
}
