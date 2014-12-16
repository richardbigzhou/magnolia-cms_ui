// Variables
def cancelActionDescription = "description"

// Breaking down the parts of the builder
def commitAction = cfg.actions.action("commit");

dialog
  .actions(
        commitAction,
        cfg.actions.action("cancel").description(cancelActionDescription)
        )
  .form()
    .description('desc')
    .tab("tab")
       .i18nBasename('test')
       .label('label')
       .fields(
          field.text("test").defaultValue("text").label("l1").i18n(),
          field.select("select").options([1, 2, 3, 4, 5]))

