package sgl
package android

import _root_.android.view.View
import _root_.android.view.GestureDetector
import _root_.android.view.MotionEvent
import _root_.android.view.KeyEvent

trait AndroidInputProvider extends InputProvider with Lifecycle {
  this: AndroidWindowProvider =>

  private var touchingPos: Option[(Int, Int)] = None
  private var scrollVector: Option[(Float, Float)] = None

  abstract override def startup(): Unit = {
    super.startup()
    println("init of AndroidInputProvider")

    val gestureDetector = new GestureDetector(mainActivity.gameView.getContext, new GameGestureListener)
    gestureDetector.setIsLongpressEnabled(false) //no long press, user can long press and then scroll

    mainActivity.gameView.setOnTouchListener(new View.OnTouchListener {
      override def onTouch(view: View, event: MotionEvent): Boolean = {
        gestureDetector.onTouchEvent(event)

        if(event.getAction == MotionEvent.ACTION_DOWN) {
          val x = event.getX.toInt
          val y = event.getY.toInt
          inputBuffer.touchingDown = Some((x, y))
          inputBuffer.touchDown = Some((x, y))
        } else if(event.getAction == MotionEvent.ACTION_MOVE) {
          val x = event.getX.toInt
          val y = event.getY.toInt
          inputBuffer.touchingDown = Some((x, y))
        } else if(event.getAction == MotionEvent.ACTION_UP) {
          val x = event.getX.toInt
          val y = event.getY.toInt
          inputBuffer.touchingDown = None
          inputBuffer.touchUp = Some((x, y))
        }
        /*
         * if true is not returned then we do not get follow up events 
         * (like UP after DOWN) and the gesture detector is not working properly (missing some events)
         */
        true
      }
    })

    var backPressed = false
    var menuPressed = false
    //TODO: clarify what is proper way to detect those events
    mainActivity.gameView.setOnKeyListener(new View.OnKeyListener {
      override def onKey(v: View, keyCode: Int, event: KeyEvent): Boolean = {
        if(keyCode == KeyEvent.KEYCODE_BACK) {
          if(event.getAction == KeyEvent.ACTION_UP && backPressed) {
            backPressed = false
            inputBuffer.backPressed = true
            true
          } else if(event.getAction == KeyEvent.ACTION_DOWN) {
            backPressed = true
            true
          } else false
        } else if(keyCode == KeyEvent.KEYCODE_MENU) {
          if(event.getAction == KeyEvent.ACTION_UP && backPressed) {
            menuPressed = false
            inputBuffer.menuPressed = true
            true
          } else if(event.getAction == KeyEvent.ACTION_DOWN) {
            menuPressed = true
            true
          } else false
        } else {
          false
        }
      }
    })

  }

  class GameGestureListener extends GestureDetector.SimpleOnGestureListener {
    override def onScroll(ev1: MotionEvent, ev2: MotionEvent, distanceX: Float, distanceY: Float): Boolean = {
      inputBuffer.touchScrollVector match {
        case None =>
          inputBuffer.touchScrollVector = Some((distanceX, distanceY))
        case Some((dx, dy)) =>
          inputBuffer.touchScrollVector = Some((dx + distanceX, dy + distanceY))
      }
      true
    }
    override def onSingleTapUp(event: MotionEvent): Boolean = {
      val x = event.getX.toInt
      val y = event.getY.toInt
      inputBuffer.touchPoint =  Some((x, y))
      true
    }
  }

}
