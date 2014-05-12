/**
 * This file Copyright (c) 2013-2014 Magnolia International
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
package info.magnolia.ui.admincentral.shellapp.favorites;

import static junit.framework.Assert.assertTrue;
import static org.mockito.Mockito.*;

import info.magnolia.i18nsystem.SimpleTranslator;
import info.magnolia.ui.api.shell.Shell;
import info.magnolia.ui.vaadin.integration.jcr.JcrNewNodeAdapter;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

/**
 * A class to test {@code FavoritesForm}.
 */
public class FavoritesFormTest {

    @Test
    public void testAddNewGroupWithNewFavorite(){

        JcrNewNodeAdapter newFavoriteNodeAdapter = mock(JcrNewNodeAdapter.class);
        JcrNewNodeAdapter newGroupNodeAdapter = mock(JcrNewNodeAdapter.class);
        FavoritesView.Listener viewListener = mock(FavoritesView.Listener.class);
        Shell shell = mock(Shell.class);
        SimpleTranslator simpleTranslator = mock(SimpleTranslator.class);

        FavoritesForm favoritesForm = new FavoritesForm(newFavoriteNodeAdapter, newGroupNodeAdapter, getAvailableGroups(), viewListener, shell, simpleTranslator);


        assertTrue(true);

    }

    private Map<String, String> getAvailableGroups(){
        Map<String, String> map = new HashMap<String, String>();
        map.put("my-pages", "my pages");
        map.put("sub-apps", "sub apps");
        map.put("config-nodes", "config nodes");
        return map;
    }


}
