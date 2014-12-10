dialog.with {

    cfg.actions.with {
        actions(
            action("commit").with {
                implementation null
            },
        )
    }

    form().with {

        description 'desc'

        tab("tab2").with {
            i18nBasename("test")
        }

        tab("tab").with {

            i18nBasename 'test'

            label('label')

            cfg.fields.with {
                fields text("test"), select("select")
            }
        }
    }
}




