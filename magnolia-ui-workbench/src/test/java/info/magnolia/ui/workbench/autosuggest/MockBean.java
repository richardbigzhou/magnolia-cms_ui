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

import info.magnolia.ui.workbench.autosuggest.AutoSuggesterForConfigurationAppTest.TestEnum;

import java.util.Collection;
import java.util.Map;

/**
 * MockBean contains properties of primitive, array, collection, map and bean types.
 */
public class MockBean {

    private String stringProperty;
    private boolean booleanProperty;
    private TestEnum enumProperty;
    private Boolean booleanWrappedProperty;
    private Character characterProperty;
    private Class<?> classProperty;
    private Long longProperty;
    private Integer integerProperty;
    private Byte byteProperty;
    private Short shortProperty;
    private Double doubleProperty;
    private Float floatProperty;
    private StringBuffer stringBufferProperty;
    private MockBean[] arrayProperty;
    private Collection<MockBean> collectionProperty;
    private Map<String, MockBean> mapProperty;

    public String getStringProperty() {
        return stringProperty;
    }

    public void setStringProperty(String stringProperty) {
        this.stringProperty = stringProperty;
    }

    public boolean isBooleanProperty() {
        return booleanProperty;
    }

    public void setBooleanProperty(boolean booleanProperty) {
        this.booleanProperty = booleanProperty;
    }

    public TestEnum getEnumProperty() {
        return enumProperty;
    }

    public void setEnumProperty(TestEnum enumProperty) {
        this.enumProperty = enumProperty;
    }

    public Boolean getBooleanWrappedProperty() {
        return booleanWrappedProperty;
    }

    public void setBooleanWrappedProperty(Boolean booleanWrappedProperty) {
        this.booleanWrappedProperty = booleanWrappedProperty;
    }

    public Character getCharacterProperty() {
        return characterProperty;
    }

    public void setCharacterProperty(Character characterProperty) {
        this.characterProperty = characterProperty;
    }

    public Class<?> getClassProperty() {
        return classProperty;
    }

    public void setClassProperty(Class<?> classProperty) {
        this.classProperty = classProperty;
    }

    public Long getLongProperty() {
        return longProperty;
    }

    public void setLongProperty(Long longProperty) {
        this.longProperty = longProperty;
    }

    public Integer getIntegerProperty() {
        return integerProperty;
    }

    public void setIntegerProperty(Integer integerProperty) {
        this.integerProperty = integerProperty;
    }

    public Byte getByteProperty() {
        return byteProperty;
    }

    public void setByteProperty(Byte byteProperty) {
        this.byteProperty = byteProperty;
    }

    public Short getShortProperty() {
        return shortProperty;
    }

    public void setShortProperty(Short shortProperty) {
        this.shortProperty = shortProperty;
    }

    public Double getDoubleProperty() {
        return doubleProperty;
    }

    public void setDoubleProperty(Double doubleProperty) {
        this.doubleProperty = doubleProperty;
    }

    public Float getFloatProperty() {
        return floatProperty;
    }

    public void setFloatProperty(Float floatProperty) {
        this.floatProperty = floatProperty;
    }

    public StringBuffer getStringBufferProperty() {
        return stringBufferProperty;
    }

    public void setStringBufferProperty(StringBuffer stringBufferProperty) {
        this.stringBufferProperty = stringBufferProperty;
    }

    public Integer getExtends() {
        return null;
    }

    public void setExtends(Integer i) {

    }

    public MockBean[] getArrayProperty() {
        return arrayProperty;
    }

    public void setArrayProperty(MockBean[] arrayProperty) {
        this.arrayProperty = arrayProperty;
    }

    public Collection<MockBean> getCollectionProperty() {
        return collectionProperty;
    }

    public void setCollectionProperty(Collection<MockBean> collectionProperty) {
        this.collectionProperty = collectionProperty;
    }

    public Map<String, MockBean> getMapProperty() {
        return mapProperty;
    }

    public void setMapProperty(Map<String, MockBean> mapProperty) {
        this.mapProperty = mapProperty;
    }

}
