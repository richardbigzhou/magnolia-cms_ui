package info.magnolia.ui.dialog.registry;

import info.magnolia.registry.RegistrationException;
import info.magnolia.registry.RegistryMap;
import info.magnolia.ui.dialog.definition.BaseDialogDefinition;

import javax.inject.Singleton;
import java.util.List;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: sasha
 * Date: 8/30/13
 * Time: 12:42 AM
 * To change this template use File | Settings | File Templates.
 */
@Singleton
public class BaseDialogDefinitionRegistry<T extends BaseDialogDefinition> {

    private final RegistryMap<String, BaseDialogDefinitionProvider<T>> registry = new RegistryMap<String, BaseDialogDefinitionProvider<T>>() {

        @Override
        protected String keyFromValue(BaseDialogDefinitionProvider<T> value) {
            return value.getId();
        }
    };

    public T get(String id) throws RegistrationException {
        BaseDialogDefinitionProvider<T> provider;
        try {
            provider = registry.getRequired(id);
        } catch (RegistrationException e) {
            throw new RegistrationException("No dialog definition registered for id: " + id, e);
        }
        return provider.getDialogDefinition();
    }

    public void register(BaseDialogDefinitionProvider<T> provider) {
        registry.put(provider);
    }

    public Set<String> unregisterAndRegister(Set<String> registeredIds, List<BaseDialogDefinitionProvider<T>> providers) {
        return registry.removeAndPutAll(registeredIds, providers);
    }
}
