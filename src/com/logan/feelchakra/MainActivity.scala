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

  private val albumSelectionFragment =  AlbumSelectionFragment.create()
  private val artistSelectionFragment =  ArtistSelectionFragment.create()
  private val trackSelectionFragment =  TrackSelectionFragment.create()
  private val stationSelectionFragment = StationSelectionFragment.create()

  private lazy val dim = dimension(this)

  private lazy val slideLayout = {
    val sl = new LinearLayout(this) with HorizontalSlideView {
      override val velMs: Int = 1
      override lazy val left: Int = -2 * dim.x 
      override val right: Int = 0 
      override def onSlideRightEnd(): Unit = {
        mainActorRef ! MainActor.SetPresentTrackToPrev
        _xGestureOn = true
      }
      override def onSlideLeftEnd(): Unit = {
        mainActorRef ! MainActor.SetPresentTrackToNext
        _xGestureOn = true
      }
    }
    sl.setOrientation(HORIZONTAL)
    sl.setBackgroundColor(BLACK)
    sl.setPadding(0, this.dp(smallDp), 0, this.dp(smallDp))
    sl.setLayoutParams(new LLLayoutParams(3 * dim.x, WRAP_CONTENT))

    sl
  }

  def slide(): Unit = {
    if (slideLayout.getX() > -dim.x / 2 ) {
      _xGestureOn = false
      HorizontalSlideView.slideRight(slideLayout)
    } else if (slideLayout.getX() < 3 * -dim.x / 2) {
      _xGestureOn = false
      HorizontalSlideView.slideLeft(slideLayout)
    } else if (slideLayout.getX() > -dim.x) {
      HorizontalSlideView.slideLeft(slideLayout, -dim.x)
    } else if (slideLayout.getX() < -dim.x) {
      HorizontalSlideView.slideRight(slideLayout, -dim.x)
    }
  }

  private lazy val playerFragment = PlayerFragment.create(slideLayout)

  private var menu: Menu = _ 

  var _playerOpen: Boolean = false

  lazy val bottomHeight = this.dp(medDp) + 2 * this.dp(smallDp)

  lazy val selectionFrame = new FrameLayout(that) {
    setId(MainActivity.selectionFrameId)
  } 

  private var _xGestureOn = true

  lazy val playerFrame = new FrameLayout(that) with VerticalSlideView {
    
    override val velMs = 2
    override val upY = 0
    override lazy val downY = contentView.getBottom() - bottomHeight  
    override def onSlideUpEnd() = {
      mainActorRef ! MainActor.SetPlayerOpen(true)
    }
    override def onSlideDownEnd() = {
      mainActorRef ! MainActor.SetPlayerOpen(false)
    }

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
          if (_xGestureOn && (_prev || _next)) {
            val x = slideLayout.getX()
            if (!_prev) {
              slideLayout.setX(Math.min(-dim.x, x - scrollX))
            } else if (!_next) {
              slideLayout.setX(Math.max(-dim.x, x - scrollX))
            } else {
              slideLayout.setX(x - scrollX)
            }
          }
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
            if (_xGestureOn) {
              if (_prev && velX > that.dp(96)) {
                _xGestureOn = false
                HorizontalSlideView.slideRight(slideLayout)
              } else if (_prev && velX > 0) {
                HorizontalSlideView.slideLeft(slideLayout, -dim.x)
              } else if (_next && velX < that.dp(-96)) {
                _xGestureOn = false
                HorizontalSlideView.slideLeft(slideLayout)
              } else if (_next && velX < 0) {
                HorizontalSlideView.slideRight(slideLayout, -dim.x)
              }
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
            motion match {
              case YMotion =>
                VerticalSlideView.slide(playerFrame)
              case XMotion =>
                slide()
              case NoMotion =>
            }
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

  private var _prev = false
  private var _next = false

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

        case OnPastTrackListChanged(pastTrackList) =>
          _prev = !pastTrackList.isEmpty
          true
        case OnPresentTrackOptionChanged(presentTrackOp) =>
          slideLayout.setX(-dim.x)
          true
        case OnFutureTrackListChanged(futureTrackList) =>
          _next = !futureTrackList.isEmpty 
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
