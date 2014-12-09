package info.magnolia.ui.form.groovy

/**
 * BuildExtension.
 * TODO: Add proper JavaDoc.
 */
class BuildExtension {

    def build() {
        Object.metaClass.build = {
            Closure closure -> instance(delegate, closure)
        }
    }

}
