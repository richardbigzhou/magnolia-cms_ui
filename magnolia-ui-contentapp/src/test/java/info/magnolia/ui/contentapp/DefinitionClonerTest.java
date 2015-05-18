/**
 * This file Copyright (c) 2015 Magnolia International
 * Ltd.  (http://www.magnolia-cms.com). All rights reserved.
 *
 *
 * This file is dual-licensed under both the Magnolia
 * Network Agreement and the GNU General Public License.
 * You may elect to use one or the other of these licenses.
 *
 * This file is distributed in the hope that it will be
 * useful, but AS-IS and WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE, TITLE, or NONINFRINGEMENT.
 * Redistribution, except as permitted by whichever of the GPL
 * or MNA you select, is prohibited.
 *
 * 1. For the GPL license (GPL), you can redistribute and/or
 * modify this file under the terms of the GNU General
 * Public License, Version 3, as published by the Free Software
 * Foundation.  You should have received a copy of the GNU
 * General Public License, Version 3 along with this program;
 * if not, write to the Free Software Foundation, Inc., 51
 * Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * 2. For the Magnolia Network Agreement (MNA), this file
 * and the accompanying materials are made available under the
 * terms of the MNA which accompanies this distribution, and
 * is available at http://www.magnolia-cms.com/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
 */
package info.magnolia.ui.contentapp;

import static org.junit.Assert.*;

import info.magnolia.context.AbstractMapBasedContext;
import info.magnolia.context.Context;
import info.magnolia.test.model.Color;
import info.magnolia.test.model.Pair;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Objects;

import org.junit.Ignore;
import org.junit.Test;

import com.thoughtworks.proxy.factory.CglibProxyFactory;
import com.thoughtworks.proxy.toys.decorate.Decorating;
import com.thoughtworks.proxy.toys.decorate.DecoratingInvoker;
import com.thoughtworks.proxy.toys.decorate.Decorator;

/**
 * Tests for {@link info.magnolia.ui.contentapp.DefinitionCloner}.
 */
public class DefinitionClonerTest {
    @Test
    public void clonesAreEqualButNotSameInstance() {
        // GIVEN
        final Bar b1 = new Bar(Color.ORANGE, new Pair("a", 12));

        // WHEN
        final Bar b2 = new DefinitionCloner().deepClone(b1);

        // THEN
        assertEquals(b1, b2);
        assertNotSame(b1, b2);
        assertEquals(b1.color, b2.color);
        assertNotSame(b1.color, b2.color);
        assertEquals(b1.pair, b2.pair);
        assertNotSame(b1.pair, b2.pair);
    }

    @Test
    public void referencesToContextAreNotCloned() {
        // Sanity check - checking equals implementation of DummyContext
        assertEquals(new DummyContext(), new DummyContext());
        assertNotSame(new DummyContext(), new DummyContext());

        // GIVEN
        final Foo foo1 = new Foo(Color.PINK, new BarWithContext(Color.RED, new Pair("a", 12), new DummyContext()));

        // WHEN
        final Foo foo2 = new DefinitionCloner().deepClone(foo1);

        // THEN
        assertEquals(foo1, foo2);
        final BarWithContext b1 = (BarWithContext) foo1.bar;
        final BarWithContext b2 = (BarWithContext) foo2.bar;
        assertEquals(b1, b2);
        assertNotSame(b1, b2);
        assertEquals(b1.ctx, b2.ctx);
        assertSame("Context should not be cloned, ie same instance should be referenced on both sides", b1.ctx, b2.ctx);
    }

    @Ignore("We're cloning form definitions in some corner cases and afterwards modify those. Introduce decorators for this.")
    @Test
    public void referencesToProxyCallbacksAreNotCloned() throws Exception {
        final Bar bar = new Bar(Color.RED, new Pair("a", 12));
        final Bar decoratedBar = Decorating.proxy(bar).visiting(new DummyDecorator()).build(new CglibProxyFactory());

        // GIVEN
        final Foo foo1 = new Foo(Color.ORANGE, decoratedBar);

        // WHEN
        final Foo foo2 = new DefinitionCloner().deepClone(foo1);

        // THEN
        // Decorating doesn't implement equals() by default, so we're just checking they're not the same instance
        assertNotSame(foo1.getBar(), foo2.getBar());

        // However, the decorators should not have been cloned
        final DummyDecorator dec1 = extractDecorator(foo1.getBar());
        final DummyDecorator dec2 = extractDecorator(foo2.getBar());
        assertSame("decorator off proxy should not have been cloned, should be same instance.", dec1, dec2);
    }

    public static class Foo {
        private final Color color;
        private final Bar bar;

        public Foo(Color c, Bar b) {
            this.color = c;
            this.bar = b;
        }

        public Color getColor() {
            return color;
        }

        public Bar getBar() {
            return bar;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            Foo foo = (Foo) o;
            return Objects.equals(color, foo.color) &&
                    Objects.equals(bar, foo.bar);
        }

        @Override
        public int hashCode() {
            return Objects.hash(color, bar);
        }
    }

    public static class Bar {
        private final Color color;
        private final Pair pair;

        public Bar(Color c, Pair d) {
            this.color = c;
            this.pair = d;
        }

        public Color getColor() {
            return color;
        }

        public Pair getPair() {
            return pair;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            Bar bar = (Bar) o;
            return Objects.equals(color, bar.color) &&
                    Objects.equals(pair, bar.pair);
        }

        @Override
        public int hashCode() {
            return Objects.hash(color, pair);
        }
    }

    public static class BarWithContext extends Bar {
        private final Context ctx;

        public BarWithContext(Color c, Pair p, Context ctx) {
            super(c, p);
            this.ctx = ctx;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            if (!super.equals(o)) {
                return false;
            }
            BarWithContext that = (BarWithContext) o;
            return Objects.equals(ctx, that.ctx);
        }

        @Override
        public int hashCode() {
            return Objects.hash(super.hashCode(), ctx);
        }
    }

    /**
     * A dummy map-based implementation of Context, which implements equals/hashcode so we can check things in this test.
     */
    private static class DummyContext extends AbstractMapBasedContext {

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            AbstractMapBasedContext that = (AbstractMapBasedContext) o;
            return Objects.equals(getMap(), that.getMap());
        }

        @Override
        public int hashCode() {
            return Objects.hash(getMap());
        }
    }

    /**
     * A completely useless Decorator, just used to validate it doesn't get cloned.
     * Note: Rits Cloner uses Objenesis, which bypasses the default constructor, even if it exists and is public, so we
     * can only rely on something like {@link #extractDecorator(Object)} to validate this wasn't cloned.
     */
    public static class DummyDecorator extends Decorator<Bar> {
        @Override
        public Object decorateResult(Bar proxy, Method method, Object[] args, Object result) {
            return super.decorateResult(proxy, method, args, result);
        }
    }

    private DummyDecorator extractDecorator(Object o) throws IllegalAccessException, NoSuchFieldException {
        final DecoratingInvoker invoker = (DecoratingInvoker) new CglibProxyFactory().getInvoker(o);
        final Field decorator = invoker.getClass().getDeclaredField("decorator");
        decorator.setAccessible(true);
        return (DummyDecorator) decorator.get(invoker);
    }

}