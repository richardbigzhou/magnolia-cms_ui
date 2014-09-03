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

/**
 * MockBeanWithClassProperties contains properties of type Class.
 */
public class MockBeanWithClassProperties {

    private Class classPropertyWithoutTypeParameter;

    private Class<?> classPropertyWithUnboundedWildcardTypeParameter;

    private Class<MockClassWithSubClasses> classPropertyWithConcreteTypeParameter;

    private Class<? super MockClassWithSubClasses> classPropertyWithLowerBoundedWildcardTypeParameter;

    private Class<? extends MockClassWithSubClasses> classPropertyWithUpperBoundedWildcardTypeParameter;

    private String modelClass;

    public Class getClassPropertyWithoutTypeParameter() {
        return classPropertyWithoutTypeParameter;
    }

    public void setClassPropertyWithoutTypeParameter(Class classPropertyWithoutTypeParameter) {
        this.classPropertyWithoutTypeParameter = classPropertyWithoutTypeParameter;
    }

    public Class<?> getClassPropertyWithUnboundedWildcardTypeParameter() {
        return classPropertyWithUnboundedWildcardTypeParameter;
    }

    public void setClassPropertyWithUnboundedWildcardTypeParameter(Class<?> classPropertyWithUnboundedWildcardTypeParameter) {
        this.classPropertyWithUnboundedWildcardTypeParameter = classPropertyWithUnboundedWildcardTypeParameter;
    }

    public String getModelClass() {
        return modelClass;
    }

    public void setModelClass(String modelClass) {
        this.modelClass = modelClass;
    }

    public Class<MockClassWithSubClasses> getClassPropertyWithConcreteTypeParameter() {
        return classPropertyWithConcreteTypeParameter;
    }

    public void setClassPropertyWithConcreteTypeParameter(Class<MockClassWithSubClasses> classPropertyWithConcreteTypeParameter) {
        this.classPropertyWithConcreteTypeParameter = classPropertyWithConcreteTypeParameter;
    }

    public Class<? super MockClassWithSubClasses> getClassPropertyWithLowerBoundedWildcardTypeParameter() {
        return classPropertyWithLowerBoundedWildcardTypeParameter;
    }

    public void setClassPropertyWithLowerBoundedWildcardTypeParameter(Class<? super MockClassWithSubClasses> classPropertyWithLowerBoundedWildcardTypeParameter) {
        this.classPropertyWithLowerBoundedWildcardTypeParameter = classPropertyWithLowerBoundedWildcardTypeParameter;
    }

    public Class<? extends MockClassWithSubClasses> getClassPropertyWithUpperBoundedWildcardTypeParameter() {
        return classPropertyWithUpperBoundedWildcardTypeParameter;
    }

    public void setClassPropertyWithUpperBoundedWildcardTypeParameter(Class<? extends MockClassWithSubClasses> classPropertyWithUpperBoundedWildcardTypeParameter) {
        this.classPropertyWithUpperBoundedWildcardTypeParameter = classPropertyWithUpperBoundedWildcardTypeParameter;
    }

}
