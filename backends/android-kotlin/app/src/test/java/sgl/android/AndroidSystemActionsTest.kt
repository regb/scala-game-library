package sgl.android

import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import scala.collection.immutable.`Set$`
import sgl.`Input$`
import sgl.InputActions
import sgl.InputProcessor

class AndroidSystemActionsTest {
    @Test
    fun backUsesScalaSingletonAndFollowsActiveScreen() {
        assertSame(InputActions.`Back$`.`MODULE$`, AndroidSystemActions.Back)
        val input = `Input$`.`MODULE$`
        var backReceived = false
        val mapProcessor = object : InputProcessor {
            override fun systemAction(action: InputActions.Action): Boolean {
                backReceived = action === InputActions.`Back$`.`MODULE$`
                return backReceived
            }
        }
        val backActions = `Set$`.`MODULE$`.empty<InputActions.Action>()
            .incl(InputActions.`Back$`.`MODULE$`)
        try {
            input.setInputProcessor(mapProcessor, backActions)
            assertTrue(input.handlesSystemAction(AndroidSystemActions.Back))
            assertTrue(input.inputProcessor().systemAction(AndroidSystemActions.Back))
            assertTrue(backReceived)

            // A menu processor does not declare Back, so Android must not intercept.
            input.setInputProcessor(object : InputProcessor {})
            assertFalse(input.handlesSystemAction(AndroidSystemActions.Back))
            assertFalse(input.inputProcessor().systemAction(AndroidSystemActions.Back))
        } finally {
            input.clearInputProcessor()
        }
    }
}
