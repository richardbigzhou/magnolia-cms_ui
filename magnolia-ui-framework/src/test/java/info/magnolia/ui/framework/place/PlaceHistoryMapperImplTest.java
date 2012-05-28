/**
 * This file Copyright (c) 2012 Magnolia International
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
package info.magnolia.ui.framework.place;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * Test case for {@link PlaceHistoryMapperImpl}.
 *
 * @version $Id$
 */
public class PlaceHistoryMapperImplTest {

    @Prefix("test")
    public static class TestPlace extends Place {

        public static class Tokenizer implements PlaceTokenizer<TestPlace> {

            @Override
            public TestPlace getPlace(String token) {
                return new TestPlace(token);
            }

            @Override
            public String getToken(TestPlace place) {
                return place.getPath();
            }
        }

        private String path;

        public TestPlace(String path) {
            this.path = path;
        }

        public String getPath() {
            return path;
        }
    }

    @Prefix("no-tokenizer")
    public static class PlaceWithoutTokenizer extends Place {
    }

    public static class PlaceWithoutPrefix extends Place {

    }

    @Test
    public void testCanMapPlaceToTokenAndViceVersa() {

        PlaceHistoryMapperImpl mapper = new PlaceHistoryMapperImpl(TestPlace.class, PlaceWithoutTokenizer.class, PlaceWithoutPrefix.class);

        assertNotNull(mapper.getTokenizer("test"));
        assertNull(mapper.getTokenizer("no-tokenizer"));

        TestPlace place = (TestPlace) mapper.getPlace("test:/path/to/something");
        assertEquals("/path/to/something", place.getPath());
        assertNull(mapper.getPlace("test"));
        assertNull(mapper.getPlace("no-tokenizer:foobar"));
        assertNull(mapper.getPlace("/path/somewhere"));

        assertEquals("test:/path/to/something", mapper.getToken(place));
        assertNull(mapper.getToken(new PlaceWithoutPrefix()));
        assertNull(mapper.getToken(new PlaceWithoutTokenizer()));
    }
}
