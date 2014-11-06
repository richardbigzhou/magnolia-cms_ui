/**
 * This file Copyright (c) 2014 Magnolia International
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
package info.magnolia.ui.workbench.autosuggest;

import java.util.Map;

/**
 * MockBeanForNameOfPropertyAndNameOfNode contains properties of primitive, map and bean types.
 */
public class MockBeanForNameOfPropertyAndNameOfNode extends MockParentBeanForNameOfPropertyAndNameOfNode {

    private boolean booleanProperty;
    private String stringProperty;
    private Object objectProperty;
    private Map<String, String> mapProperty;
    private MockBean testBean;
    private boolean deprecatedBooleanProperty;
    private String deprecatedStringProperty;
    private MockBean deprecatedTestBean;
    private MockBean mockBean;

    public boolean isBooleanProperty() {
        return booleanProperty;
    }

    public void setBooleanProperty(boolean booleanProperty) {
        this.booleanProperty = booleanProperty;
    }

    public String getStringProperty() {
        return stringProperty;
    }

    public void setStringProperty(String stringProperty) {
        this.stringProperty = stringProperty;
    }

    public Object getObjectProperty() {
        return objectProperty;
    }

    public void setObjectProperty(Object objectProperty) {
        this.objectProperty = objectProperty;
    }

    public Map<String, String> getMapProperty() {
        return mapProperty;
    }

    public void setMapProperty(Map<String, String> mapProperty) {
        this.mapProperty = mapProperty;
    }

    public MockBean getTestBean() {
        return testBean;
    }

    public void setTestBean(MockBean testBean) {
        this.testBean = testBean;
    }

    @Deprecated
    public boolean isDeprecatedBooleanProperty() {
        return deprecatedBooleanProperty;
    }

    @Deprecated
    public void setDeprecatedBooleanProperty(boolean deprecatedBooleanProperty) {
        this.deprecatedBooleanProperty = deprecatedBooleanProperty;
    }

    @Deprecated
    public String getDeprecatedStringProperty() {
        return deprecatedStringProperty;
    }

    @Deprecated
    public void setDeprecatedStringProperty(String deprecatedStringProperty) {
        this.deprecatedStringProperty = deprecatedStringProperty;
    }

    @Deprecated
    public MockBean getDeprecatedTestBean() {
        return deprecatedTestBean;
    }

    @Deprecated
    public void setDeprecatedTestBean(MockBean deprecatedTestBean) {
        this.deprecatedTestBean = deprecatedTestBean;
    }

    public MockBean getMockBean() {
        return mockBean;
    }

    public void setMockBean(MockBean mockBean) {
        this.mockBean = mockBean;
    }

}
