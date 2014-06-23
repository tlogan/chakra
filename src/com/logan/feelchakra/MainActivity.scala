package com.logan.feelchakra

import android.util.Log
import android.widget.Toast

import RichView.view2RichView
import RichListView.listView2RichListView
import RichMenuItem.menuItem2RichMenuItem
import RichContext.context2RichContext

object MainActivity {
   
   val selectionFrameId = 23;
   val playerFrameId = 56;

}

class MainActivity extends Activity {

  private val that = this

  lazy val originalBottom = contentView.getBottom() 

  private val albumSelectionFragment =  Fragment.createAlbumSelection()
  private val artistSelectionFragment =  Fragment.createArtistSelection()
  private val trackSelectionFragment =  Fragment.createTrackSelection()
  private val stationSelectionFragment = Fragment.createStationSelection()

  private lazy val dim = dimension(this)

  private lazy val slideLayout = {
    val sl = new LinearLayout(this) with HorizontalSlideView {
      override val velMs: Int = 1
      override lazy val left: Int = -2 * dim.x 
      override val right: Int = 0 
      override def onSlideRightEnd(): Unit = this.setX(-dim.x)
      override def onSlideLeftEnd(): Unit = this.setX(-dim.x)
    }
    sl.setOrientation(HORIZONTAL)
    sl.setBackgroundColor(BLACK)
    sl.setPadding(0, this.dp(smallDp), 0, this.dp(smallDp))
    sl.setLayoutParams(new LLLayoutParams(3 * dim.x, WRAP_CONTENT))

    sl
  }

  def slide(): Unit = {
    if (slideLayout.getX() > -dim.x / 2 ) {
      Slider.slideRight(slideLayout)
    } else if (slideLayout.getX() < 3 * -dim.x / 2) {
      Slider.slideLeft(slideLayout)
    } else if (slideLayout.getX() > -dim.x) {
      Slider.slideLeft(slideLayout, -dim.x)
    } else if (slideLayout.getX() < -dim.x) {
      Slider.slideRight(slideLayout, -dim.x)
    }
  }

  private lazy val playerFragment = Fragment.createPlayer(slideLayout)

  private var menu: Menu = _ 

  var _playerOpen: Boolean = false

  lazy val bottomHeight = this.dp(medDp) + 2 * this.dp(smallDp)

  lazy val selectionFrame = new FrameLayout(that) {
    setId(MainActivity.selectionFrameId)
  } 

  lazy val playerFrame = new FrameLayout(that) with VerticalSlideView {
    
    override val velMs = 2
    override val upY = 0
    override lazy val downY = contentView.getBottom() - bottomHeight  
    override def onSlideUpEnd() = mainActorRef ! MainActor.SetPlayerOpen(true)
    override def onSlideDownEnd() = mainActorRef ! MainActor.SetPlayerOpen(false)

    setVisibility(GONE)
    setId(MainActivity.playerFrameId)
    setLayoutParams {
      new RLLayoutParams(MATCH_PARENT, MATCH_PARENT)
    }
  }

  lazy val contentView: RelativeLayout = new RelativeLayout(this) {

    setLayoutParams {
      new VGLayoutParams(MATCH_PARENT, MATCH_PARENT)
    }
    
    sealed trait MainMotion
    case object YMotion extends MainMotion
    case object XMotion extends MainMotion
    case object NoMotion extends MainMotion

    var motion: MainMotion = NoMotion

    val gestureDetector = new GestureDetector(that, new SimpleOnGestureListener {

      override def onDown(e: MotionEvent): Boolean = {

        val touchStartY = e.getY().toInt
        if (!_playerOpen && touchStartY > playerFrame.downY) {
          true
        } else if (_playerOpen && touchStartY < 100) {
          true
        } else {
          false
        }

      }

      override def onScroll(e1: MotionEvent, e2: MotionEvent, scrollX: Float, scrollY: Float): Boolean = {
        val totalDispY = e2.getY().toInt - e1.getY().toInt 
        val totalDispX = e2.getX().toInt - e1.getX().toInt 

        if (motion == YMotion || (motion == NoMotion && Math.abs(totalDispY) > Math.abs(totalDispX))) {
          motion = YMotion
          val offset = if (_playerOpen) totalDispY else totalDispY + playerFrame.downY 
          if (totalDispY < 0) {
            VerticalSlideView.moveUp(playerFrame, offset)
          } else {
            selectionFrame.setVisibility(VISIBLE)
            VerticalSlideView.moveDown(playerFrame, offset)
          }
        } else if (motion == XMotion || Math.abs(totalDispY) < Math.abs(totalDispX)) {
          motion = XMotion
          val x = slideLayout.getX()
          slideLayout.setX(x - scrollX)
        }

        true
      }

      override def onFling(e1: MotionEvent, e2: MotionEvent, velX: Float, velY: Float): Boolean = {

        motion match {
          case YMotion =>
            if (velY < 0) {
              VerticalSlideView.slideUp(playerFrame)
            } else {
              VerticalSlideView.slideDown(playerFrame)
            }
          case XMotion =>
            if (velX > 0) {
              Slider.slideRight(slideLayout)
            } else {
              Slider.slideLeft(slideLayout)
            }
          case NoMotion =>
        }
        true
      }
    })

    this.setOnTouch((view, event) => {
      if (!gestureDetector.onTouchEvent(event)) {
        event.getAction() match {
          case ACTION_UP => 
            motion match {
              case YMotion =>
                VerticalSlideView.slide(playerFrame)
              case XMotion =>
                slide()
              case NoMotion =>
            }
            motion = NoMotion
            true
          case ACTION_CANCEL => 
            motion match {
              case YMotion =>
                VerticalSlideView.slide(playerFrame)
              case XMotion =>
                slide()
              case NoMotion =>
            }
            motion = NoMotion
            true
          case _ =>
            motion = NoMotion
            false
        }
      } else true 

    })

    this.addOnLayoutChange((view, left, top, right, bottom, ol, ot, or, ob, remove) => {
      selectionFrame.setBottom(bottom - bottomHeight)
      playerFrame.setVisibility(VISIBLE)
    })

  }

  override def onCreate(savedInstanceState: Bundle): Unit = {

    super.onCreate(savedInstanceState)
    Toast.makeText(that, "ON CREATE", Toast.LENGTH_SHORT).show()
    getActionBar().hide()
    getActionBar().setDisplayShowHomeEnabled(false)
    getActionBar().setDisplayShowTitleEnabled(false)
    setContentView(contentView)

    contentView.addView(selectionFrame)
    contentView.addView(playerFrame)
    contentView.bringChildToFront(playerFrame)

    val playerFragTrans = getFragmentManager().beginTransaction()
    playerFragTrans.replace(playerFrame.getId(), playerFragment).commit()

    val playerServiceIntent = new Intent(this, classOf[PlayerService])
    startService(playerServiceIntent);


  }

  override def onCreateOptionsMenu(menu: Menu): Boolean  = {
    Toast.makeText(that, "ON CREATE MENU", Toast.LENGTH_SHORT).show()
    this.menu = menu
    mainActorRef ! MainActor.SetDatabase(new Database(this))
    mainActorRef ! MainActor.SetCacheDir(getCacheDir())
    mainActorRef ! MainActor.Subscribe(this.toString, handler)
    getActionBar().show()
    super.onCreateOptionsMenu(menu)
  }

  override def onDestroy(): Unit = {
    super.onDestroy()
    val playerServiceIntent = new Intent(this, classOf[PlayerService])
    stopService(playerServiceIntent);
    mainActorRef ! MainActor.Unsubscribe(this.toString)
  }

  private val handler = new Handler(new HandlerCallback() {
    override def handleMessage(msg: Message): Boolean = {
      import UI._
      msg.obj match {
        case OnSelectionListChanged(selectionList) => 
          that.setSelectionList(selectionList)
          true
        case OnPlayerOpenChanged(playerOpen) => 
          _playerOpen = playerOpen
          that.setPlayerVisibility(playerOpen)
          true
        case OnSelectionChanged(selection) => 
          replaceSelectionFragment(selection)
          true
        case _ => false
      }
    }
  })

  private def setSelectionList(selectionList: List[Selection]): Unit = {

    selectionList.zipWithIndex foreach { pair => 
      val selection = pair._1
      val index = pair._2
      val item = menu.add(0, index, 0, selection.label)
      item.setShowAsAction(SHOW_AS_ACTION_ALWAYS)
      item.setOnClick(it => {
        mainActorRef ! MainActor.SetSelection(selection)
        true
      })
    }

  }


  private def setPlayerVisibility(playerOpen: Boolean): Unit = {
    if (playerOpen) {
      Log.d("chakra", "Player Opened!!")
      VerticalSlideView.moveUp(playerFrame)
      selectionFrame.setVisibility(GONE)
    } else {
      selectionFrame.setVisibility(VISIBLE)
      VerticalSlideView.moveDown(playerFrame)
    }
  }

  private def replaceSelectionFragment(selection: Selection): Unit = {

    val transaction = getFragmentManager().beginTransaction()

    selection match {
      case ArtistSelection => 
        transaction.replace(selectionFrame.getId(), artistSelectionFragment)
      case AlbumSelection => 
        transaction.replace(selectionFrame.getId(), albumSelectionFragment)
      case TrackSelection => 
        transaction.replace(selectionFrame.getId(), trackSelectionFragment)
      case StationSelection =>
        transaction.replace(selectionFrame.getId(), stationSelectionFragment)
    }

    transaction.commit()
    
  }

}
