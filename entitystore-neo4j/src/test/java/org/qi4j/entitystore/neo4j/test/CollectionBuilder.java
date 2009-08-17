/* Copyright 2008 Neo Technology, http://neotechnology.com.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.qi4j.entitystore.neo4j.test;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.Assert;
import org.qi4j.api.entity.EntityBuilder;
import org.qi4j.api.entity.association.ManyAssociation;
import org.qi4j.api.unitofwork.UnitOfWork;

/**
 * @author Tobias Ivarsson (tobias.ivarsson@neotechnology.com)
 */
public abstract class CollectionBuilder<T extends Collection>
{
    private final String name;
    private static final Map<Class, Applyer> appliers = Collections.unmodifiableMap(new HashMap<Class, Applyer>()
    {
        {
            put(List.class, new Applyer()
            {
                public <T extends Collection> void apply(CollectionBuilder<T> builder, ElementOwnerComposite owner, ElementFactory factory)
                {
                    builder.buildCollection((T) owner.many(), factory);
                }

                public void verify(UnitOfWork uow, ElementOwnerComposite elementOwner, int[] expected)
                {
                    CollectionBuilder.verify(elementOwner.many(), expected);
                }
            });
        }
    });

    private int[] expected;
    private ElementFactory factory;
    private String id;

    public CollectionBuilder(String name)
    {
        if (name == null)
        {
            throw new NullPointerException("name cannot be null");
        }
        this.name = name;
    }

    public CollectionBuilder(int... expected)
    {
        this.name = null;
        this.expected = expected;
    }

    protected abstract void build(T collection);

    protected final ContainedElement element(int index)
    {
        if (factory == null)
        {
            throw new IllegalStateException("element(int) may only be invoked from within the build(...) method.");
        }
        return factory.element(index);
    }

    static <T extends Collection> Case<T> produce(UnitOfWork uow, CollectionBuilder<T>[] builders)
    {
        ElementFactory factory = new ElementFactory(uow);
        Type type = ((ParameterizedType) builders[0].getClass().getGenericSuperclass()).getActualTypeArguments()[0];
        Class clazz;
        if (type instanceof ParameterizedType)
        {
            ParameterizedType pType = (ParameterizedType) type;
            clazz = (Class) pType.getRawType();
        } else
        {
            clazz = (Class) type;
        }
        Applyer applyer = appliers.get(clazz);
        Verifyer<T> result = new Verifyer<T>(applyer);
        if (applyer == null)
        {
            throw new IllegalArgumentException("Unsupported Collection type: " + clazz);
        }
        for (CollectionBuilder<T> builder : builders)
        {
            ElementOwnerComposite owner;
            if (builder.name != null)
            {
                owner = result.getOwner(uow, builder.name);
            } else
            {
                owner = newElementOwner(uow);
                builder.id = owner.identity().get();
                result.addVerifier(builder);
            }
            applyer.apply(builder, owner, factory);
        }
        return result;
    }

    static <T extends Collection> void verify(UnitOfWork uow, Case<T> test, String name, int[] expected)
    {
        ((Verifyer<T>) test).verify(uow, name, expected);
    }

    static <T extends Collection> void verify(UnitOfWork uow, Case<T> test)
    {
        ((Verifyer<T>) test).verify(uow);
    }

    private static void verify(ManyAssociation<ContainedElement> list, int[] expected)
    {
        Assert.assertEquals("Erronious List size", (Object) expected.length, (Object) list.count());
        Iterator<ContainedElement> iter = list.iterator();
        int index;
        for (index = 0; iter.hasNext() && index < expected.length; index++)
        {
            ContainedElement element = iter.next();
            Assert.assertEquals("Element on position " + index + " does not match expectation",
                    (Object) expected[index], (Object) element.index().get());
        }
        Assert.assertTrue("Expected values not exhausted, stopped after " + index + " of " + expected.length, index == expected.length);
        Assert.assertFalse("List not exhausted", iter.hasNext());
    }

    private static void verify(List<ContainedElement> list, int[] expected)
    {
        Assert.assertEquals("Erronious List size", (Object) expected.length, (Object) list.size());
        Iterator<ContainedElement> iter = list.iterator();
        int index;
        for (index = 0; iter.hasNext() && index < expected.length; index++)
        {
            ContainedElement element = iter.next();
            Assert.assertEquals("Element on position " + index + " does not match expectation",
                    (Object) expected[index], (Object) element.index().get());
        }
        Assert.assertTrue("Expected values not exhausted, stopped after " + index + " of " + expected.length, index == expected.length);
        Assert.assertFalse("List not exhausted", iter.hasNext());
    }

    private static void verify(Set<ContainedElement> set, int[] expected)
    {
        Assert.assertEquals("Erronious Set size", (Object) expected.length, (Object) set.size());
        Set<Integer> expectedSet = new HashSet<Integer>();
        for (int i : expected)
        {
            expectedSet.add(i);
        }
        for (ContainedElement element : set)
        {
            Integer value = element.index().get();
            Assert.assertTrue("Unexpected value " + value + " in Set.", expectedSet.remove(value));
        }
        Assert.assertTrue("Not all expected values in Set, " + expectedSet.size() + " values missing", expectedSet.isEmpty());
    }

    private void buildCollection(T collection, ElementFactory factory)
    {
        this.factory = factory;
        try
        {
            build(collection);
        }
        finally
        {
            this.factory = null;
        }
    }

    private static class ElementFactory
    {
        private final Map<Integer, ContainedElement> singletons = new HashMap<Integer, ContainedElement>();
        private final UnitOfWork uow;

        ElementFactory(UnitOfWork uow)
        {
            this.uow = uow;
        }

        final ContainedElement element(int index)
        {
            ContainedElement result = singletons.get(index);
            if (result == null)
            {
                result = newContainedElement(uow, index);
                singletons.put(index, result);
            }
            return result;
        }
    }

    private static ElementOwnerComposite newElementOwner(UnitOfWork uow)
    {
        return uow.newEntityBuilder(ElementOwnerComposite.class, null).newInstance();
    }

    private static ElementOwnerComposite getElementOwner(UnitOfWork uow, String id)
    {
        return uow.get(ElementOwnerComposite.class, id);
    }

    private static ContainedElement newContainedElement(UnitOfWork uow, int index)
    {
        EntityBuilder<ContainedElementComposite> builder = uow
                .newEntityBuilder(ContainedElementComposite.class, null);
        ContainedElement prototype = builder.instance();
        prototype.index().set(index);
        return builder.newInstance();
    }

    private static class Verifyer<T extends Collection> implements Case<T>
    {
        private Map<String, String> composites = new HashMap<String, String>();
        private Applyer applyer;
        private List<CollectionBuilder<T>> verifiers = new LinkedList<CollectionBuilder<T>>();

        Verifyer(Applyer applyer)
        {
            this.applyer = applyer;
        }

        ElementOwnerComposite getOwner(UnitOfWork uow, String name)
        {
            String id = composites.get(name);
            if (id != null)
            {
                return getElementOwner(uow, id);
            } else
            {
                ElementOwnerComposite result = newElementOwner(uow);
                composites.put(name, result.identity().get());
                return result;
            }
        }

        void verify(UnitOfWork uow, String name, int[] expected)
        {
            String id = composites.get(name);
            if (id == null)
            {
                throw new IllegalArgumentException("No Collection defined named \"" + name + "\".");
            }
            applyer.verify(uow, getElementOwner(uow, id), expected);
        }

        void verify(UnitOfWork uow)
        {
            for (CollectionBuilder<T> verifier : verifiers)
            {
                applyer.verify(uow, getElementOwner(uow, verifier.id), verifier.expected);
            }
        }

        void addVerifier(CollectionBuilder<T> verifier)
        {
            verifiers.add(verifier);
        }
    }

    private interface Applyer
    {

        <T extends Collection> void apply(CollectionBuilder<T> builder, ElementOwnerComposite owner, ElementFactory factory);

        void verify(UnitOfWork uow, ElementOwnerComposite elementOwner, int[] expected);
    }
}
