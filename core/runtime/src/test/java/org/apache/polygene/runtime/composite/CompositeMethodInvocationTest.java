package org.apache.polygene.runtime.composite;

import org.apache.polygene.api.injection.scope.Service;
import org.apache.polygene.api.mixin.Mixins;
import org.apache.polygene.api.service.ServiceComposite;
import org.apache.polygene.bootstrap.AssemblyException;
import org.apache.polygene.bootstrap.ModuleAssembly;
import org.apache.polygene.test.AbstractPolygeneTest;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class CompositeMethodInvocationTest extends AbstractPolygeneTest {

    @Service
    MyService srv;

    /**
     * To correct, change instance pool type in CompositeMethodModel
     * AtomicInstancePool -> SynchronizedCompositeMethodInstancePool
     */
    @Test
    public void corruptedMethodInvocation() throws InterruptedException {
        srv.dummy(); // to avoid concurrent activation (it can have own issues)

        ExecutorService exe = Executors.newFixedThreadPool(10);
        for (int i = 0; i < 10; i++) {
            exe.execute(() -> {
                while (true) {
                    srv.dummy();
                }
            });
        }
        exe.awaitTermination(10, TimeUnit.MINUTES);
    }

    @Override
    public void assemble(ModuleAssembly module) throws AssemblyException {
        module.services(MyService.class);
    }

    @Mixins(Impl.class)
    public interface MyService extends ServiceComposite {
        void dummy();
    }

    public abstract static class Impl implements MyService {
        @Override
        public void dummy() {
        }
    }

}
