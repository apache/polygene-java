/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.polygene.runtime.composite;

import org.apache.polygene.api.injection.scope.Service;
import org.apache.polygene.api.mixin.Mixins;
import org.apache.polygene.api.service.ServiceComposite;
import org.apache.polygene.bootstrap.AssemblyException;
import org.apache.polygene.bootstrap.ModuleAssembly;
import org.apache.polygene.test.AbstractPolygeneTest;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/** Regression test POLYGENE-308
 */
public class CompositeMethodInvocationTest extends AbstractPolygeneTest {

    @Service
    MyService srv;

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
        exe.awaitTermination(30, TimeUnit.SECONDS);
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
