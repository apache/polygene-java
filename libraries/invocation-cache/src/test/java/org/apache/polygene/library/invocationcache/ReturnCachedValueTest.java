package org.apache.polygene.library.invocationcache;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.polygene.api.injection.scope.Service;
import org.apache.polygene.api.mixin.Mixins;
import org.apache.polygene.bootstrap.AssemblyException;
import org.apache.polygene.bootstrap.ModuleAssembly;
import org.apache.polygene.test.AbstractPolygeneTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

public class ReturnCachedValueTest extends AbstractPolygeneTest {

    @Service
    ExpensiveOperation srv;

    @BeforeEach
    void resetInvoicationCounter() {
        ExpensiveOperation.invoicationCount.set(0);
    }

    @Test
    public void methodResultIsCachedForEqualParameters() {

        String parameter = "cachedHash";
        assertThat(srv.compute(parameter), equalTo(parameter.hashCode()));
        assertThat(srv.compute(parameter), equalTo(parameter.hashCode()));

        assertThat(ExpensiveOperation.invoicationCount.intValue(), equalTo(1));
    }

    //@Test
    //Ignored: cache key builder has to be fixed to support this case
    public void cacheKeyNotWorkWithVarargsParameter() {
        ExpensiveOperation.invoicationCount.set(0);
        srv.compute(7, 13);
        srv.compute(7, 13);

        assertThat(ExpensiveOperation.invoicationCount.intValue(), equalTo(1));
    }

    @Override
    public void assemble(ModuleAssembly module) throws AssemblyException {
        module.services(ExpensiveOperation.class)
                .withMixins(SimpleInvocationCacheMixin.class)
                .withConcerns(ReturnCachedValueConcern.class);
    }


    @Mixins(Impl.class)
    public interface ExpensiveOperation {

        AtomicInteger invoicationCount = new AtomicInteger(0);

        @Cached
        int compute(String param);

        int compute(int... arguments);
    }

    public abstract static class Impl implements ExpensiveOperation {

        @Override
        public int compute(String param) {
            invoicationCount.incrementAndGet();
            return param.hashCode();
        }

        @Override
        public int compute(int... arguments) {
            invoicationCount.incrementAndGet();
            return Arrays.stream(arguments).sum();
        }
    }
}

