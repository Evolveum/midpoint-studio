package com.evolveum.midpoint.studio.ui

import com.intellij.ui.layout.panel
import javax.swing.JPanel

/**
 * Created by Viliam Repan (lazyman).
 */
fun wizard(): JPanel {
    return panel {
        row {
            row {
                label("Doe")
                textField({ "sample" }, {})
                    .comment("test another comment")
            }
            row {
                checkBox(
                    "checkbox",
                    true,
                    comment = "comment some"
                )
            }
            row("Label", separated = true) {
                textField({ "abc" }, {}).comment("comment")
            }
            row("Label", separated = true) {
                textField({ "abc" }, {}).comment("comment")
            }
        }
    }
}
