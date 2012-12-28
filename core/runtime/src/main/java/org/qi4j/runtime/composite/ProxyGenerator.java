package org.qi4j.runtime.composite;

import java.lang.reflect.Proxy;

/**
 * generates proxyclasses
 */
public class ProxyGenerator {
    public static Class<?> createProxyClass(ClassLoader mainTypeClassLoader, Class<?>[] interfaces) {
        ClassLoader effectiveClassLoader = Thread.currentThread().getContextClassLoader();
        if (effectiveClassLoader == null) {
            effectiveClassLoader = mainTypeClassLoader;
        }
        return Proxy.getProxyClass(effectiveClassLoader, interfaces);
    }
}
