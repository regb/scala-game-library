package sgl.android

import android.content.Context
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import sgl.`Input$`

class AndroidInputListener(context: Context): View.OnTouchListener {

    val gestureDetector = GestureDetector(context, GameGestureListener())
    init {
        //no long press, user can long press and then scroll
        gestureDetector.setIsLongpressEnabled(false)
    }

    override fun onTouch(view: View?, event: MotionEvent?): Boolean {
        if(view == null || event == null)
            return false

        gestureDetector.onTouchEvent(event)

        /*
         * Multi-touch on android starts with a first ACTION_DOWN event for the
         * very first pointer, then following down are ACTION_POINTER_DOWN, and
         * first up (as long as one finger is still left) is ACTION_POINTER_UP.
         * Seems like we don't need that level of precision in the engine, so we
         * only have the notion of Up and Down, relying on the pointer id if we
         * need to track a multitouch gesture
         */

        val action = event.getActionMasked()
        for(p in 0 until event.getPointerCount()) {
            if(action == MotionEvent.ACTION_DOWN) {
                val x = event.getX(p).toInt()
                val y = event.getY(p).toInt()
                `Input$`.`MODULE$`.inputProcessor().touchDown(x, y, event.getPointerId(p))
            } else if(action == MotionEvent.ACTION_POINTER_DOWN) {
                val x = event.getX(p).toInt()
                val y = event.getY(p).toInt()
                `Input$`.`MODULE$`.inputProcessor().touchDown(x, y, event.getPointerId(p))
            } else if(action == MotionEvent.ACTION_MOVE) {
                //ACTION_MOVE is sometimes batched, meaning that we need to consume
                //historical data from the event, that shows intermediate position
                //before getting to the final getX/getY positions
                for(h in 0 until event.getHistorySize()) {
                    val x = event.getHistoricalX(p, h).toInt()
                    val y = event.getHistoricalY(p, h).toInt()
                    `Input$`.`MODULE$`.inputProcessor().touchMoved(x, y, event.getPointerId(p))
                }

                val x = event.getX(p).toInt()
                val y = event.getY(p).toInt()
                `Input$`.`MODULE$`.inputProcessor().touchMoved(x, y, event.getPointerId(p))
            } else if(action == MotionEvent.ACTION_POINTER_UP) {
                val x = event.getX(p).toInt()
                val y = event.getY(p).toInt()
                `Input$`.`MODULE$`.inputProcessor().touchUp(x, y, event.getPointerId(p))
            } else if(action == MotionEvent.ACTION_UP) {
                val x = event.getX(p).toInt()
                val y = event.getY(p).toInt()
                `Input$`.`MODULE$`.inputProcessor().touchUp(x, y, event.getPointerId(p))
            }
        }

        /*
         * if true is not returned then we do not get follow up events
         * (like UP after DOWN) and the gesture detector is not working properly (missing some events)
         */
        return true
    }

}

//TODO: must reintegrate these scrolling detection somewhere in the framework
class GameGestureListener: GestureDetector.SimpleOnGestureListener() {
//    override fun onScroll(ev1: MotionEvent, ev2: MotionEvent, distanceX: Float, distanceY: Float): Boolean {
//        //inputBuffer.touchScrollVector match {
//        //  case None =>
//        //    //inputBuffer.touchScrollVector = Some((distanceX, distanceY))
//        //  case Some((dx, dy)) =>
//        //    //inputBuffer.touchScrollVector = Some((dx + distanceX, dy + distanceY))
//        //}
//        return true
//    }
    override fun onSingleTapUp(event: MotionEvent): Boolean {
        val x = event.getX().toInt()
        val y = event.getY().toInt()
        //inputBuffer.touchPoint =  Some((x, y))
        return true
    }
}