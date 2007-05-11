/*
 * Copyright (c) 2007, Rickard Öberg. All Rights Reserved.
 * Copyright (c) 2007, Niclas Hedhman. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package iop.runtime;

import org.qi4j.api.annotation.Modifies;
import org.qi4j.api.annotation.Uses;

import java.lang.reflect.*;
import java.lang.annotation.Annotation;
import java.util.List;

/**
 * TODO
 *
 */
public final class OldMethodInvocationHandler
    implements InvocationHandler
{
   public static Object getMixin(Object aProxy)
   {
      Object handler = Proxy.getInvocationHandler(aProxy);
      if (!(handler instanceof OldMethodInvocationHandler ))
         throw new IllegalArgumentException("Not a method proxy");

      return ((OldMethodInvocationHandler) handler).getMixin();
   }

   public static <T extends Annotation> T getMixinAnnotation(Object aProxy, Class<T> anAnnotation)
   {
      try
      {
         Object handler = Proxy.getInvocationHandler(aProxy);
         if (!(handler instanceof OldMethodInvocationHandler ))
            throw new IllegalArgumentException("Not a method proxy");

         OldMethodInvocationHandler methodHandler = ((OldMethodInvocationHandler) handler);

         return methodHandler.getMixin().getClass().getMethod(methodHandler.getMethod().getName(), methodHandler.getMethod().getParameterTypes()).getAnnotation(anAnnotation);
      } catch (NoSuchMethodException e)
      {
         return null;
      }
   }

   private Object proxy;
   private Method method;
   private List modifiers;
   private Object mixin;

   private int idx = 0;

   public OldMethodInvocationHandler(Object aProxy, Method aMethod, List aModifiers, Object aMixin)
      throws IllegalAccessException
   {
      proxy = aProxy;
      method = aMethod;
      modifiers = aModifiers;
      mixin = aMixin;

      for (Object modifier : modifiers )
      {
         Field[] fields = modifier.getClass().getDeclaredFields();

          for (Field field : fields)
         {
            Modifies target = field.getAnnotation(Modifies.class);
            if (target != null)
            {
               Class targetType = field.getType();
               field.setAccessible(true);
               field.set(modifier, Proxy.newProxyInstance(targetType.getClassLoader(), new Class[]{targetType}, this));
            }

            Uses uses = field.getAnnotation(Uses.class);
            if (uses != null)
            {
               field.setAccessible(true);
               field.set(modifier, aProxy);
            }
         }
      }
   }

   public Object getProxy()
   {
      return proxy;
   }

   public Method getMethod()
   {
      return method;
   }

   public List getModifiers()
   {
      return modifiers;
   }

   public Object getMixin()
   {
      return mixin;
   }


   public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
   {
      if (idx < modifiers.size())
      {
         try
         {
            Object modifier = modifiers.get(idx);
            idx++;
            if (modifier instanceof InvocationHandler)
            {
               return ((InvocationHandler) modifier).invoke(Proxy.newProxyInstance(proxy.getClass().getClassLoader(), proxy.getClass().getInterfaces(), this), method, args);
            } else
            {
               try
               {
                  return method.invoke(modifier, args);
               } catch (InvocationTargetException e)
               {
                  throw e.getTargetException();
               } catch (Throwable e)
               {
                  throw e;
               }
            }
         } finally
         {
            idx--;
         }
      } else
      {
         try
         {
            return method.invoke(mixin, args);
         } catch (InvocationTargetException e)
         {
            throw e.getTargetException();
         } catch (Throwable e)
         {
            throw e;
         }
      }
   }
}
