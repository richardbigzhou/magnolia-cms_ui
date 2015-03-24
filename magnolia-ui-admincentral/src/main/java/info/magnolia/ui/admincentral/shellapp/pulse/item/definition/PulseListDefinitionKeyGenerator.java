package info.magnolia.ui.admincentral.shellapp.pulse.item.definition;

import info.magnolia.i18nsystem.AbstractI18nKeyGenerator;

import java.lang.reflect.AnnotatedElement;
import java.util.List;

/**
 * Key generator for {@link PulseListDefinition}.
 */
public class PulseListDefinitionKeyGenerator extends AbstractI18nKeyGenerator<PulseListDefinition> {

    @Override
    protected void keysFor(List<String> keys, PulseListDefinition definition, AnnotatedElement el) {
        addKey(keys, createId(definition), fieldOrGetterName(el));
    }

    protected String createId(PulseListDefinition definition) {
        return PulseListDefinition.PULSE_CONFIG_NODE_NAME + "." + definition.getName();
    }
}
