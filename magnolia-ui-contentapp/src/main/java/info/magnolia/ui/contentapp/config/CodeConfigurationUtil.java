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
package info.magnolia.ui.contentapp.config;

import info.magnolia.registry.RegistrationException;
import info.magnolia.ui.actionbar.config.ActionbarConfig;
import info.magnolia.ui.dialog.config.Dialog;
import info.magnolia.ui.dialog.config.DialogBuilder;
import info.magnolia.ui.dialog.config.DialogConfig;
import info.magnolia.ui.dialog.definition.DialogDefinition;
import info.magnolia.ui.dialog.registry.DialogDefinitionProvider;
import info.magnolia.ui.dialog.registry.DialogDefinitionRegistry;
import info.magnolia.ui.form.config.FormConfig;
import info.magnolia.ui.framework.app.AppDescriptor;
import info.magnolia.ui.framework.app.config.App;
import info.magnolia.ui.framework.app.config.AppBuilder;
import info.magnolia.ui.framework.app.registry.AppDescriptorProvider;
import info.magnolia.ui.framework.app.registry.AppDescriptorRegistry;
import info.magnolia.ui.framework.config.UiConfig;
import info.magnolia.ui.workbench.config.WorkbenchConfig;

import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for detecting annotated methods and registering providers with respective registries.
 */
public class CodeConfigurationUtil {

    private static final Logger log = LoggerFactory.getLogger(CodeConfigurationUtil.class);

    public static void registerAnnotatedDialogProviders(DialogDefinitionRegistry dialogDefinitionRegistry, Object instance) {
        for (Method method : findAnnotatedMethods(instance.getClass(), Dialog.class)) {
            Dialog dialog = method.getAnnotation(Dialog.class);
            dialogDefinitionRegistry.register(new JustInTimeDialogProvider(dialog.value(), instance, method));
        }
    }

    public static void registerAnnotatedAppProviders(AppDescriptorRegistry appDescriptorRegistry, Object instance) {
        for (Method method : findAnnotatedMethods(instance.getClass(), App.class)) {
            App app = method.getAnnotation(App.class);

            // TODO allow either AppBuilder OR ContentAppBuilder but never both

            try {
                appDescriptorRegistry.register(new JustInTimeAppProvider(app.value(), instance, method));
            } catch (RegistrationException e) {
                log.error("Failed to register app [{}] provided by method [{}]", app.value(), method);
            }
        }
    }

    private static class JustInTimeDialogProvider implements DialogDefinitionProvider {

        private final String id;
        private final Object instance;
        private final Method method;

        private JustInTimeDialogProvider(String id, Object instance, Method method) {
            this.id = id;
            this.method = method;
            this.instance = instance;
        }

        @Override
        public String getId() {
            return id;
        }

        @Override
        public DialogDefinition getDialogDefinition() throws RegistrationException {

            DialogBuilder dialogBuilder = new DialogBuilder(id);

            try {

                Object[] parameters = new Object[method.getParameterTypes().length];
                for (int parameterIndex = 0; parameterIndex < parameters.length; parameterIndex++) {
                    Class<?> parameterType = method.getParameterTypes()[parameterIndex];

                    if (parameterType.equals(DialogBuilder.class)) {
                        parameters[parameterIndex] = dialogBuilder;
                    } else if (parameterType.equals(UiConfig.class)) {
                        parameters[parameterIndex] = new UiConfig();
                    } else if (parameterType.equals(DialogConfig.class)) {
                        parameters[parameterIndex] = new DialogConfig();
                    } else if (parameterType.equals(FormConfig.class)) {
                        parameters[parameterIndex] = new FormConfig();
                    } else {
                        throw new RegistrationException("Unable to resolve parameter " + parameterIndex + " for method " + method);
                    }
                }

                Object returnValue = invokeMethod(method, instance, parameters);
                if (returnValue instanceof DialogDefinition) {
                    return (DialogDefinition) returnValue;
                }
                return dialogBuilder.exec();

            } catch (IllegalAccessException e) {
                throw new RegistrationException("Unable to create dialog [" + id + "]", e);
            } catch (InvocationTargetException e) {
                throw new RegistrationException("Unable to create dialog [" + id + "]", e);
            }
        }
    }

    private static class JustInTimeAppProvider implements AppDescriptorProvider {

        private final String name;
        private final Object instance;
        private final Method method;

        public JustInTimeAppProvider(String name, Object instance, Method method) {
            this.name = name;
            this.instance = instance;
            this.method = method;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public AppDescriptor getAppDescriptor() throws RegistrationException {

            AppBuilder appBuilder = null;
            ContentAppBuilder contentAppBuilder = null;

            try {
                Object[] parameters = new Object[method.getParameterTypes().length];
                for (int parameterIndex = 0; parameterIndex < parameters.length; parameterIndex++) {
                    Class<?> parameterType = method.getParameterTypes()[parameterIndex];

                    if (parameterType.equals(ContentAppBuilder.class)) {
                        parameters[parameterIndex] = contentAppBuilder = new ContentAppBuilder(name);
                    } else if (parameterType.equals(AppBuilder.class)) {
                        parameters[parameterIndex] = appBuilder = new AppBuilder(name);
                    } else if (parameterType.equals(UiConfig.class)) {
                        parameters[parameterIndex] = new UiConfig();
                    } else if (parameterType.equals(DialogConfig.class)) {
                        parameters[parameterIndex] = new DialogConfig();
                    } else if (parameterType.equals(WorkbenchConfig.class)) {
                        parameters[parameterIndex] = new WorkbenchConfig();
                    } else if (parameterType.equals(ActionbarConfig.class)) {
                        parameters[parameterIndex] = new ActionbarConfig();
                    } else if (parameterType.equals(FormConfig.class)) {
                        parameters[parameterIndex] = new FormConfig();
                    }
                    else if (parameterType.equals(ContentAppConfig.class)) {
                        parameters[parameterIndex] = new ContentAppConfig();
                    }
                    else {
                        throw new RegistrationException("Unable to resolve parameter " + parameterIndex + " for method " + method);
                    }
                }

                Object returnValue = invokeMethod(method, instance, parameters);
                if (returnValue instanceof AppDescriptor) {
                    return (AppDescriptor) returnValue;
                }
                return contentAppBuilder != null ? contentAppBuilder.exec() : appBuilder != null ? appBuilder.exec() : null;

            } catch (IllegalAccessException e) {
                throw new RegistrationException("Unable to create app [" + name + "]", e);
            } catch (InvocationTargetException e) {
                throw new RegistrationException("Unable to create app [" + name + "]", e);
            }
        }
    }

    private static List<Method> findAnnotatedMethods(Class<?> clazz, Class<? extends Annotation> annotation) {
        LinkedList<Method> methods = new LinkedList<Method>();
        Class<?> targetClass = clazz;
        do {
            Method[] declaredMethods = targetClass.getDeclaredMethods();
            for (Method declaredMethod : declaredMethods) {
                if (declaredMethod.isAnnotationPresent(annotation) && !Modifier.isStatic(declaredMethod.getModifiers()) && !isOverridden(declaredMethod, clazz)) {
                    methods.addFirst(declaredMethod);
                }
            }
            targetClass = targetClass.getSuperclass();
        } while (targetClass != null && targetClass != Object.class);

        return methods;
    }

    private static boolean isOverridden(Method method, Class<?> clazz) {
        if (method.getDeclaringClass().equals(clazz)) {
            return false;
        }
        int modifiers = method.getModifiers();
        if (Modifier.isPrivate(modifiers)) {
            return false;
        }
        Class<?> targetClass = clazz;
        do {
            for (Method declaredMethod : targetClass.getDeclaredMethods()) {
                if (overrides(declaredMethod, method)) {
                    return true;
                }
            }
            targetClass = clazz.getSuperclass();
        } while (targetClass != null && !targetClass.equals(method.getDeclaringClass()));
        return false;
    }

    /**
     * Returns true if a method overrides a method in a super class.
     */
    private static boolean overrides(Method method, Method methodInSuperclass) {
        if (!hasSameSignatures(method, methodInSuperclass)) {
            return false;
        }

        // See JLS section 8.4.8.1
        int modifiers = methodInSuperclass.getModifiers();
        if (Modifier.isPublic(modifiers) || Modifier.isProtected(modifiers)) {
            return true;
        }
        if (Modifier.isPrivate(modifiers)) {
            return false;
        }
        // methodInSuperclass must be package-private
        return method.getDeclaringClass().getPackage().equals(methodInSuperclass.getDeclaringClass().getPackage());
    }

    private static boolean hasSameSignatures(Method lhs, Method rhs) {
        return lhs.getName().equals(rhs.getName()) && Arrays.equals(lhs.getParameterTypes(), rhs.getParameterTypes());
    }

    private static void makeAccessible(AccessibleObject accessibleObject) {
        if (!accessibleObject.isAccessible()) {
            accessibleObject.setAccessible(true);
        }
    }

    private static Object invokeMethod(Method method, Object instance, Object... parameters) throws IllegalAccessException, InvocationTargetException {
        makeAccessible(method);
        try {
            return method.invoke(instance, parameters);
        } catch (InvocationTargetException e) {
            return rethrowInvocationTargetException(e);
        }
    }

    // TODO the return here is total bogus
    private static <T> T rethrowInvocationTargetException(InvocationTargetException e) throws InvocationTargetException {
        Throwable targetException = e.getTargetException();
        if (targetException instanceof Error) {
            throw (Error) targetException;
        }
        if (targetException instanceof RuntimeException) {
            throw (RuntimeException) targetException;
        }
        throw e;
    }
}
