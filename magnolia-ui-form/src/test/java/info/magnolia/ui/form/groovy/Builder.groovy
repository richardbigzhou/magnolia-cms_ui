package info.magnolia.ui.form.groovy

/**
 * Builder.
 * TODO: Add proper JavaDoc.
 */
class Builder extends BuildExtension {


    static {

    }

    def init() {

    }

    static def <T> T instance(T obj, Closure cls) {
        obj.with cls
        obj
    }

    def <T> T build(Class<? extends T> clazz, Closure cls) {

        build();

        Object.metaClass.build = {
            Closure closure -> instance(delegate, closure)
        }

        def instance = (clazz.newInstance())
        cls.setDelegate(instance)
        cls.setResolveStrategy(Closure.DELEGATE_FIRST)
        cls.delegate.metaClass.propertyMissing = {
            String name, String value -> missingProperty(name, value)
        }

        cls()
    }

}
