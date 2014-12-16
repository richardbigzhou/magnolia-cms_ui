import groovy.transform.Field
import info.magnolia.config.BuildExtensions
import info.magnolia.ui.dialog.DialogAnnotation
import info.magnolia.ui.dialog.config.DialogBuilder
import info.magnolia.ui.form.config.FieldConfig
import info.magnolia.ui.form.config.categories.fields.FieldCategory
import info.magnolia.ui.framework.action.AddNodeAction
import info.magnolia.ui.framework.action.DeleteAction
import info.magnolia.ui.framework.config.UiConfig

@Field UiConfig uiConfig = new UiConfig()

@DialogAnnotation("testDialog")
def dottedDialog(DialogBuilder dialog, UiConfig cfg, FieldConfig field) {
    // Variables
    def cancelActionDescription = "description"

// Breaking down the parts of the builder
    def commitAction = cfg.actions.action("commit");

    dialog
     .actions(
       commitAction,
       cfg.actions.action("cancel").description(cancelActionDescription)
    ).form()
       .description('desc')
       .tab("tab")
         .i18nBasename('test')
         .label('label')
         .fields(
            field.text("test").defaultValue("text").label("l1").i18n(),
            field.select("select").options([1, 2, 3, 4, 5]))
}


@DialogAnnotation("testDialog")
def testDialog(DialogBuilder dialog, UiConfig cfg) {
    use(FieldCategory) {
        use(BuildExtensions) {

            dialog.with {

                cfg.actions.buildWith() {

                    actions(

                            action("commit").with {
                                implementation AddNodeAction.class
                                description "save and add"
                            },

                            action("cancel").with {
                                implementation DeleteAction.class
                                description "save and delete"
                            }
                    )
                }

                form().with {

                    description 'desc'

                    tab("tab2").with {
                        i18nBasename("test")
                    }

                    tab("tab").with {
                        text("text")
                        date("dateText")
                        i18nBasename 'test'
                        label('label')
                    }

                }
            }
        }
    }
}