package info.magnolia.ui.form.groovy
import info.magnolia.ui.form.config.FieldConfig
import info.magnolia.ui.form.config.FormBuilder
import info.magnolia.ui.form.config.ValidatorConfig
/**
 * TestScript.
 * TODO: Add proper JavaDoc.
 */



//Object.metaClass.propertyMissing = {
//    String name, String value -> missingProperty(name, value)
//}

def missingProperty(String s, String v) {
    println s
}


//Integer.metaClass.getDaysFromNow = { ->
//    Calendar today = Calendar.instance
//    today.add(Calendar.DAY_OF_MONTH, delegate)
//    today.time
//}

//println(5.daysFromNow)


def <T> T create(Class<? extends T> clazz) {
    clazz.newInstance()
}

def <T> T build(Class<? extends T> clazz, Closure cls) {
    def instance = (clazz.newInstance())
    cls.setDelegate(instance)
    cls.setResolveStrategy(Closure.DELEGATE_FIRST)
    cls.delegate.metaClass.propertyMissing = {
        String name, String value -> missingProperty(name, value)
    }

    cls()
}

create FormBuilder

new Builder().init()

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


def Integer two = 2;

def builder = new FormBuilder().with {

    description 'desc'


    tab("tab").with {

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

//def form = create(ConfiguredFormDefinition).build {
//
//    label = 'test'
//
//    tabs = [
//            create(ConfiguredTabDefinition).build {
//
//                fields = [
//
//                ]
//            }
//    ]
//
//}

//assert form != null
//
//private def t() {
//
//}
//
//def test = this.<ConfiguredFormDefinition>build(ConfiguredFormDefinition.class) {
//
//}

