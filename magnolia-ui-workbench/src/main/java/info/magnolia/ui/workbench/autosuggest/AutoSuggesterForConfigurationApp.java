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

import info.magnolia.cms.beans.config.VirtualURIMapping;
import info.magnolia.commands.chain.Command;
import info.magnolia.jcr.node2bean.PropertyTypeDescriptor;
import info.magnolia.jcr.node2bean.TypeDescriptor;
import info.magnolia.jcr.node2bean.TypeMapping;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.jcr.util.SessionUtil;
import info.magnolia.jcr.wrapper.DelegateNodeWrapper;
import info.magnolia.jcr.wrapper.ExtendingNodeWrapper;
import info.magnolia.objectfactory.Components;
import info.magnolia.rendering.renderer.Renderer;
import info.magnolia.rendering.template.TemplateDefinition;
import info.magnolia.rendering.template.configured.ConfiguredTemplateDefinition;
import info.magnolia.ui.api.action.ConfiguredActionDefinition;
import info.magnolia.ui.api.app.AppDescriptor;
import info.magnolia.ui.api.autosuggest.AutoSuggester;
import info.magnolia.ui.dialog.definition.FormDialogDefinition;
import info.magnolia.ui.dialog.registry.DialogDefinitionRegistry;
import info.magnolia.ui.form.fieldtype.definition.FieldTypeDefinition;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemId;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeItemId;
import info.magnolia.ui.vaadin.integration.jcr.JcrPropertyItemId;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;

import org.apache.commons.lang3.ClassUtils;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Returns suggestions and how to display them for a cell in the Configuration App tree.
 */
public class AutoSuggesterForConfigurationApp implements AutoSuggester {

    private static Logger log = LoggerFactory.getLogger(AutoSuggesterForConfigurationApp.class);

    private TypeMapping typeMapping = null;
    private DialogDefinitionRegistry dialogDefinitionRegistry = null;
    private Reflections reflections = null;

    public AutoSuggesterForConfigurationApp() {
        typeMapping = Components.getComponentProvider().getComponent(TypeMapping.class);
        if (typeMapping == null) {
            log.warn("Could not get TypeMapping using component provider.");
        }

        dialogDefinitionRegistry = Components.getComponentProvider().getComponent(DialogDefinitionRegistry.class);
        if (dialogDefinitionRegistry == null) {
            log.warn("Could not get DialogDefinitionRegistry using component provider.");
        }

        // QUESTION Can we efficiently search for subclasses in user packages to suggest the value of class properties?
        reflections = new Reflections(ClasspathHelper.forPackage("info.magnolia"), new SubTypesScanner());
    }

    @Override
    public AutoSuggesterResult getSuggestionsFor(Object itemId, Object propertyId) {
        if (itemId == null || propertyId == null) {
            return noSuggestionsAvailable();
        }

        // If processing a JCR node
        if (itemId instanceof JcrNodeItemId) {
            Node node = getNodeFromJcrItemId((JcrNodeItemId) itemId);

            if (node != null) {
                // If processing name field of a node
                if ("jcrName".equals(propertyId)) {
                    return getSuggestionsForNameOfNode(node);
                }
                // If not processing name field of a node
                else {
                    return noSuggestionsAvailable();
                }
            }
            else {
                return noSuggestionsAvailable();
            }
        }
        // If processing a JCR property
        else if (itemId instanceof JcrPropertyItemId) {
            JcrPropertyItemId propertyItemId = (JcrPropertyItemId) itemId;
            Node parentNode = getNodeFromJcrItemId(propertyItemId);

            if (parentNode != null) {
                String propertyName = propertyItemId.getPropertyName();

                // If processing name field of a property
                if ("jcrName".equals(propertyId)) {
                    return getSuggestionsForNameOfProperty(propertyName, parentNode);
                }
                // If processing value field of a property
                else if ("value".equals(propertyId)) {
                    return getSuggestionsForValueOfProperty(propertyName, parentNode);
                }
                // If processing type field of a property
                else if ("type".equals(propertyId)) {
                    return getSuggestionsForTypeOfProperty(propertyName, parentNode);
                }
                // If not processing name, value, or type field of a property
                else {
                    return noSuggestionsAvailable();
                }
            }
            else {
                return noSuggestionsAvailable();
            }
        }
        // If neither processing a JCR node nor a JCR property
        else {
            return noSuggestionsAvailable();
        }
    }

    protected AutoSuggesterResult noSuggestionsAvailable() {
        return new AutoSuggesterForConfigurationAppResult();
    }

    protected AutoSuggesterResult getSuggestionsForNameOfNode(Node node) {
        if (node == null) {
            return noSuggestionsAvailable();
        }

        try {
            Node parentNode = node.getParent();

            // If processing a node that has a parent node
            if (parentNode != null) {
                TypeDescriptor parentNodeTypeDescriptor = getNodeTypeDescriptor(parentNode);

                // If processing name field of a node whose parent has no class
                if (parentNodeTypeDescriptor == null) {
                    return getSuggestionsForNameOfNodeInUnknown(node, parentNode);
                }
                // If processing name field of a node whose parent is an array
                else if (parentNodeTypeDescriptor.isArray()) {
                    return getSuggestionsForNameOfNodeInArray(parentNode, parentNodeTypeDescriptor);
                }
                // If processing name field of a node whose parent is a collection
                else if (parentNodeTypeDescriptor.isCollection()) {
                    return getSuggestionsForNameOfNodeInCollection(parentNode, parentNodeTypeDescriptor);
                }
                // If processing name field of a node whose parent is a map
                else if (parentNodeTypeDescriptor.isMap()) {
                    return getSuggestionsForNameOfNodeInMap(parentNode, parentNodeTypeDescriptor);
                }
                // If processing name field of a node whose parent is a bean
                else {
                    return getSuggestionsForNameOfNodeInBean(node, parentNode, parentNodeTypeDescriptor);
                }
            }
            // If processing a node that does not have a parent
            else {
                return noSuggestionsAvailable();
            }
        } catch (RepositoryException ex) {
            log.warn("Could not get suggestions for name of node: " + ex);
            return noSuggestionsAvailable();
        }
    }

    /**
     * Get suggestions for node name when node has a parent but the parent's type is unknown.
     * This includes the case where the parent node is a folder.
     */
    protected AutoSuggesterResult getSuggestionsForNameOfNodeInUnknown(final Node node, final Node parentNode) {
        // QUESTION Are there other cases where we can recommend a name for the node when its parent type is unknown?

        if (node == null || parentNode == null) {
            return noSuggestionsAvailable();
        }

        try {
            // If node is a folder and node's parent is a folder
            if (NodeUtil.isNodeType(node, NodeTypes.Content.NAME) && NodeUtil.isNodeType(parentNode, NodeTypes.Content.NAME)) {
                String parentPath = parentNode.getPath();

                // If node's parent's path is available
                if (parentPath != null) {

                    // If node's parent is /modules/<moduleName>
                    if (parentPath.startsWith("/modules/") && parentPath.indexOf("/", "/modules/".length()) == -1) {

                        String nodeName = node.getName();

                        if (nodeName != null) {
                            // QUESTION Would it not be better to make suggestions based on existing subfolders under /modules/<moduleName>/ rather than hardcoding it?
                            final Collection<String> suggestions = getAllPossibleNewSubnodeNames(nodeName, parentNode, Arrays.asList(
                                    "apps", "templates", "dialogs", "commands", "fieldTypes", "virtualURIMapping", "renderers", "config"));

                            return new AutoSuggesterForConfigurationAppResult(suggestions != null && !suggestions.isEmpty(), suggestions, AutoSuggesterResult.STARTS_WITH, true, false);
                        }
                        else {
                            return noSuggestionsAvailable();
                        }
                    }
                    // If node's parent is not /modules/<moduleName>
                    else {
                        return noSuggestionsAvailable();
                    }
                }
                // If node's parent's path is not available
                else {
                    return noSuggestionsAvailable();
                }
            }
            // If node is not a folder or node's parent is not a folder
            else {
                return noSuggestionsAvailable();
            }
        } catch (RepositoryException ex) {
            log.warn("Could not get suggestions for name of node when type of parent is unknown: " + ex);
            return noSuggestionsAvailable();
        }
    }

    protected AutoSuggesterResult getSuggestionsForNameOfNodeInMap(Node parentNode, TypeDescriptor parentNodeTypeDescriptor) {
        // TODO
        // QUESTION Are there special cases where we can make suggestions for the name of node in a map?
        return noSuggestionsAvailable();
    }

    protected AutoSuggesterResult getSuggestionsForNameOfNodeInCollection(Node parentNode, TypeDescriptor parentNodeTypeDescriptor) {
        // TODO
        // QUESTION Are there special cases where we can make suggestions for the name of node in a collection?
        return noSuggestionsAvailable();
    }

    protected AutoSuggesterResult getSuggestionsForNameOfNodeInArray(Node parentNode, TypeDescriptor parentNodeTypeDescriptor) {
        // TODO
        // QUESTION Are there special cases where we can make suggestions for the name of node in an array?
        return noSuggestionsAvailable();
    }

    protected AutoSuggesterResult getSuggestionsForNameOfNodeInBean(final Node node, final Node parentNode, final TypeDescriptor parentNodeTypeDescriptor) {
        if (node == null || parentNode == null || parentNodeTypeDescriptor == null) {
            return noSuggestionsAvailable();
        }

        try {
            final Collection<String> possibleSubnodeNames = getAllPossibleSubnodeNames(parentNodeTypeDescriptor);
            if (possibleSubnodeNames != null) {
                String nodeName = node.getName();

                if (nodeName != null) {
                    final Collection<String> suggestions = getAllPossibleNewSubnodeNames(nodeName, parentNode, possibleSubnodeNames);

                    return new AutoSuggesterForConfigurationAppResult(suggestions != null && !suggestions.isEmpty(), suggestions, AutoSuggesterResult.STARTS_WITH, true, true);
                }
                else {
                    return noSuggestionsAvailable();
                }
            }
            else {
                return noSuggestionsAvailable();
            }
        } catch (RepositoryException ex) {
            log.warn("Could not get suggestions for name of node in bean: " + ex);
            return noSuggestionsAvailable();
        }
    }

    protected AutoSuggesterResult getSuggestionsForNameOfProperty(String propertyName, Node parentNode) {
        if (propertyName == null || parentNode == null) {
            return getSuggestionsForNameOfPropertyBasedOnJCROnly(propertyName, parentNode);
        }

        TypeDescriptor parentTypeDescriptor = getNodeTypeDescriptor(parentNode);

        // If we can get the type of the parent node
        if (parentTypeDescriptor != null) {
            Collection<String> possibleSubpropertyNames = getAllPossibleSubpropertyNames(parentTypeDescriptor);

            if (possibleSubpropertyNames != null) {
                Collection<String> suggestions = getAllPossibleNewSubpropertyNames(propertyName, parentNode, possibleSubpropertyNames);

                return new AutoSuggesterForConfigurationAppResult(suggestions != null && !suggestions.isEmpty(), suggestions, AutoSuggesterResult.STARTS_WITH, true, true);
            }
            else {
                return getSuggestionsForNameOfPropertyBasedOnJCROnly(propertyName, parentNode);
            }
        }
        // If we cannot get the type of the parent node
        else {
            return getSuggestionsForNameOfPropertyBasedOnJCROnly(propertyName, parentNode);
        }
    }

    /**
     * Try to get suggestions for name of property based on JCR only, when we cannot use the bean to get suggestions.
     * Always suggest "extends" and "class" unless the node already has it and it is not the one being named.
     */
    protected AutoSuggesterResult getSuggestionsForNameOfPropertyBasedOnJCROnly(String propertyName, Node parentNode) {
        if (propertyName == null || parentNode == null) {
            return noSuggestionsAvailable();
        }

        Collection<String> possibleSubpropertyNames = new HashSet<String>();
        possibleSubpropertyNames.add("class");
        possibleSubpropertyNames.add("extends");

        // Check if we should suggest the version property if property's parent is a folder /modules/<moduleName>
        try {
            // If parent node is a folder
            if (NodeUtil.isNodeType(parentNode, NodeTypes.Content.NAME)) {
                String parentPath = parentNode.getPath();

                // If property's parent is /modules/<moduleName>
                if (parentPath != null && parentPath.startsWith("/modules/") && parentPath.indexOf("/", "/modules/".length()) == -1) {
                    possibleSubpropertyNames.add("version");
                }
            }
        } catch (RepositoryException ex) {
            log.warn("Could not check if version property should be added as a suggestion: " + ex);
        }

        Collection<String> suggestions = getAllPossibleNewSubpropertyNames(propertyName, parentNode, possibleSubpropertyNames);

        return new AutoSuggesterForConfigurationAppResult(suggestions != null && !suggestions.isEmpty(), suggestions, AutoSuggesterResult.STARTS_WITH, true, true);
    }

    protected AutoSuggesterResult getSuggestionsForValueOfProperty(String propertyName, Node parentNode) {
        // TODO
        if (propertyName == null || parentNode == null) {
            return noSuggestionsAvailable();
        }

        // Get all values that may be useful for subsequent method calls
        TypeDescriptor parentTypeDescriptor = getNodeTypeDescriptor(parentNode);
        Class<?> parentClass = null;
        if (parentTypeDescriptor != null) {
            parentClass = parentTypeDescriptor.getType();
        }
        Property valueProperty = null;
        int valueJCRType = -1;
        try {
            valueProperty = parentNode.getProperty(propertyName);
            valueJCRType = valueProperty.getType();
        } catch (RepositoryException ex) {
            valueProperty = null;
            valueJCRType = -1;
        }
        PropertyTypeDescriptor valuePropertyTypeDescriptor = null;
        Class<?> valueClass = null;
        if (parentTypeDescriptor != null) {
            valuePropertyTypeDescriptor = getPropertyTypeDescriptor(propertyName, parentTypeDescriptor);

            if (valuePropertyTypeDescriptor != null) {
                TypeDescriptor valueTypeDescriptor = valuePropertyTypeDescriptor.getType();

                if (valueTypeDescriptor != null) {
                    valueClass = valueTypeDescriptor.getType();
                }
            }
        }

        AutoSuggesterResult autoSuggesterResult = null;

        // If suggest true/false for a boolean value
        if ((autoSuggesterResult = getSuggestionsForValueOfPropertyIfPropertyIsTypeBoolean(propertyName, parentNode, parentTypeDescriptor, valueProperty, valueJCRType, valuePropertyTypeDescriptor, valueClass)).suggestionsAvailable()) {
            return autoSuggesterResult;
        }
        // If suggest for a dialog reference
        else if ((autoSuggesterResult = getSuggestionsForValueOfPropertyIfPropertyIsDialogReference(propertyName, valueJCRType, parentClass)).suggestionsAvailable()) {
            return autoSuggesterResult;
        }
        // If suggest for a class reference
        else if ((autoSuggesterResult = getSuggestionsForValueOfPropertyIfPropertyIsClassReference(propertyName, parentNode, valueJCRType, parentClass, valuePropertyTypeDescriptor, valueClass)).suggestionsAvailable()) {
            return autoSuggesterResult;
        }
        // If no suggestions for value of property
        else {
            return noSuggestionsAvailable();
        }
    }

    /**
     * Get suggestions for property value according to the logic below, if it is a reference to a class.
     * - If parent has bean class
     * --| If property named "class"
     * --|-| If JCR String
     * --|-|-| Suggest based on parentNodeType determined from grandparent if possible, otherwise parent
     * --|-| Else if not JCR String
     * --|-|-- No suggestions
     * --| Else if property not named "class"
     * --|-- If property has bean type Class
     * --|---| If generic parameter gotten
     * --|---|-| Suggest based on generic parameter
     * --|---| Else if no generic parameter gotten
     * --|---|-- No suggestions
     * --|-- Else if property does not have bean type Class
     * --|---- No suggestions
     * - Else if parent does not have bean class
     * --- No suggestions
     */
    protected AutoSuggesterResult getSuggestionsForValueOfPropertyIfPropertyIsClassReference(String propertyName, Node parentNode, int valueJCRType, Class<?> parentClass, PropertyTypeDescriptor valuePropertyTypeDescriptor, Class<?> valueClass) {
        if (parentClass != null) {

            if ("class".equals(propertyName)) {
                if (valueJCRType == PropertyType.STRING) {
                    Class<?> mostGeneralNodeClass = null;
                    if (parentNode != null) {
                        mostGeneralNodeClass = getMostGeneralNodeClass(parentNode);
                    }
                    mostGeneralNodeClass = (mostGeneralNodeClass == null ? parentClass : mostGeneralNodeClass);

                    Collection<String> suggestions = getSubclassNames(mostGeneralNodeClass);

                    return new AutoSuggesterForConfigurationAppResult(suggestions != null && suggestions.size() > 0, suggestions, AutoSuggesterResult.CONTAINS, true, true);

                }
                else {
                    return noSuggestionsAvailable();
                }
            }
            else {
                if (valuePropertyTypeDescriptor != null && valueClass != null && ClassUtils.isAssignable(valueClass, Class.class)) {
                    Class<?> genericTypeParameter = getGenericTypeParameterOfClassType(valuePropertyTypeDescriptor);

                    if (genericTypeParameter != null) {
                        Collection<String> suggestions = getSubclassNames(genericTypeParameter);

                        return new AutoSuggesterForConfigurationAppResult(suggestions != null && suggestions.size() > 0, suggestions, AutoSuggesterResult.CONTAINS, true, true);
                    }
                    else {
                        return noSuggestionsAvailable();
                    }
                }
                else {
                    return noSuggestionsAvailable();
                }
            }
        }
        else {
            return noSuggestionsAvailable();
        }
    }

    /**
     * Get suggestions for property value according to the logic below, if it is a reference to a dialog.
     * - If can get bean type of parent
     * --| If parent bean type is subclass of ConfiguredTemplateDefinition
     * --|-| If property is "dialog" and property JCR type is String
     * --|-|-| Suggest dialogs
     * --|-| Else if property is not "dialog" or property JCR type is not String
     * --|-|-- Suggest dialogs
     * --| Else if parent bean type is subclass of ConfiguredActionDefinition
     * --|-| If property is "dialogName" and property JCR type is String
     * --|-|-| Suggest dialogs
     * --|-| Else if property is not "dialogName" or property JCR type is not String
     * --|-|-- No suggestions
     * --| Else if parent bean type is not any of the above types
     * --|-- No suggestions
     * - Else cannot get bean type of parent
     * --- No suggestions
     */
    protected AutoSuggesterResult getSuggestionsForValueOfPropertyIfPropertyIsDialogReference(String propertyName, int valueJCRType, Class<?> parentClass) {
        if (dialogDefinitionRegistry == null) {
            log.warn("Could not get suggestions for dialog reference because dialogDefinitionRegistry does not exist.");
            return noSuggestionsAvailable();
        }

        if (parentClass != null) {
            if (ClassUtils.isAssignable(parentClass, ConfiguredTemplateDefinition.class)) {
                if ("dialog".equals(propertyName) && valueJCRType == PropertyType.STRING) {
                    Collection<String> suggestions = dialogDefinitionRegistry.getRegisteredDialogNames();

                    return new AutoSuggesterForConfigurationAppResult(suggestions != null && !suggestions.isEmpty(), suggestions, AutoSuggesterResult.CONTAINS, true, true);
                }
                else {
                    return noSuggestionsAvailable();
                }
            }
            else if (ClassUtils.isAssignable(parentClass, ConfiguredActionDefinition.class)) {
                if ("dialogName".equals(propertyName) && valueJCRType == PropertyType.STRING) {
                    Collection<String> suggestions = dialogDefinitionRegistry.getRegisteredDialogNames();

                    return new AutoSuggesterForConfigurationAppResult(suggestions != null && !suggestions.isEmpty(), suggestions, AutoSuggesterResult.CONTAINS, true, true);
                }
                else {
                    return noSuggestionsAvailable();
                }
            }
            else {
                return noSuggestionsAvailable();
            }
        }
        else {
            return noSuggestionsAvailable();
        }
    }

    /**
     * Get suggestions for property value if it is a boolean according to the logic below, otherwise return a suggestions unavailable.
     * - If can get bean type of parent
     * --| If can get type of property from type of parent
     * --|-| If type of property from type of parent is Boolean and JCR type is Boolean or String
     * --|-|-| Suggest true/false
     * --|-| Else if type of property from type of parent is not Boolean or JCR type is not Boolean or String
     * --|-|-- No suggestions
     * --| Else if cannot get type of property from type of parent
     * --|-- If property has JCR type Boolean
     * --|---| Suggest true/false
     * --|-- Else if property does not have JCR type Boolean
     * --|---- No suggestions
     * - Else if cannot get bean type of parent
     * ---- If property has JCR type Boolean
     * -----| Suggest true/false
     * ---- Else if property does not have JCR type Boolean
     * ------ No suggestions
     */
    protected AutoSuggesterResult getSuggestionsForValueOfPropertyIfPropertyIsTypeBoolean(String propertyName, Node parentNode, TypeDescriptor parentTypeDescriptor, Property valueProperty, int valueJCRType, PropertyTypeDescriptor valuePropertyTypeDescriptor, Class<?> valueClass) {
        if (parentTypeDescriptor != null) {
            if (valueClass != null) {
                if (ClassUtils.isAssignable(valueClass, Boolean.class) && (valueJCRType == PropertyType.BOOLEAN || valueJCRType == PropertyType.STRING)) {
                    return new AutoSuggesterForConfigurationAppResult(true, Arrays.asList("true", "false"), AutoSuggesterResult.STARTS_WITH, true, true);
                }
                else {
                    return noSuggestionsAvailable();
                }
            }
            else {
                return getSuggestionsForValueOfPropertyIfPropertyIsTypeBooleanBasedOnJCROnly(valueProperty, valueJCRType);
            }
        }
        else {
            return getSuggestionsForValueOfPropertyIfPropertyIsTypeBooleanBasedOnJCROnly(valueProperty, valueJCRType);
        }
    }

    protected AutoSuggesterResult getSuggestionsForValueOfPropertyIfPropertyIsTypeBooleanBasedOnJCROnly(Property valueProperty, int valueJCRType) {
        if (valueProperty == null) {
            return noSuggestionsAvailable();
        }

        if (PropertyType.BOOLEAN == valueJCRType) {
            return new AutoSuggesterForConfigurationAppResult(true, Arrays.asList("true", "false"), AutoSuggesterResult.STARTS_WITH, true, true);
        }
        else {
            return noSuggestionsAvailable();
        }
    }

    /**
     * Get suggestion for JCR type of a property based on the logic below.
     * - If can get bean type of parent
     * --| If can get type of property from type of parent
     * --|-| If one of the Java types we know how to map to a JCR type
     * --|-|-| Suggest based on following map from Java to JCR type {String, Char, Class, Enum = String; Byte, Short, Int, Long = Long; Float, Double = Double; Boolean = Boolean}
     * --|-| Else if not one of the Java types we know how to map to a JCR type
     * --|-|-- If "class" or "extends"
     * --|-|---| Suggest String
     * --|-|-- Else if not class or extends
     * --|-|---- Suggest all JCR types
     * --| Else if cannot get type of property from type of parent
     * --|-- If "class" or "extends"
     * --|---| Suggest String
     * --|-- Else if not class or extends
     * --|---- Suggest all JCR types
     * - Else if cannot get bean type of parent
     * --- If "class" or "extends"
     * ----| Suggest String
     * --- Else if not class or extends
     * ----- Suggest all JCR types
     */
    protected AutoSuggesterResult getSuggestionsForTypeOfProperty(String propertyName, Node parentNode) {
        if (propertyName == null || parentNode == null) {
            return getSuggestionsForTypeOfPropertyBasedOnJCROnly(propertyName);
        }

        TypeDescriptor parentTypeDescriptor = getNodeTypeDescriptor(parentNode);

        // If we can get the type of the node containing the property
        if (parentTypeDescriptor != null) {
            PropertyTypeDescriptor valuePropertyTypeDescriptor = getPropertyTypeDescriptor(propertyName, parentTypeDescriptor);

            // If we can get the type of the property from the type of the node containing the property
            if (valuePropertyTypeDescriptor != null) {
                TypeDescriptor valueTypeDescriptor = valuePropertyTypeDescriptor.getType();

                if (valueTypeDescriptor != null) {
                    Class<?> propertyClass = valueTypeDescriptor.getType();

                    if (propertyClass != null) {

                        // If Java class String, Character, Class, or Enum, map to JCR type String
                        if (propertyClass.equals(String.class) || propertyClass.isEnum() || propertyClass.equals(Class.class) || ClassUtils.isAssignable(propertyClass, Character.class)) {
                            return new AutoSuggesterForConfigurationAppResult(true, Arrays.asList("String"), AutoSuggesterResult.STARTS_WITH, true, false);
                        }
                        // If Java class Boolean, map to JCR type Boolean
                        else if (ClassUtils.isAssignable(propertyClass, Boolean.class)) {
                            return new AutoSuggesterForConfigurationAppResult(true, Arrays.asList("Boolean"), AutoSuggesterResult.STARTS_WITH, true, false);
                        }
                        // If Java class Long, Integer, or Byte, map to JCR type Long
                        else if (ClassUtils.isAssignable(propertyClass, Long.class) || ClassUtils.isAssignable(propertyClass, Integer.class) || ClassUtils.isAssignable(propertyClass, Byte.class) || ClassUtils.isAssignable(propertyClass, Short.class)) {
                            return new AutoSuggesterForConfigurationAppResult(true, Arrays.asList("Long"), AutoSuggesterResult.STARTS_WITH, true, false);
                        }
                        // If Java class Double or Float, map to JCR type Double
                        else if (ClassUtils.isAssignable(propertyClass, Double.class) || ClassUtils.isAssignable(propertyClass, Float.class)) {
                            return new AutoSuggesterForConfigurationAppResult(true, Arrays.asList("Double"), AutoSuggesterResult.STARTS_WITH, true, false);
                        }
                        // If Java type that does not map to a JCR type
                        else {
                            return getSuggestionsForTypeOfPropertyBasedOnJCROnly(propertyName);
                        }
                    }
                    else {
                        return getSuggestionsForTypeOfPropertyBasedOnJCROnly(propertyName);
                    }
                }
                else {
                    return getSuggestionsForTypeOfPropertyBasedOnJCROnly(propertyName);
                }
            }
            // If we cannot get the type of the property from the type of the node containing the property
            else {
                return getSuggestionsForTypeOfPropertyBasedOnJCROnly(propertyName);
            }
        }
        // If we cannot get the type of the node containing the property
        else {
            return getSuggestionsForTypeOfPropertyBasedOnJCROnly(propertyName);
        }
    }

    protected AutoSuggesterResult getSuggestionsForTypeOfPropertyBasedOnJCROnly(String propertyName) {
        if (propertyName == null) {
            return noSuggestionsAvailable();
        }

        // If property name is "class" or "extends"
        if ("class".equals(propertyName) || "extends".equals(propertyName)) {
            return new AutoSuggesterForConfigurationAppResult(true, Arrays.asList("String"), AutoSuggesterResult.STARTS_WITH, true, false);
        } else {
            return new AutoSuggesterForConfigurationAppResult(true, Arrays.asList("String", "Boolean", "Long", "Double"), AutoSuggesterResult.STARTS_WITH, true, false);
        }
    }

    // UTILITY METHODS

    private Node getNodeFromJcrItemId(JcrItemId itemId) {
        if (itemId == null) {
            return null;
        }

        return SessionUtil.getNodeByIdentifier(itemId.getWorkspace(), itemId.getUuid());
    }

    /**
     * Get the type of a node. Take into account the class property of the node, class property of
     * ancestors, and location of the node within the configuration hierarchy. Take extends into
     * account. Take implementations mapped by ComponentProvider into account. Take into account
     * that some types cannot be types for nodes. Return null if the type cannot be deduced.
     */
    private TypeDescriptor getNodeTypeDescriptor(final Node node) {
        if (node == null) {
            return null;
        }

        NodeAndEntryTypeDescriptor nodeAndEntryTypeDescriptor = getNodeAndEntryTypeDescriptor(node);

        if (nodeAndEntryTypeDescriptor != null) {
            return nodeAndEntryTypeDescriptor.getTypeDescriptor();
        }
        else {
            return null;
        }
    }

    /**
     * Get the type of the node and the type of the entry if the node is an array, collection, or map.
     * Take into account the class property of the node, class property of ancestors, and location of
     * the node within the configuration hierarchy. Take extends into account. Take implementations
     * mapped by ComponentProvider into account. Take into account that some types cannot be types for
     * nodes. Return null if the type cannot be deduced.
     */
    private NodeAndEntryTypeDescriptor getNodeAndEntryTypeDescriptor(final Node node) {
        if (typeMapping == null) {
            log.warn("Could not get type for node because TypeMapping does not exist.");
            return null;
        }

        if (node == null) {
            return null;
        }

        // Take into account properties inherited due to extends
        final Node extendedNode = wrapNodeAndAncestorsForExtends(node);
        if (extendedNode == null) {
            return null;
        }

        try {
            // Try to get the type based on node's class property
            NodeAndEntryTypeDescriptor nodeAndEntryTypeDescriptor = getNodeAndEntryTypeDescriptorBasedOnClassProperty(extendedNode);

            // If we can get the type based on node's class property
            if (nodeAndEntryTypeDescriptor != null) {
                return nodeAndEntryTypeDescriptor;
            }
            // If we can't get the type based on node's class property
            else {
                // If node does not have class property or has a class property but we can't use it to get the type, try to get type based on parent
                Node parentNode = extendedNode.getParent();

                // If node has a parent
                if (parentNode != null) {

                    // Try to use path information to get the type of the node
                    nodeAndEntryTypeDescriptor = getImplementingNodeAndEntryTypeDescriptorBasedOnParentFolder(extendedNode, parentNode);

                    // If we are able to use path information to get the type of the node
                    if (nodeAndEntryTypeDescriptor != null) {
                        return nodeAndEntryTypeDescriptor;
                    }
                    // If we are not able to use path information to get the type of the node
                    else {
                        // Recursively get type of parent
                        NodeAndEntryTypeDescriptor parentNodeAndEntryTypeDescriptor = getNodeAndEntryTypeDescriptor(parentNode);

                        // If we can get the type of the parent using recursion
                        if (parentNodeAndEntryTypeDescriptor != null) {
                            TypeDescriptor parentTypeDescriptor = parentNodeAndEntryTypeDescriptor.getTypeDescriptor();

                            if (parentTypeDescriptor != null) {

                                // If parent is array, collection, or map
                                if (parentTypeDescriptor.isArray() || parentTypeDescriptor.isCollection() || parentTypeDescriptor.isMap()) {
                                    TypeDescriptor parentEntryTypeDescriptor = parentNodeAndEntryTypeDescriptor.getEntryTypeDescriptor();

                                    // QUESTION How do we deal with a parent whose type is a Collection<Collection<T>>?
                                    // If parent's entry type is known and is for a content node
                                    if (parentEntryTypeDescriptor != null && isTypeDescriptorForContentNode(parentEntryTypeDescriptor)) {
                                        return new NodeAndEntryTypeDescriptor(getImplementingTypeDescriptor(parentEntryTypeDescriptor), null);
                                    }
                                    // If parent's entry type is either not known or is not for a content node
                                    else {
                                        return null;
                                    }
                                }
                                // If parent is not array, collection, or map, and thus assumed to be a bean
                                else {
                                    PropertyTypeDescriptor nodePropertyTypeDescriptor = getPropertyTypeDescriptor(extendedNode.getName(), parentTypeDescriptor);

                                    // If can use the name of the node to get the type from the parent
                                    if (nodePropertyTypeDescriptor != null) {
                                        TypeDescriptor nodeTypeDescriptor = nodePropertyTypeDescriptor.getType();

                                        // If type gotten from the parent is for a content node
                                        if (nodeTypeDescriptor != null && isTypeDescriptorForContentNode(nodeTypeDescriptor)) {

                                            // If the type of the node is an array, collection, or map
                                            if (nodeTypeDescriptor.isArray() || nodeTypeDescriptor.isCollection() || nodeTypeDescriptor.isMap()) {
                                                return new NodeAndEntryTypeDescriptor(getImplementingTypeDescriptor(nodeTypeDescriptor),
                                                        nodePropertyTypeDescriptor.getCollectionEntryType());
                                            }
                                            // If the type of the node is not array, collection, or map
                                            else {
                                                return new NodeAndEntryTypeDescriptor(getImplementingTypeDescriptor(nodeTypeDescriptor), null);
                                            }
                                        }
                                        // If type gotten from the parent is not for a content node
                                        else {
                                            return null;
                                        }
                                    }
                                    // If cannot use the name of the node to get the type from the parent
                                    else {
                                        return null;
                                    }
                                }
                            }
                            else {
                                return null;
                            }
                        }
                        // If cannot get the type of the parent using recursion
                        else {
                            return null;
                        }
                    }
                }
                // If node does not have a parent
                else {
                    return null;
                }
            }

        } catch (RepositoryException ex) {
            log.warn("Could not get TypeDescriptor for node: " + ex);
            return null;
        }
    }

    /**
     * Try to use class property of a node to get its type. Take extends into account. Take implementations
     * mapped by ComponentProvider into account. Returns null if the node does not have a class property or
     * its value is invalid.
     */
    private NodeAndEntryTypeDescriptor getNodeAndEntryTypeDescriptorBasedOnClassProperty(Node node) {
        if (typeMapping == null) {
            log.warn("Could not get type of node based on class property because TypeMapping does not exist.");
            return null;
        }

        if (node == null) {
            return null;
        }

        // Take into account properties inherited due to extends
        final Node extendedNode = wrapNodeAndAncestorsForExtends(node);
        if (extendedNode == null) {
            return null;
        }

        try {
            // If node has a class property
            if (extendedNode.hasProperty("class")) {
                Property classProperty = extendedNode.getProperty("class");

                // If node has class property and we can get the class property value
                if (classProperty != null) {
                    try {
                        TypeDescriptor nodeTypeDescriptor = typeMapping.getTypeDescriptor(Class.forName(classProperty.getString()));

                        // If we can get the TypeDescriptor based on the class
                        if (nodeTypeDescriptor != null) {
                            return new NodeAndEntryTypeDescriptor(getImplementingTypeDescriptor(nodeTypeDescriptor), null);
                        }
                        // If we can't get the TypeDescriptor based on the class
                        else {
                            return null;
                        }
                    } catch (ClassNotFoundException ex) {
                        log.warn("Could not get TypeDescriptor based on invalid value for class property: " + ex);
                        return null;
                    }
                }
                // If node has class property but we can't get the value
                else {
                    return null;
                }
            }
            // If node does not have a class property
            else {
                return null;
            }
        } catch (RepositoryException ex) {
            log.warn("Could not get TypeDescriptor for node based on class property: " + ex);
            return null;
        }
    }

    /**
     * Try to use path information to get the type of a node. For example, if all a node's ancestors are
     * folders and node is in /modules/<moduleName>/apps/, then we can assume it is a ConfiguredAppDescriptor.
     * Similarly for templates, dialogs, etc. Take implementations mapped by ComponentProvider into account.
     * Returns null if the type cannot be guessed based on folders.
     */
    private NodeAndEntryTypeDescriptor getImplementingNodeAndEntryTypeDescriptorBasedOnParentFolder(Node node, Node parentNode) {
        if (node == null || parentNode == null) {
            return null;
        }

        NodeAndEntryTypeDescriptor nodeAndEntryTypeDescriptor = getUnimplementingNodeAndEntryTypeDescriptorBasedOnParentFolder(node, parentNode);

        if (nodeAndEntryTypeDescriptor != null) {
            TypeDescriptor nodeTypeDescriptor = nodeAndEntryTypeDescriptor.getTypeDescriptor();
            TypeDescriptor entryTypeDescriptor = nodeAndEntryTypeDescriptor.getEntryTypeDescriptor();

            TypeDescriptor implementingNodeTypeDescriptor = (nodeTypeDescriptor == null ? null : getImplementingTypeDescriptor(nodeTypeDescriptor));
            TypeDescriptor implementingEntryTypeDescriptor = (entryTypeDescriptor == null ? null : getImplementingTypeDescriptor(entryTypeDescriptor));

            if (implementingNodeTypeDescriptor != null) {
                return new NodeAndEntryTypeDescriptor(implementingNodeTypeDescriptor, implementingEntryTypeDescriptor);
            }
            else {
                return null;
            }
        }
        else {
            return null;
        }
    }

    /**
     * Try to use path information to get the type of a node. For example, if all a node's ancestors are
     * folders and node is in /modules/<moduleName>/apps/, then we can assume it is a ConfiguredAppDescriptor.
     * Similarly for templates, dialogs, etc. Does not take implementations mapped by ComponentProvider into account.
     * Returns null if the type cannot be guessed based on folders.
     */
    private NodeAndEntryTypeDescriptor getUnimplementingNodeAndEntryTypeDescriptorBasedOnParentFolder(Node node, Node parentNode) {
        if (typeMapping == null) {
            log.warn("Could not get type of node based on path because TypeMapping does not exist.");
            return null;
        }

        if (node == null || parentNode == null) {
            return null;
        }

        try {
            // If node is a content node and node's parent is a folder and thus all its ancestors are folders
            if (NodeUtil.isNodeType(node, NodeTypes.ContentNode.NAME) && NodeUtil.isNodeType(parentNode, NodeTypes.Content.NAME)) {
                String nodePath = node.getPath();
                if (nodePath == null) {
                    return null;
                }

                // If node is in /modules/
                if (nodePath.startsWith("/modules/")) {
                    int indexOfModuleSubfolderNameEnd = nodePath.indexOf("/", "/modules/".length());

                    // If node is in a subfolder of /modules/
                    if (indexOfModuleSubfolderNameEnd != -1) {
                        String nodePathStartingAfterSubfolderOfModule = nodePath.substring(indexOfModuleSubfolderNameEnd + 1);

                        // If node is in /modules/<moduleName>/apps/
                        if (nodePathStartingAfterSubfolderOfModule.startsWith("apps/")) {
                            return new NodeAndEntryTypeDescriptor(typeMapping.getTypeDescriptor(AppDescriptor.class), null);
                        }
                        // If node is in /modules/<moduleName>/templates/
                        else if (nodePathStartingAfterSubfolderOfModule.startsWith("templates/")) {
                            return new NodeAndEntryTypeDescriptor(typeMapping.getTypeDescriptor(TemplateDefinition.class), null);
                        }
                        // If node is in /modules/<moduleName>/dialogs/
                        else if (nodePathStartingAfterSubfolderOfModule.startsWith("dialogs/")) {
                            return new NodeAndEntryTypeDescriptor(typeMapping.getTypeDescriptor(FormDialogDefinition.class), null);
                        }
                        // If node is in /modules/<moduleName>/commands/
                        else if (nodePathStartingAfterSubfolderOfModule.startsWith("commands/")) {
                            return new NodeAndEntryTypeDescriptor(typeMapping.getTypeDescriptor(Command.class), null);
                        }
                        // If node is in /modules/<moduleName>/fieldTypes/
                        else if (nodePathStartingAfterSubfolderOfModule.startsWith("fieldTypes/")) {
                            return new NodeAndEntryTypeDescriptor(typeMapping.getTypeDescriptor(FieldTypeDefinition.class), null);
                        }
                        // If node is in /modules/<moduleName>/virtualURIMapping/
                        else if (nodePathStartingAfterSubfolderOfModule.startsWith("virtualURIMapping/")) {
                            return new NodeAndEntryTypeDescriptor(typeMapping.getTypeDescriptor(VirtualURIMapping.class), null);
                        }
                        // If node is in /modules/<moduleName>/renderers/
                        else if (nodePathStartingAfterSubfolderOfModule.startsWith("renderers/")) {
                            return new NodeAndEntryTypeDescriptor(typeMapping.getTypeDescriptor(Renderer.class), null);
                        }
                        // QUESTION Are there more cases when we can tell the type of a node based on its path?
                        // If node is not in a recognized subfolder of /modules/<moduleName>/
                        else {
                            return null;
                        }
                    }
                    // If node is directly under /modules/
                    else {
                        return null;
                    }
                }
                // If node is not in /modules/
                else {
                    return null;
                }
            }
            // If node is not a content node or node's parent is not a folder
            else {
                return null;
            }
        } catch (RepositoryException ex) {
            log.warn("Could not get TypeDescriptor for node based on path: " + ex);
            return null;
        }
    }

    private boolean isTypeDescriptorForContentNode(TypeDescriptor typeDescriptor) {
        if (typeDescriptor == null) {
            return false;
        }

        Class<?> clazz = typeDescriptor.getType();
        if (clazz == null) {
            return false;
        }

        // QUESTION Is there a better way to check that a class is for a bean?
        return !(ClassUtils.isPrimitiveOrWrapper(clazz) || clazz.isEnum() || clazz.equals(String.class) || clazz.equals(Class.class));
    }

    private Node wrapNodeAndAncestorsForExtends(Node node) {
        if (node == null) {
            return null;
        }

        try {
            if (!NodeUtil.isWrappedWith(node, ExtendingNodeAndAncestorsWrapper.class)) {
                return new ExtendingNodeAndAncestorsWrapper(node);
            }
            else {
                return node;
            }
        } catch (RepositoryException ex) {
            log.warn("Could not wrap node in ExtendingNodeAndAncestorsWrapper: " + ex);
            return null;
        }
    }

    /**
     * Same as TypeDescriptor.getPropertyTypeDescriptor(String propertyName) except we compensate for Oracle Java bug 4275879
     * by looking for property recursively in super-interfaces of an interface as well.
     */
    private PropertyTypeDescriptor getPropertyTypeDescriptor(String propertyName, TypeDescriptor parentTypeDescriptor) {
        if (typeMapping == null) {
            log.warn("Could not get property type descriptor from type descriptor because TypeMapping does not exist.");
            return null;
        }

        if (propertyName == null || parentTypeDescriptor == null) {
            return null;
        }

        TypeDescriptor implementedParentTypeDescriptor = getImplementingTypeDescriptor(parentTypeDescriptor);
        if (implementedParentTypeDescriptor == null) {
            return null;
        }

        PropertyTypeDescriptor propertyTypeDescriptor = implementedParentTypeDescriptor.getPropertyTypeDescriptor(propertyName, typeMapping);

        // If we are able to get the PropertyTypeDescriptor through type mapping
        if (propertyTypeDescriptor != null) {
            return propertyTypeDescriptor;
        }
        // If we are not able to get the PropertyTypeDescriptor through type mapping
        else {
            Class<?> parentClass = implementedParentTypeDescriptor.getType();

            // If parent's type is an interface, property may still be inherited from a super-interface
            if (parentClass != null && parentClass.isInterface()) {
                Class<?>[] superInterfaceClasses = parentClass.getInterfaces();

                if (superInterfaceClasses != null) {

                    // Recursively look for propertyTypeDescriptor in super-interfaces
                    for (Class<?> superInterfaceClass : superInterfaceClasses) {
                        TypeDescriptor superInterfaceTypeDescriptor = typeMapping.getTypeDescriptor(superInterfaceClass);

                        if (superInterfaceTypeDescriptor != null) {
                            PropertyTypeDescriptor superInterfacePropertyTypeDescriptor = getPropertyTypeDescriptor(propertyName, superInterfaceTypeDescriptor);

                            if (superInterfacePropertyTypeDescriptor != null) {
                                return superInterfacePropertyTypeDescriptor;
                            }
                        }
                    }

                    // Did not find propertyTypeDescriptor in super-interfaces
                    return null;
                }
                else {
                    return null;
                }
            }
            // If parent's type is not known or not an interface
            else {
                return null;
            }
        }
    }

    /**
     * If typeDescriptor is for an interface and the componentProvider maps it to an implementing type,
     * return the typeDescriptor of the implementing type. If not, then return original typeDescriptor.
     */
    private TypeDescriptor getImplementingTypeDescriptor(final TypeDescriptor typeDescriptor) {
        if (typeMapping == null) {
            log.warn("Could not get implementing type descriptor because TypeMapping does not exist.");
            return typeDescriptor;
        }

        if (typeDescriptor == null) {
            return null;
        }

        try {
            Class<?> interfaceClass = typeDescriptor.getType();

            if (interfaceClass != null) {
                Class<?> implementingClass = Components.getComponentProvider().getImplementation(interfaceClass);

                if (implementingClass != null) {
                    TypeDescriptor implementingTypeDescriptor = typeMapping.getTypeDescriptor(implementingClass);

                    if (implementingTypeDescriptor != null) {
                        return implementingTypeDescriptor;
                    }
                    else {
                        return typeDescriptor;
                    }
                }
                else {
                    return typeDescriptor;
                }
            }
            else {
                log.warn("Could not get implementing type descriptor because original type descriptor does not specify a class.");
                return typeDescriptor;
            }
        } catch (ClassNotFoundException ex) {
            log.warn("Could not get implementing type descriptor because could not get implementing class: " + ex);
            return typeDescriptor;
        }
    }

    private Collection<String> getAllPossibleSubnodeNames(TypeDescriptor nodeTypeDescriptor) {
        if (nodeTypeDescriptor == null) {
            return null;
        }

        return getAllPossibleSubnodeAndSubpropertyNames(nodeTypeDescriptor, true);
    }

    private Collection<String> getAllPossibleSubpropertyNames(TypeDescriptor nodeTypeDescriptor) {
        if (nodeTypeDescriptor == null) {
            return null;
        }

        return getAllPossibleSubnodeAndSubpropertyNames(nodeTypeDescriptor, false);
    }

    /**
     * For a node which is a bean, get all possible subnode or subproperty names. Use the implementing
     * class mapped to the type by ComponentProvider to deduce the subnode or subproperty names. Take
     * into consideration that "extends" and "class" are always possible property names. getSubnode is
     * true to get subnode names, false to get property names. Return null if the type of the node is
     * not a bean.
     */
    private Collection<String> getAllPossibleSubnodeAndSubpropertyNames(TypeDescriptor nodeTypeDescriptor, boolean getSubnode) {
        if (typeMapping == null) {
            log.warn("Could not get subnode names because TypeMapping does not exist.");
            return null;
        }

        if (nodeTypeDescriptor == null) {
            return null;
        }

        TypeDescriptor implementedNodeTypeDescriptor = getImplementingTypeDescriptor(nodeTypeDescriptor);
        if (implementedNodeTypeDescriptor == null) {
            return null;
        }

        // QUESTION Is there a better way to check that a TypeDescriptor is for a bean?
        // If the node is not a bean
        if (implementedNodeTypeDescriptor.isArray() || implementedNodeTypeDescriptor.isCollection() || implementedNodeTypeDescriptor.isMap()) {
            return null;
        }
        // If the node is a bean
        else {
            Map<String, PropertyTypeDescriptor> subnodeOrSubpropertyPropertyTypeDescriptors = getAllPropertyTypeDescriptors(implementedNodeTypeDescriptor);

            // If we can get the PropertyTypeDescriptors of subnodes and sub-properties
            if (subnodeOrSubpropertyPropertyTypeDescriptors != null) {
                Collection<String> possibleSubnodeOrSubpropertyNames = new HashSet<String>();

                for (PropertyTypeDescriptor subnodeOrSubpropertyPropertyTypeDescriptor : subnodeOrSubpropertyPropertyTypeDescriptors.values()) {
                    TypeDescriptor subnodeOrSubpropertyTypeDescriptor = subnodeOrSubpropertyPropertyTypeDescriptor.getType();

                    if (subnodeOrSubpropertyTypeDescriptor != null) {
                        String propertyName = subnodeOrSubpropertyPropertyTypeDescriptor.getName();

                        if (propertyName != null) {

                            if ((getSubnode && isTypeDescriptorForContentNode(subnodeOrSubpropertyTypeDescriptor))
                                    || (!getSubnode && !isTypeDescriptorForContentNode(subnodeOrSubpropertyTypeDescriptor))) {
                                possibleSubnodeOrSubpropertyNames.add(propertyName);
                            }
                        }
                    }
                }

                // "class" and "extends" are always possible property names
                if (!getSubnode) {
                    possibleSubnodeOrSubpropertyNames.add("class");
                    possibleSubnodeOrSubpropertyNames.add("extends");
                }

                return possibleSubnodeOrSubpropertyNames;
            }
            // If we cannot get the PropertyTypeDescriptors of subnodes and sub-properties
            else {
                return null;
            }
        }
    }

    private Collection<String> getAllPossibleNewSubnodeNames(String subnodeName, Node parentNode, Collection<String> possibleSubnodeNames) {
        if (subnodeName == null || parentNode == null || possibleSubnodeNames == null) {
            return null;
        }

        return getAllPossibleNewSubnodeOrSubpropertyNames(subnodeName, parentNode, possibleSubnodeNames, true);
    }

    private Collection<String> getAllPossibleNewSubpropertyNames(String subpropertyName, Node parentNode, Collection<String> possibleSubpropertyNames) {
        if (subpropertyName == null || parentNode == null || possibleSubpropertyNames == null) {
            return null;
        }

        return getAllPossibleNewSubnodeOrSubpropertyNames(subpropertyName, parentNode, possibleSubpropertyNames, false);
    }

    /**
     * Get all names from possibleSubnodeNames that are not already subnodes of parentNode.
     * If one of the names matches the current node's name and the current node is a subnode
     * of parentNode, include it also. getSubnode is true to get node names, false to get
     * property names.
     */
    private Collection<String> getAllPossibleNewSubnodeOrSubpropertyNames(String subnodeOrSubpropertyName, Node parentNode, Collection<String> possibleSubnodeOrSubpropertyNames, boolean getSubnode) {
        if (subnodeOrSubpropertyName == null || parentNode == null || possibleSubnodeOrSubpropertyNames == null) {
            return null;
        }

        Collection<String> possibleNewSubnodeOrSubpropertyNames = new HashSet<String>();

        try {
            // Add all names in possibleSubnodeOrSubpropertyNames that are not already subnodes or sub-properties of parentNode
            for (String possibleSubnodeOrSubpropertyName : possibleSubnodeOrSubpropertyNames) {

                if ((getSubnode && !parentNode.hasNode(possibleSubnodeOrSubpropertyName))
                        || (!getSubnode && !parentNode.hasProperty(possibleSubnodeOrSubpropertyName))) {
                    possibleNewSubnodeOrSubpropertyNames.add(possibleSubnodeOrSubpropertyName);
                }
            }

            // Add current node or property name as well if it is one of the possibleSubnodeOrSubpropertyNames and is a subnode or sub-property of parentNode
            if (possibleSubnodeOrSubpropertyNames.contains(subnodeOrSubpropertyName)) {
                if ((getSubnode && parentNode.hasNode(subnodeOrSubpropertyName))
                        || (!getSubnode && parentNode.hasProperty(subnodeOrSubpropertyName))) {
                    possibleNewSubnodeOrSubpropertyNames.add(subnodeOrSubpropertyName);
                }
            }

            return possibleNewSubnodeOrSubpropertyNames;
        } catch (RepositoryException ex) {
            log.warn("Could not get nonexisting subnode names: " + ex);
            return null;
        }
    }

    /**
     * Same as TypeDescriptor.getPropertyTypeDescriptors() except we compensate for Oracle Java bug 4275879
     * by looking for properties recursively in super-interfaces of an interface as well.
     */
    private Map<String, PropertyTypeDescriptor> getAllPropertyTypeDescriptors(TypeDescriptor parentTypeDescriptor) {
        if (typeMapping == null) {
            log.warn("Could not get all property type descriptors from type descriptor because TypeMapping does not exist.");
            return null;
        }

        if (parentTypeDescriptor == null) {
            return null;
        }

        TypeDescriptor implementedParentTypeDescriptor = getImplementingTypeDescriptor(parentTypeDescriptor);
        if (implementedParentTypeDescriptor == null) {
            return null;
        }

        Map<String, PropertyTypeDescriptor> propertyTypeDescriptors = implementedParentTypeDescriptor.getPropertyDescriptors(typeMapping);

        if (propertyTypeDescriptors != null) {
            Class<?> parentClass = implementedParentTypeDescriptor.getType();

            // If parent is an interface
            if (parentClass != null && parentClass.isInterface()) {
                Class<?>[] superInterfaceClasses = parentClass.getInterfaces();

                if (superInterfaceClasses != null) {
                    Map<String, PropertyTypeDescriptor> collectedPropertyTypeDescriptors = new HashMap<String, PropertyTypeDescriptor>();
                    collectedPropertyTypeDescriptors.putAll(propertyTypeDescriptors);

                    // Recursively look for propertyTypeDescriptor in super-interfaces
                    for (Class<?> superInterfaceClass : superInterfaceClasses) {
                        TypeDescriptor superInterfaceTypeDescriptor = typeMapping.getTypeDescriptor(superInterfaceClass);

                        if (superInterfaceTypeDescriptor != null) {
                            Map<String, PropertyTypeDescriptor> superInterfacePropertyTypeDescriptors = getAllPropertyTypeDescriptors(superInterfaceTypeDescriptor);

                            if (superInterfacePropertyTypeDescriptors != null) {
                                for (Map.Entry<String, PropertyTypeDescriptor> superInterfacePropertyTypeDescriptor : superInterfacePropertyTypeDescriptors.entrySet()) {
                                    if (!collectedPropertyTypeDescriptors.containsKey(superInterfacePropertyTypeDescriptor.getKey())) {
                                        collectedPropertyTypeDescriptors.put(superInterfacePropertyTypeDescriptor.getKey(), superInterfacePropertyTypeDescriptor.getValue());
                                    }
                                }
                            }
                        }
                    }

                    return collectedPropertyTypeDescriptors;
                }
                else {
                    return propertyTypeDescriptors;
                }
            }
            // If parent class is not known or parent is not an interface
            else {
                return propertyTypeDescriptors;
            }
        }
        else {
            return null;
        }
    }

    /**
     * For a property of type Class<T> where T is either a raw type, parameterized type or a wildcard
     * type with one or more upper bounds, return either T if T is a raw or parameterized type or the
     * first upper bound. If parameter does not fit above description, return null.
     */
    private Class<?> getGenericTypeParameterOfClassType(PropertyTypeDescriptor propertyTypeDescriptor) {
        if (propertyTypeDescriptor == null) {
            return null;
        }

        Method writeMethod = propertyTypeDescriptor.getWriteMethod();

        // If we can get the write method for the property
        if (writeMethod != null) {
            Type[] parameterTypes = writeMethod.getGenericParameterTypes();

            // If the write method has exactly one parameter
            if (parameterTypes != null && parameterTypes.length == 1) {

                // If the single parameter to write method is a parameterized type (like Class<? extend T>)
                if (parameterTypes[0] instanceof ParameterizedType) {
                    ParameterizedType parameterizedType = (ParameterizedType) parameterTypes[0];
                    Type parameterRawType = parameterizedType.getRawType();
                    Type[] genericTypeParameters = parameterizedType.getActualTypeArguments();

                    // If the single parameter is of type Class<T> where T can be raw or parameterized type or wildcard
                    if (parameterRawType != null && parameterRawType instanceof Class && ClassUtils.isAssignable((Class<?>) parameterRawType, Class.class)
                            && genericTypeParameters != null && genericTypeParameters.length == 1) {
                        Type genericTypeParameter = genericTypeParameters[0];

                        // T is a parameterized type like List<String>
                        if (genericTypeParameter instanceof ParameterizedType) {
                            Type genericTypeParameterRawType = ((ParameterizedType) genericTypeParameter).getRawType();

                            if (genericTypeParameterRawType instanceof Class) {
                                return (Class<?>) genericTypeParameterRawType;
                            }
                            else {
                                return null;
                            }
                        }
                        // T is a wildcard, with possible upper and lower bounds
                        else if (genericTypeParameter instanceof WildcardType) {
                            WildcardType wildcardType = (WildcardType) genericTypeParameter;

                            // If one or more upper bounds
                            if (wildcardType.getUpperBounds().length > 0) {
                                // QUESTION Is there a better way to handle multiple upper bounds?
                                Type firstUpperBound = wildcardType.getUpperBounds()[0];

                                if (firstUpperBound instanceof Class) {
                                    return (Class<?>) firstUpperBound;
                                }
                                else {
                                    return null;
                                }
                            }
                            // If no upper bounds
                            else {
                                return null;
                            }
                        }
                        // T is a raw type
                        else if (genericTypeParameter instanceof Class) {
                            return (Class<?>) genericTypeParameter;
                        }
                        // T is neither a wildcard nor raw type nor a parameterized type
                        else {
                            return null;
                        }
                    }
                    // If the single parameter is not of type Class<T> where T can be raw or parameterized type or wildcard
                    else {
                        return null;
                    }
                }
                // If the single parameter to write method is not a parameterized type
                else {
                    return null;
                }
            }
            // If the write method does not have exactly one parameter
            else {
                return null;
            }
        }
        // We cannot get the write method
        else {
            return null;
        }
    }

    /**
     * Gets the names of all subclasses of the class parameter from the package that the
     * reflector is configured with. Includes the name of the class parameter itself.
     * Excludes inner classes.
     */
    private Collection<String> getSubclassNames(Class<?> clazz) {
        if (clazz == null) {
            return null;
        }

        Set<?> subclasses = reflections.getSubTypesOf(clazz);

        if (subclasses != null) {
            Collection<String> subclassNames = new HashSet<String>();
            subclassNames.add(clazz.getName());

            for (Object subclassObj : subclasses) {
                Class<?> subclass = (Class<?>) subclassObj;

                if (!ClassUtils.isInnerClass(subclass)) {
                    subclassNames.add(subclass.getName());
                }

            }

            return subclassNames;
        }
        else {
            return null;
        }
    }

    /**
     * Get the bean class of the node based on its parent bean's property associated with it.
     * Return null if can't get a bean class associated with node from parent.
     */
    private Class<?> getMostGeneralNodeClass(Node node) {
        if (node == null) {
            return null;
        }

        try {
            Node parentNode = node.getParent();

            // If can get parent
            if (parentNode != null) {

                NodeAndEntryTypeDescriptor nodeAndEntryTypeDescriptor = getUnimplementingNodeAndEntryTypeDescriptorBasedOnParentFolder(node, parentNode);

                // If can get type based on parent folder
                if (nodeAndEntryTypeDescriptor != null) {
                    TypeDescriptor nodeTypeDescriptor = nodeAndEntryTypeDescriptor.getTypeDescriptor();

                    if (nodeTypeDescriptor != null) {
                        return nodeTypeDescriptor.getType();
                    }
                    else {
                        return null;
                    }
                }
                // If cannot get type based on parent folder
                else {
                    NodeAndEntryTypeDescriptor parentNodeAndEntryTypeDescriptor = getNodeAndEntryTypeDescriptor(parentNode);

                    // If can get type of parent
                    if (parentNodeAndEntryTypeDescriptor != null) {
                        TypeDescriptor parentTypeDescriptor = parentNodeAndEntryTypeDescriptor.getTypeDescriptor();

                        if (parentTypeDescriptor != null) {

                            // If parent is array, collection, or map
                            if (parentTypeDescriptor.isArray() || parentTypeDescriptor.isCollection() || parentTypeDescriptor.isMap()) {
                                TypeDescriptor parentEntryTypeDescriptor = parentNodeAndEntryTypeDescriptor.getEntryTypeDescriptor();

                                // If parent's entry type is known and is for a content node mapped to a bean
                                if (parentEntryTypeDescriptor != null && isTypeDescriptorForContentNode(parentEntryTypeDescriptor)
                                        && !parentEntryTypeDescriptor.isArray() && !parentEntryTypeDescriptor.isCollection() && !parentEntryTypeDescriptor.isMap()) {
                                    return parentEntryTypeDescriptor.getType();
                                }
                                // If parent's entry type is either not known or is not for a content node mapped to a bean
                                else {
                                    return null;
                                }
                            }
                            // If parent is not array, collection, or map, and thus assumed to be a bean
                            else {
                                PropertyTypeDescriptor nodePropertyTypeDescriptor = getPropertyTypeDescriptor(node.getName(), parentTypeDescriptor);

                                // If can use the name of the node to get the type from the parent
                                if (nodePropertyTypeDescriptor != null) {
                                    TypeDescriptor nodeTypeDescriptor = nodePropertyTypeDescriptor.getType();

                                    // If type gotten from the parent is for a content node mapped to a bean
                                    if (nodeTypeDescriptor != null && isTypeDescriptorForContentNode(nodeTypeDescriptor)
                                            && !nodeTypeDescriptor.isArray() && !nodeTypeDescriptor.isCollection() && !nodeTypeDescriptor.isMap()) {
                                        return nodeTypeDescriptor.getType();
                                    }
                                    // If type gotten from the parent is not for a content node mapped to a bean
                                    else {
                                        return null;
                                    }
                                }
                                // If cannot use the name of the node to get the type from the parent
                                else {
                                    return null;
                                }
                            }
                        }
                        else {
                            return null;
                        }
                    }
                    // If cannot get type of the parent
                    else {
                        return null;
                    }
                }
            }
            // If cannot get parent
            else {
                return null;
            }
        } catch (RepositoryException ex) {
            log.warn("Could not get the property class of the node: " + ex);
            return null;
        }
    }

    /**
     * Convenient adapter for AutoSuggesterResult.
     */
    private static class AutoSuggesterForConfigurationAppResult implements AutoSuggesterResult {
        private boolean suggestionsAvailable;
        private Collection<String> suggestions;
        private int matchMethod;
        boolean showMismatchedSuggestions;
        boolean showErrorHighlighting;

        public AutoSuggesterForConfigurationAppResult(boolean suggestionsAvailable, Collection<String> suggestions, int matchMethod, boolean showMismatchedSuggestions, boolean showErrorHighlighting) {
            this.suggestionsAvailable = suggestionsAvailable;
            this.suggestions = suggestions;
            this.matchMethod = matchMethod;
            this.showMismatchedSuggestions = showMismatchedSuggestions;
            this.showErrorHighlighting = showErrorHighlighting;
        }

        /**
         * The result constructed by default has no suggestions available.
         */
        public AutoSuggesterForConfigurationAppResult() {
            this(false, null, STARTS_WITH, false, false);
        }

        @Override
        public boolean suggestionsAvailable() {
            if (!suggestionsAvailable) {
                return false;
            }
            else if (suggestions == null || suggestions.isEmpty()) {
                return false;
            }
            else {
                return true;
            }
        }

        @Override
        public Collection<String> getSuggestions() {
            return suggestions;
        }

        @Override
        public int getMatchMethod() {
            return matchMethod;
        }

        @Override
        public boolean showMismatchedSuggestions() {
            return showMismatchedSuggestions;
        }

        @Override
        public boolean showErrorHighlighting() {
            return showErrorHighlighting;
        }
    }

    /**
     * Wrapper that wraps both the node and its ancestors in {@link ExtendingNodeWrapper}, taking
     * into account items that may be inherited due to nodes extended by ancestors.
     */
    private static class ExtendingNodeAndAncestorsWrapper extends DelegateNodeWrapper {
        private ExtendingNodeAndAncestorsWrapper parent = null;

        public ExtendingNodeAndAncestorsWrapper(Node node) throws RepositoryException {
            if (node.getDepth() == 0) {
                this.parent = null;
                setWrappedNode(new ExtendingNodeWrapper(node));
            }
            else {
                this.parent = new ExtendingNodeAndAncestorsWrapper(node.getParent());
                setWrappedNode(this.parent.getNode(node.getName()));
            }
        }

        @Override
        public Node getParent() {
            return this.parent;
        }
    }

    /**
     * Convenience class so that we can return both the TypeDescriptor for a node and
     * the TypeDescriptor for the entry type if the node is an array, collection, or map
     * at the same time.
     */
    private static class NodeAndEntryTypeDescriptor {
        private TypeDescriptor typeDescriptor;
        private TypeDescriptor entryTypeDescriptor;

        public NodeAndEntryTypeDescriptor(TypeDescriptor typeDescriptor, TypeDescriptor entryTypeDescriptor) {
            this.typeDescriptor = typeDescriptor;
            this.entryTypeDescriptor = entryTypeDescriptor;
        }

        public TypeDescriptor getTypeDescriptor() {
            return typeDescriptor;
        }

        public TypeDescriptor getEntryTypeDescriptor() {
            return entryTypeDescriptor;
        }
    }
}
