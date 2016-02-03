/**
 * This file Copyright (c) 2015-2016 Magnolia International
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
package info.magnolia.ui.mediaeditor.setup;

import static info.magnolia.test.hamcrest.NodeMatchers.hasNode;
import static org.hamcrest.MatcherAssert.assertThat;

import info.magnolia.context.MgnlContext;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.module.ModuleManagementException;
import info.magnolia.module.ModuleVersionHandler;
import info.magnolia.module.ModuleVersionHandlerTestCase;
import info.magnolia.module.model.Version;
import info.magnolia.repository.RepositoryConstants;

import java.util.Arrays;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.junit.Test;

/**
 * Test class for {@link MediaEditorModuleVersionHandler}.
 */
public class MediaEditorModuleVersionHandlerTest extends ModuleVersionHandlerTestCase {

    @Override
    protected String getModuleDescriptorPath() {
        return "/META-INF/magnolia/ui-mediaeditor.xml";
    }

    @Override
    protected ModuleVersionHandler newModuleVersionHandlerForTests() {
        return new MediaEditorModuleVersionHandler();
    }

    @Override
    protected List<String> getModuleDescriptorPathsForTests() {
        // Super implementation returns this module's descriptor.
        // We override this because version-handler logic doesn't depend on the whole module dependency cascade
        // (only needs to be a non-empty list of existing module descriptors, so we pick the most basic one).
        return Arrays.asList(
                "/META-INF/magnolia/core.xml"
        );
    }

    @Test
    public void testUpdateFrom542() throws ModuleManagementException, RepositoryException {
        // GIVEN
        setupConfigNode("/modules/ui-mediaeditor/mediaEditors/image/actions/crop");

        // WHEN
        executeUpdatesAsIfTheCurrentlyInstalledVersionWas(Version.parseVersion("5.4.2"));

        // THEN
        Node node = MgnlContext.getJCRSession(RepositoryConstants.CONFIG).getNode("/modules/ui-mediaeditor/mediaEditors/image/actions/crop");
        assertThat(node, hasNode("availability", NodeTypes.ContentNode.NAME));
    }
}
