/**
 * This file Copyright (c) 2013-2015 Magnolia International
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
package info.magnolia.ui.contentapp.setup;

import info.magnolia.cms.i18n.DefaultI18nContentSupport;
import info.magnolia.cms.i18n.I18nContentSupport;
import info.magnolia.cms.util.ClasspathResourcesUtil;
import info.magnolia.context.MgnlContext;
import info.magnolia.jcr.util.PropertiesImportExport;
import info.magnolia.module.InstallContextImpl;
import info.magnolia.module.ModuleRegistryImpl;
import info.magnolia.objectfactory.Components;
import info.magnolia.repository.RepositoryManager;
import info.magnolia.test.ComponentsTestUtil;
import info.magnolia.test.RepositoryTestCase;

import java.io.ByteArrayInputStream;
import java.net.URL;

import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.junit.After;
import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract test class, that set the necessary environment for {@link DataTypeToWorkspaceMigrationTask} test class.
 */
public abstract class AbstractAbstractDataTypeMigrationTaskTest extends RepositoryTestCase {
    private static final Logger log = LoggerFactory.getLogger(AbstractAbstractDataTypeMigrationTaskTest.class);
    protected Session dataSession;
    protected Session targetSession;
    protected InstallContextImpl installContext;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        dataSession = MgnlContext.getJCRSession("data");
        // Register node type definition
        RepositoryManager repositoryManager = Components.getComponent(RepositoryManager.class);
        try {
            // Register the target Node type definition
            repositoryManager.getRepositoryProvider("magnolia").registerNodeTypes(new ByteArrayInputStream(getTargetNodeTypeDefinition().getBytes()));
            // Register the data node type definition
            repositoryManager.getRepositoryProvider("magnolia").registerNodeTypes(new ByteArrayInputStream(getDataNodeTypeDefinition().getBytes()));
            // Register the custom node type (this type is for example category in data)
            repositoryManager.getRepositoryProvider("magnolia").registerNodeTypes(new ByteArrayInputStream(getCustomNodeTypeDefinition().getBytes()));
        } catch (RepositoryException e) {
            log.error("", e);
        }
        final ModuleRegistryImpl moduleRegistry = new ModuleRegistryImpl();
        installContext = new InstallContextImpl(moduleRegistry);

        // Init target session
        targetSession = MgnlContext.getJCRSession(getTargetWorkSpaceName());

        // Init data node structure
        URL resource = ClasspathResourcesUtil.getResource(getDataNodeStructureDefinitionFileName());
        new PropertiesImportExport().createNodes(dataSession.getRootNode(), resource.openStream());
        dataSession.save();

        // Init Components
        ComponentsTestUtil.setImplementation(I18nContentSupport.class, DefaultI18nContentSupport.class);

    }

    @Override
    @After
    public void tearDown() throws Exception {
        super.tearDown();
        ComponentsTestUtil.clear();
    }

    @Override
    protected String getRepositoryConfigFileName() {
        String repositoryFileName = getDataRepositoryDefinition();
        setRepositoryConfigFileName(repositoryFileName);
        return repositoryFileName;
    }

    /**
     * Return the file name (testNodeTree.properties) defining the node structure used in the data workspace.<br>
     * Normally located under src/rest/resources folder.
     */
    protected abstract String getDataNodeStructureDefinitionFileName();

    /**
     * Return the file name (test-data-repositories.xml) defining the available workspaces definition.
     * Normally located under src/rest/resources folder.
     */
    protected abstract String getDataRepositoryDefinition();

    /**
     * Return the target workspace name.<br>
     * <b>This workspace name has to be defined into the file returned by getDataRepositoryDefinition()</b>
     */
    protected abstract String getTargetWorkSpaceName();

    /**
     * Define the target node type definition (mgnl:category).
     */
    protected abstract String getTargetNodeTypeDefinition();

    /**
     * Define the custom data source node type definition (category).
     */
    protected abstract String getCustomNodeTypeDefinition();

    protected String getDataNodeTypeDefinition() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?><nodeTypes xmlns:rep=\"internal\" xmlns:nt=\"http://www.jcp.org/jcr/nt/1.0\" xmlns:mix=\"http://www.jcp.org/jcr/mix/1.0\" xmlns:mgnl=\"http://www.magnolia.info/jcr/mgnl\" xmlns:jcr=\"http://www.jcp.org/jcr/1.0\">"
                + " <nodeType name=\"dataBase\" isMixin=\"false\" hasOrderableChildNodes=\"true\" primaryItemName=\"\"><supertypes><supertype>mix:referenceable</supertype><supertype>nt:hierarchyNode</supertype><supertype>mgnl:created</supertype><supertype>mgnl:lastModified</supertype></supertypes><propertyDefinition name=\"*\" requiredType=\"undefined\" autoCreated=\"false\" mandatory=\"false\" onParentVersion=\"COPY\" protected=\"false\" multiple=\"false\"/><childNodeDefinition name=\"*\" defaultPrimaryType=\"\" autoCreated=\"false\" mandatory=\"false\" onParentVersion=\"COPY\" protected=\"false\" sameNameSiblings=\"true\"/></nodeType>"
                + " <nodeType name=\"dataItemBase\" isMixin=\"false\" hasOrderableChildNodes=\"true\" primaryItemName=\"\"><supertypes><supertype>dataBase</supertype></supertypes></nodeType>"
                + " <nodeType name=\"dataFolder\" isMixin=\"false\" hasOrderableChildNodes=\"true\" primaryItemName=\"\"><supertypes><supertype>dataBase</supertype><supertype>mgnl:activatable</supertype></supertypes></nodeType>"
                + " <nodeType name=\"dataItem\" isMixin=\"false\" hasOrderableChildNodes=\"true\" primaryItemName=\"\"><supertypes><supertype>dataItemBase</supertype><supertype>mgnl:activatable</supertype><supertype>mgnl:versionable</supertype></supertypes></nodeType>"
                + " <nodeType name=\"dataItemNode\" isMixin=\"false\" hasOrderableChildNodes=\"true\" primaryItemName=\"\"><supertypes><supertype>dataItemBase</supertype><supertype>mgnl:activatable</supertype></supertypes></nodeType>"
                + " </nodeTypes>";
    }

}
