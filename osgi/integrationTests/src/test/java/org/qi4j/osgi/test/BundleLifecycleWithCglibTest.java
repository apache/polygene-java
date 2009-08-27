package org.qi4j.osgi.test;

import static org.ops4j.pax.exam.CoreOptions.options;
import static org.ops4j.pax.exam.CoreOptions.provision;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.Configuration;

public final class BundleLifecycleWithCglibTest extends BundleLifecycleTest
{
    @Configuration
    public final Option[] cglibProvision()
    {
        return options(
            provision( "mvn:net.sourceforge.cglib/com.springsource.net.sf.cglib/2.1.3" )
        );
    }
}
