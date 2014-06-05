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

  private val albumSelectionFragment =  new AlbumSelectionFragment
  private val artistSelectionFragment =  new ArtistSelectionFragment
  private val trackSelectionFragment =  new TrackSelectionFragment
  private val stationSelectionFragment = new StationSelectionFragment

  private var menu: Menu = _ 

  var _playerOpen: Boolean = false

  lazy val frameDivideY = getResources().getDisplayMetrics().heightPixels - this.dp(200)

  lazy val selectionFrame = new FrameLayout(that) {
    setId(MainActivity.selectionFrameId)
    setLayoutParams {
      new RLLayoutParams(MATCH_PARENT, frameDivideY)
    }
  } 

  lazy val playerFrame = new FrameLayout(that) with VerticalSlideView {
    
    override val velMs = 2
    override val topY = 0
    override lazy val bottomY = frameDivideY
    override def onSlideUpEnd() = mainActorRef ! MainActor.SetPlayerOpen(true)
    override def onSlideDownEnd() = mainActorRef ! MainActor.SetPlayerOpen(false)

    setId(MainActivity.playerFrameId)
    setLayoutParams {
      new RLLayoutParams(MATCH_PARENT, MATCH_PARENT)
    }
  }


  lazy val contentView: RelativeLayout = new RelativeLayout(this) {

    val gestureDetector = new GestureDetector(that, new SimpleOnGestureListener {

      override def onDown(e: MotionEvent): Boolean = {

        val touchStartY = e.getY().toInt
        if (!_playerOpen && touchStartY > playerFrame.bottomY) {
          true
        } else if (_playerOpen && touchStartY < 100) {
          true
        } else {
          false
        }

      }

      override def onScroll(e1: MotionEvent, e2: MotionEvent, distX: Float, distY: Float): Boolean = {
        val totalDispY = e2.getY().toInt - e1.getY().toInt 
        val offset = if (_playerOpen) totalDispY else totalDispY + playerFrame.bottomY 
        if (totalDispY < 0) {
          playerFrame.setY(Math.max(offset, 0))
        } else {
          selectionFrame.setVisibility(VISIBLE)
          playerFrame.setY(Math.min(offset, playerFrame.bottomY))
        }
        true
      }

      override def onFling(e1: MotionEvent, e2: MotionEvent, velX: Float, velY: Float): Boolean = {
        if (velY < 0) {
          playerFrame.slideUp()
        } else {
          playerFrame.slideDown()
        }
        true
      }
    })

    this.setOnTouch((view, event) => {
      if (!gestureDetector.onTouchEvent(event)) {
        event.getAction() match {
          case ACTION_UP => 
            playerFrame.slide()
            true
          case ACTION_CANCEL => 
            true
          case _ =>
            false
        }
      } else true 

    })

    addView { 
      selectionFrame
    }
    addView(playerFrame)
    bringChildToFront(playerFrame)
  }

  override def onCreate(savedInstanceState: Bundle): Unit = {

    getActionBar().hide()
    getActionBar().setDisplayShowHomeEnabled(false)
    getActionBar().setDisplayShowTitleEnabled(false)
    super.onCreate(savedInstanceState)
    setContentView(contentView)

    val playerFragTrans = getFragmentManager().beginTransaction()
    playerFragTrans.replace(playerFrame.getId(), new PlayerFragment).commit()

    val playerServiceIntent = new Intent(this, classOf[PlayerService])
    startService(playerServiceIntent);

    mainActorRef ! MainActor.SetDatabase(new Database(this))
    mainActorRef ! MainActor.SetCacheDir(getCacheDir())

  }

  override def onCreateOptionsMenu(menu: Menu): Boolean  = {
    this.menu = menu
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
      playerFrame.moveTop()
      selectionFrame.setVisibility(GONE)
    } else {
      selectionFrame.setVisibility(VISIBLE)
      playerFrame.moveBottom()
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
