import info.magnolia.ui.form.config.FieldConfig
import info.magnolia.ui.form.config.FormBuilder
import info.magnolia.ui.form.config.ValidatorConfig

Object.metaClass.with = { Closure closure -> instance(delegate, closure) }

Object.metaClass.expand = {  }

static def <T> T instance(T obj, Closure cls) {
    Closure clsClone = cls.clone()
    clsClone.resolveStrategy = Closure.DELEGATE_FIRST
    clsClone.delegate = obj
    clsClone(obj)
    obj
}

def f2 = new FieldConfig().select("test").with {

    label 'label'

}

new FormBuilder().with {

    description 'desc'

    tabs tab("tab").with {

        i18nBasename 'test'

        label('label')

        fields(
                new FieldConfig().text("test").expand {

                    i18n("test")

                    label 'label'

                    i18nBasename("test")

                    validator new ValidatorConfig().email().with {

                        errorMessage 'test'


                    }
                },

                f2
        )
    }
}