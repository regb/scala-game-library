package sgl
package scene
package ui

trait PopupsComponent extends ButtonsComponent {
  this: GraphicsProvider with WindowProvider with SceneComponent =>

  import Graphics._

  /** A Popup is a SceneNode that cover the entire space.
    *
    * The popup covers the width/height area and intercept all click events. It
    * contains an inner node which is the actual content of the popup. The
    * coordinates of the inner node are relative to the popup coordinates and
    * not centered, which gives the option to the inner node to implement a
    * transition to show up on screen.
    *
    * The popup starts invisible and non-active (it is there, but should not be
    * noticed). You can enable it by calling show()  whenever we need the popup
    * to show.
    *
    * The popup provides the base mecanism for showing something on screen, it
    * provides a simple default render that will draw its background color and
    * then the inner content. For simple tuning one can override the
    * backgroundColor and for more advanced tuning one can override the render
    * method.
    */
  class Popup(_width: Int, _height: Int, inner: SceneNode) extends SceneNode(0, 0, _width, _height) {

    val backgroundColor = Color.Transparent

    private var active = false

    // A popup always intecept clicks, so either the inner node processes the
    // click or otherwise the popup intercept the click (and does nothing).
    override def hit(x: Int, y: Int): Option[SceneNode] = {
      if(active)
        inner.hit(x, y).orElse(Some(this))
      else
        None
    }

    override def update(dt: Long): Unit = {
      inner.update(dt)
    }

    override def render(canvas: Canvas): Unit = {
      if(active) {
        canvas.drawColor(backgroundColor)
        inner.render(canvas)
      }
    }

    def show(): Unit = {
      active = true
    }
    def hide(): Unit = {
      active = false
    }

  }

  /** A simple SceneNode to implement a Dialog box.
    *
    * A Dialog is a widget that displays a set of choice to the player and
    * run the code corresponding to the choice made by the player. It is most
    * commonly used in a popup style, where it blocks all other interaction and
    * waits until the user clicked one of the choice.
    */
  class Dialog(_width: Int, label: String, options: List[(String, () => Unit)], fontSize: Int, fontColor: Color) extends SceneNode(0, 0, _width, 0) {

    val leftMargin = Window.dp2px(32)
    val topMargin = Window.dp2px(32)
    val rightMargin = Window.dp2px(32)
    val bottomMargin = Window.dp2px(32)

    val labelOptionsSpace = Window.dp2px(64)

    val fillColor = Color.rgba(0,0,0,200)
    val outlineColor = Color.White
    val buttonRegularTheme = ButtonTheme(
      borderColor=Color.White,
      fillColor=Color.rgb(0,0,0),
      textColor=Color.White,
      textFont=Font.Default.withSize(fontSize))
    val buttonPressedTheme = buttonRegularTheme.copy(
      fillColor=Color.rgb(150,150,150),
      textColor=Color.Black)

    // The horizontal space between two option buttons.
    val buttonMargin = Window.dp2px(24)

    private var outlinePaint: Paint = _
    private var fillPaint: Paint = _

    private val paint = defaultPaint.withFont(Font.Default.withSize(fontSize)).withColor(fontColor)

    private var buttons: List[TextButton] = Nil

    override def hit(x: Int, y: Int): Option[SceneNode] = {
      buttons.flatMap(_.hit(x, y)).headOption
    }

    override def update(dt: Long): Unit = {
      buttons.foreach(_.update(dt))
    }

    override def render(canvas: Canvas): Unit = {
      val labelText = canvas.renderText(label, _width - leftMargin - rightMargin, paint)
      val totalHeight = topMargin + labelText.height + labelOptionsSpace + fontSize + bottomMargin
      this.height = totalHeight
      val buttonsY = totalHeight - bottomMargin - fontSize

      val buttonWidth = (width.toInt - leftMargin - rightMargin - (options.size-1)*buttonMargin)/options.size

      if(fillPaint == null)
        fillPaint = defaultPaint.withColor(fillColor)
      if(outlinePaint == null)
        outlinePaint = defaultPaint.withColor(outlineColor)
      if(buttons == Nil) {
        buttons = options.zipWithIndex.map{ case ((txt, action), i) => {
          new TextButton(x.toInt + leftMargin + buttonWidth*i + buttonMargin*i, y.toInt + buttonsY, buttonWidth, fontSize + Window.dp2px(16), txt, buttonRegularTheme, buttonPressedTheme) {
            override def notifyClick(x: Int, y: Int): Boolean = {
              action()
              true
            }
          }
        }}
      }

      canvas.drawRect(x.toInt, y.toInt, _width, totalHeight, fillPaint)
      canvas.drawLine(x.toInt, y.toInt, x.toInt+_width, y.toInt, outlinePaint)
      canvas.drawLine(x.toInt+_width, y.toInt, x.toInt+_width, y.toInt+totalHeight, outlinePaint)
      canvas.drawLine(x.toInt+_width, y.toInt+totalHeight, x.toInt, y.toInt+totalHeight, outlinePaint)
      canvas.drawLine(x.toInt, y.toInt+totalHeight, x.toInt, y.toInt, outlinePaint)

      canvas.drawText(labelText, x.toInt + leftMargin, y.toInt + topMargin + fontSize)

      buttons.foreach(button => {
        button.y = y.toInt + buttonsY
        button.render(canvas)
      })
    }

  }

  /** A DialogPopup combines a Dialog and a Popup.
    *
    * This makes the Dialog into a full screen blocking interaction. This is
    * the most common use case for a Dialog object. By default, this will
    * center the dialog in the popup with the update method. To achieve a
    * transition effect, one can override the update method.
    */
  class DialogPopup(_width: Int, _height: Int, dialog: Dialog) extends Popup(_width, _height, dialog) {

    override def update(dt: Long): Unit = {
      super.update(dt)
      dialog.x = (width - dialog.width).toInt/2
      dialog.y = (height - dialog.height).toInt/2
    }

  }

}
