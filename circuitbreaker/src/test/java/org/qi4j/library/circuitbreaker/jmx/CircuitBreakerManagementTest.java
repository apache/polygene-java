package org.qi4j.library.circuitbreaker.jmx;

import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.bootstrap.SingletonAssembler;
import org.qi4j.library.circuitbreaker.CircuitBreaker;
import org.qi4j.library.circuitbreaker.CircuitBreakers;
import org.qi4j.library.circuitbreaker.service.ServiceCircuitBreaker;
import org.qi4j.library.jmx.MBeanServerImporter;

import javax.management.MBeanServer;
import java.util.Random;

/**
 * Run this as a program and connect with VisualVM. That way you can monitor changes in attributes, notifications, and
 * execute operations on the CircuitBreaker through JMX.
 */
public class CircuitBreakerManagementTest
{
   public static void main(String[] args)
   {
      SingletonAssembler assembler = new SingletonAssembler()
      {
         @Override
         public void assemble(ModuleAssembly module) throws AssemblyException
         {
            CircuitBreaker cb = new CircuitBreaker(3, 250, CircuitBreakers.in(IllegalArgumentException.class));

            module.importedServices(MBeanServer.class).importedBy(MBeanServerImporter.class);
            module.importedServices(TestService.class).setMetaInfo(new TestService(cb));

            module.services(CircuitBreakerManagement.class).instantiateOnStartup();
         }
      };

      TestService service = assembler.serviceFinder().<TestService>findService(TestService.class).get();

      while (true)
      {
         try
         {
            Thread.sleep(1000);
         } catch (InterruptedException e)
         {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
         }

         service.helloWorld();
      }
   }

   public static class TestService
           implements ServiceCircuitBreaker
   {
      CircuitBreaker cb;
      Random random = new Random();

      public TestService(CircuitBreaker cb)
      {
         this.cb = cb;
      }

      @Override
      public CircuitBreaker getCircuitBreaker()
      {
         return cb;
      }

      public void helloWorld()
      {
         if (random.nextDouble() > 0.3)
            cb.throwable(new Throwable("Failed"));
         else
            cb.success();
      }
   }
}
