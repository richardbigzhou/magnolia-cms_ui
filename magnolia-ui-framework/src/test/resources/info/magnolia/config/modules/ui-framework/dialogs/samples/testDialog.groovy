import groovy.transform.Field
import info.magnolia.ui.framework.config.samplecategories.fields.BuildExtensions
import info.magnolia.ui.dialog.config.DialogBuilder
import info.magnolia.ui.form.config.FieldConfig
import info.magnolia.ui.framework.config.samplecategories.fields.FieldCategory
import info.magnolia.ui.framework.action.AddNodeAction
import info.magnolia.ui.framework.action.DeleteAction
import info.magnolia.ui.framework.config.UiConfig
import info.magnolia.ui.framework.config.Dialog

@Field
UiConfig uiConfig = new UiConfig()

@Field
def cancelActionDescription = "description"

@Field
def commitAction = uiConfig.actions.action("commit")

@Dialog("testDialogWithADot")
def dottedDialog(DialogBuilder dialog, FieldConfig field) {
    // Variables
    dialog
     .actions(
       commitAction,
       uiConfig.actions.action("cancel").description(cancelActionDescription)
    ).form()
       .description('desc')
       .tab("tab")
         .i18nBasename('test')
         .label('label')
         .fields(
            field.text("test").defaultValue("text").label("l1").i18n(),
            field.select("select").options([1, 2, 3, 4, 5]))

    dialog.definition()
}


@Dialog("testDialog")
def testDialog(DialogBuilder dialog, UiConfig cfg) {
    // Using some Groovy categories
    use(FieldCategory) {
        use(BuildExtensions) {
            dialog.with {
                // Using 'buildWith' method instead of 'with'
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

                    description('desc')

                    tab("tab2").with {
                        i18nBasename("test")
                    }

                    tab("tab").with {
                        // 'text' and 'date' are coming from FieldCategory
                        text("text")
                        date("dateText")

                        i18nBasename 'test'
                        label('label')
                    }

                }
            }
        }
    }

    dialog.definition()
}