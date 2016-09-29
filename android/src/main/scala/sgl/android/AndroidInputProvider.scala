package sgl
package android

import _root_.android.view.View
import _root_.android.view.GestureDetector
import _root_.android.view.MotionEvent
import _root_.android.view.KeyEvent

trait AndroidInputProvider extends InputProvider with Lifecycle {
  this: AndroidWindowProvider =>

  abstract override def startup(): Unit = {
    super.startup()

    val gestureDetector = new GestureDetector(mainActivity.gameView.getContext, new GameGestureListener)
    gestureDetector.setIsLongpressEnabled(false) //no long press, user can long press and then scroll

    mainActivity.gameView.setOnTouchListener(new View.OnTouchListener {
      override def onTouch(view: View, event: MotionEvent): Boolean = {
        gestureDetector.onTouchEvent(event)

        for(p <- 0 until event.getPointerCount()) {
          if(event.getAction == MotionEvent.ACTION_DOWN) {
            val x = event.getX(p).toInt
            val y = event.getY(p).toInt
            Input.newEvent(Input.TouchDownEvent(x, y, event.getPointerId(p)))
          } else if(event.getAction == MotionEvent.ACTION_MOVE) {

            //ACTION_MOVE is sometimes batched, meaning that we need to consume
            //historical data from the event, that shows intermediate position
            //before getting to the final getX/getY positions
            for(h <- 0 until event.getHistorySize) {
              val x = event.getHistoricalX(p, h).toInt
              val y = event.getHistoricalY(p, h).toInt
              Input.newEvent(Input.TouchMovedEvent(x, y, event.getPointerId(p)))
            }

            val x = event.getX(p).toInt
            val y = event.getY(p).toInt
            Input.newEvent(Input.TouchMovedEvent(x, y, event.getPointerId(p)))
          } else if(event.getAction == MotionEvent.ACTION_UP) {
            val x = event.getX(p).toInt
            val y = event.getY(p).toInt
            Input.newEvent(Input.TouchUpEvent(x, y, event.getPointerId(p)))
          }
        }

        /*
         * if true is not returned then we do not get follow up events 
         * (like UP after DOWN) and the gesture detector is not working properly (missing some events)
         */
        true
      }
    })

    //TODO: clarify what is proper way to detect those events
    mainActivity.gameView.setOnKeyListener(new View.OnKeyListener {
      override def onKey(v: View, keyCode: Int, event: KeyEvent): Boolean = {
        if(keyCode == KeyEvent.KEYCODE_BACK) {
          if(event.getAction == KeyEvent.ACTION_UP) {
            Input.newEvent(Input.KeyUpEvent(Input.Keys.ButtonBack))
            true
          } else if(event.getAction == KeyEvent.ACTION_DOWN) {
            Input.newEvent(Input.KeyDownEvent(Input.Keys.ButtonBack))
            true
          } else false
        } else if(keyCode == KeyEvent.KEYCODE_MENU) {
          if(event.getAction == KeyEvent.ACTION_UP) {
            Input.newEvent(Input.KeyUpEvent(Input.Keys.ButtonMenu))
            true
          } else if(event.getAction == KeyEvent.ACTION_DOWN) {
            Input.newEvent(Input.KeyDownEvent(Input.Keys.ButtonMenu))
            true
          } else false
        } else {
          false
        }
      }
    })

  }

  //TODO: must reintegrate these scrolling detection somewhere in the framework
  class GameGestureListener extends GestureDetector.SimpleOnGestureListener {
    override def onScroll(ev1: MotionEvent, ev2: MotionEvent, distanceX: Float, distanceY: Float): Boolean = {
      //inputBuffer.touchScrollVector match {
      //  case None =>
      //    //inputBuffer.touchScrollVector = Some((distanceX, distanceY))
      //  case Some((dx, dy)) =>
      //    //inputBuffer.touchScrollVector = Some((dx + distanceX, dy + distanceY))
      //}
      true
    }
    override def onSingleTapUp(event: MotionEvent): Boolean = {
      val x = event.getX.toInt
      val y = event.getY.toInt
      //inputBuffer.touchPoint =  Some((x, y))
      true
    }
  }

}
