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



  private val albumSelectionFragment =  AlbumSelectionFragment.create()
  private val artistSelectionFragment =  ArtistSelectionFragment.create()
  private val trackSelectionFragment =  TrackSelectionFragment.create()
  private val stationSelectionFragment = StationSelectionFragment.create()

  private lazy val dim = dimension(this)

  private def resourceDim(stringId: String) = {

    val resourceId = getResources().getIdentifier(stringId, "dimen", "android")
    if (resourceId > 0) {
      getResources().getDimensionPixelSize(resourceId);
    } else {
      0
    } 
  }

  lazy val statusBarHeight = resourceDim("status_bar_height") 
  lazy val topAreaHeight = this.dp(medDp)
  lazy val touchAreaHeight = this.dp(medDp) + 2 * this.dp(smallDp)
  lazy val contentHeight =  dim.y - statusBarHeight - topAreaHeight - touchAreaHeight

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

  var _playerOpen: Boolean = false
  var _selectionMap: Map[Selection, TextView] = new HashMap()
  var _selectionOp: Option[Selection] = None  
  var _playerPartMap: Map[PlayerPart, TextView] = new HashMap()
  var _playerPartOp: Option[PlayerPart] = None  

  def selectNavItem[T](tmap: Map[T, TextView], deselectOp: Option[T], selection: T): Option[T] = {
    deselectOp match {
      case Some(deselection) => 
        tmap(deselection).setTextColor(WHITE)
      case None =>
    }
    tmap(selection).setTextColor(BLUE)
    Some(selection)
  }


  private def navLayout(): LinearLayout = {
    val ll = new LinearLayout(that)
    ll.setBackgroundColor(BLACK)
    ll.setLayoutParams {
      new RLLayoutParams(MATCH_PARENT, topAreaHeight)
    }
    ll.setGravity(CENTER)
    ll
  }

  lazy val selectionNavLayout = navLayout()
  lazy val queueNavLayout = navLayout()

  lazy val navFrame = new FrameLayout(that) {
    setLayoutParams {
      new RLLayoutParams(MATCH_PARENT, WRAP_CONTENT)
    }
  } 


  lazy val selectionFrame = new FrameLayout(that) {
    setId(MainActivity.selectionFrameId)
    setBackgroundColor(WHITE)
    setY(topAreaHeight)
    setLayoutParams {
      new RLLayoutParams(MATCH_PARENT, WRAP_CONTENT)
    }
  } 


  private var _xGestureOn = true

  lazy val playerFrame = new FrameLayout(that) with VerticalSlideView {
    
    override val velMs = 2
    override val upY = topAreaHeight
    override lazy val downY = dim.y - statusBarHeight - touchAreaHeight
    override def onSlideUpEnd() = {
      mainActorRef ! MainActor.SetPlayerOpen(true)
    }
    override def onSlideDownEnd() = {
      mainActorRef ! MainActor.SetPlayerOpen(false)
    }

    setId(MainActivity.playerFrameId)
    setBackgroundColor(WHITE)
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
        } else if (_playerOpen && touchStartY > playerFrame.upY && touchStartY < (playerFrame.upY + touchAreaHeight)) {
          true
        } else {
          false
        }

      }

      override def onSingleTapUp(e: MotionEvent): Boolean = {
        mainActorRef ! MainActor.FlipPlaying
        true
      }

      override def onScroll(e1: MotionEvent, e2: MotionEvent, scrollX: Float, scrollY: Float): Boolean = {
        val totalDispY = e2.getY().toInt - e1.getY().toInt 
        val totalDispX = e2.getX().toInt - e1.getX().toInt 

        if (motion == YMotion || (motion == NoMotion && Math.abs(totalDispY) > Math.abs(totalDispX))) {
          motion = YMotion
          val offset = if (_playerOpen) totalDispY  + playerFrame.upY else totalDispY + playerFrame.downY 
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

  }

  private val handler = new Handler(new HandlerCallback() {
    override def handleMessage(msg: Message): Boolean = {
      import UI._
      msg.obj match {
        case OnPlayerOpenChanged(playerOpen) => 
          _playerOpen = playerOpen
          that.setPlayerVisibility(playerOpen)
          true

        case OnSelectionListChanged(selectionList) => 
          that.setSelectionList(selectionList)
          true
        case OnSelectionChanged(selection) => 
          _selectionOp = selectNavItem(_selectionMap, _selectionOp, selection)
          replaceSelectionFragment(selection)
          true

        case OnPlayerPartListChanged(list) => 
          that.setPlayerPartList(list)
          true

        case OnPlayerPartChanged(playerPart) => 
          _playerPartOp = selectNavItem(_playerPartMap, _playerPartOp, playerPart)
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

  override def onCreate(savedInstanceState: Bundle): Unit = {

    super.onCreate(savedInstanceState)
    setContentView(contentView)

    contentView.addView(navFrame)
    contentView.addView(selectionFrame)
    contentView.addView(playerFrame)
    contentView.bringChildToFront(playerFrame)

    mainActorRef ! MainActor.SetDatabase(new Database(this))
    mainActorRef ! MainActor.SetCacheDir(getCacheDir())
    mainActorRef ! MainActor.SetModHeight(contentHeight)

    val playerFragTrans = getFragmentManager().beginTransaction()
    playerFragTrans.replace(playerFrame.getId(), playerFragment).commit()

    val playerServiceIntent = new Intent(this, classOf[PlayerService])
    startService(playerServiceIntent)

    mainActorRef ! MainActor.Subscribe(this.toString, handler)
  }

  override def onDestroy(): Unit = {
    super.onDestroy()
    mainActorRef ! MainActor.Unsubscribe(this.toString)
    val playerServiceIntent = new Intent(this, classOf[PlayerService])
    stopService(playerServiceIntent);
  }

  private var _prev = false
  private var _next = false

  private def navItemView(): TextView = {
    val tv = new TextView(that)
    val lp = new LLLayoutParams(this.dp(medDp), this.dp(medDp))
    lp.setMargins(that.dp(8), 0, that.dp(8), 0)
    tv.setLayoutParams(lp)
    tv.setGravity(CENTER)
    tv.setBackgroundColor(BLACK)
    tv.setTextColor(WHITE)
    tv.setTextSize(18)
    tv
  }

  private def setPlayerPartList(list: List[PlayerPart]): Unit = {
    _playerPartMap.foreach(pair => {
      val tv = pair._2
      queueNavLayout.removeView(tv)
    })

    _playerPartMap = list.map(playerPart => {
      val tv = navItemView()
      tv.setText(playerPart.label)
      queueNavLayout.addView(tv)
      (playerPart -> tv)

    }).toMap
  }


  private def setSelectionList(selectionList: List[Selection]): Unit = {
    _selectionMap.foreach(pair => {
      val tv = pair._2
      selectionNavLayout.removeView(tv)
    })

    _selectionMap = selectionList.map(selection => {
      val tv = navItemView()
      tv.setText(selection.label)
      tv.setOnClick(view => {
        mainActorRef ! MainActor.SetSelection(selection)
      })
      selectionNavLayout.addView(tv)
      (selection -> tv)

    }).toMap

  }


  private def setPlayerVisibility(playerOpen: Boolean): Unit = {
    if (playerOpen) {
      Log.d("chakra", "Player Opened!!")
      VerticalSlideView.moveUp(playerFrame)
      selectionFrame.setVisibility(GONE)
      navFrame.removeView(selectionNavLayout)
      if (navFrame.getChildAt(0) != queueNavLayout) {
        navFrame.addView(queueNavLayout)
      }
    } else {
      Log.d("chakra", "Player Closed!!")
      selectionFrame.setVisibility(VISIBLE)
      VerticalSlideView.moveDown(playerFrame)
      navFrame.removeView(queueNavLayout)
      if (navFrame.getChildAt(0) != selectionNavLayout) {
        navFrame.addView(selectionNavLayout)
      }
    }
  }

  private def replaceSelectionFragment(selection: Selection): Unit = {

    val transaction = getFragmentManager().beginTransaction()

    val frag = selection match {
      case ArtistSelection => artistSelectionFragment
      case AlbumSelection => albumSelectionFragment
      case TrackSelection => trackSelectionFragment
      case StationSelection => stationSelectionFragment
    }

    transaction.replace(selectionFrame.getId(), frag)
    transaction.commit()
    
  }

}
