package com.logan.feelchakra

import android.util.Log
import android.widget.Toast

import RichView.view2RichView
import RichListView.listView2RichListView

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

  private var _selectionList: List[Selection] = List() 
  var _playerOpen: Boolean = false

  private val handler = new Handler(new HandlerCallback() {
    override def handleMessage(msg: Message): Boolean = {
      import UI._
      msg.obj match {
        case OnSelectionListChanged(selectionList) => 
          _selectionList = selectionList
          that.createSelectionTabs(selectionList)
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

  lazy val frameDivideY = getResources().getDisplayMetrics().heightPixels - 200
  val velMs = 2

  lazy val selectionFrame = new FrameLayout(that) {
    setId(MainActivity.selectionFrameId)
    setLayoutParams {
      new RLLayoutParams(MATCH_PARENT, frameDivideY)
    }
  } 

  lazy val playerFrame = new FrameLayout(that) {
    setId(MainActivity.playerFrameId)
    setLayoutParams {
      new RLLayoutParams(MATCH_PARENT, MATCH_PARENT)
    }
  }


  lazy val contentView: RelativeLayout = new RelativeLayout(this) {

    val gestureDetector = new GestureDetector(that, new SimpleOnGestureListener {

      override def onDown(e: MotionEvent): Boolean = {

        val touchStartY = e.getY().toInt
        if (!_playerOpen && touchStartY > frameDivideY) {
          true
        } else if (_playerOpen && touchStartY < 100) {
          true
        } else {
          false
        }

      }

      override def onScroll(e1: MotionEvent, e2: MotionEvent, distX: Float, distY: Float): Boolean = {
        val totalDispY = e2.getY().toInt - e1.getY().toInt 
        val offset = if (_playerOpen) totalDispY else totalDispY + frameDivideY 
        if (totalDispY < 0) {
          playerFrame.setY(Math.max(offset, 0))
        } else {
          selectionFrame.setVisibility(VISIBLE)
          playerFrame.setY(Math.min(offset, frameDivideY))
        }
        true
      }

      override def onFling(e1: MotionEvent, e2: MotionEvent, velX: Float, velY: Float): Boolean = {
        if (velY < 0) {
          playerFrame.animate()
            .y(0)
            .setDuration(playerFrame.getY().toInt/velMs)
            .setListener(new AnimatorListenerAdapter() {
              override def onAnimationEnd(animator: Animator): Unit = {
                mainActorRef ! MainActor.SetPlayerOpen(true)
              }
            })
        } else {
          playerFrame.animate().y(frameDivideY).setDuration((frameDivideY - playerFrame.getY().toInt)/velMs)
            .setListener(new AnimatorListenerAdapter() {
              override def onAnimationEnd(animator: Animator): Unit = {
                mainActorRef ! MainActor.SetPlayerOpen(false)
              }
            })
        }
        true
      }
    })

    this.setOnTouch((view, event) => {
      if (!gestureDetector.onTouchEvent(event)) {
        event.getAction() match {
          case ACTION_UP => 
            if (playerFrame.getY() < frameDivideY / 2) {
              playerFrame.animate()
                .y(0)
                .setDuration((playerFrame.getY().toInt)/velMs)
                .setListener(new AnimatorListenerAdapter() {
                  override def onAnimationEnd(animator: Animator): Unit = {
                    mainActorRef ! MainActor.SetPlayerOpen(true)
                  }
                })
            } else {
              playerFrame.animate().y(frameDivideY).setDuration((frameDivideY - playerFrame.getY().toInt)/velMs)
                .setListener(new AnimatorListenerAdapter() {
                  override def onAnimationEnd(animator: Animator): Unit = {
                    mainActorRef ! MainActor.SetPlayerOpen(false)
                  }
                })
            }
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
    super.onCreate(savedInstanceState)

    setContentView(contentView)

    val playerFragTrans = getFragmentManager().beginTransaction()
    playerFragTrans.replace(playerFrame.getId(), new PlayerFragment).commit()

    val playerServiceIntent = new Intent(this, classOf[PlayerService])
    startService(playerServiceIntent);

    mainActorRef ! MainActor.Subscribe(this.toString, handler)
    mainActorRef ! MainActor.SetDatabase(new Database(this))
    mainActorRef ! MainActor.SetCacheDir(getCacheDir())

  }

  override def onCreateOptionsMenu(menu: Menu): Boolean  = {
    //getMenuInflater().inflate(R.menu.selection, menu);

    super.onCreateOptionsMenu(menu)
  }

  override def onOptionsItemSelected(item: MenuItem): Boolean = {
    false
  }

  override def onDestroy(): Unit = {
    super.onDestroy()
    val playerServiceIntent = new Intent(this, classOf[PlayerService])
    stopService(playerServiceIntent);
    mainActorRef ! MainActor.Unsubscribe(this.toString)
  }

  private def createSelectionTabs(selectionList: List[Selection]): Unit = {

    that.getActionBar().setNavigationMode(NAVIGATION_MODE_TABS)
    that.getActionBar().setDisplayShowTitleEnabled(false)
    that.getActionBar().setDisplayShowHomeEnabled(false)
    that.getActionBar().removeAllTabs()

    selectionList foreach { selection => 
      val tabListener = new TabListener() {
        override def onTabSelected(tab: Tab, ft: FragmentTransaction): Unit = {
          mainActorRef ! MainActor.SetSelection(selection)
        }

        override def onTabUnselected(tab: Tab, ft: FragmentTransaction): Unit = {
        }

        override def onTabReselected(tab: Tab, ft: FragmentTransaction): Unit = {
        }
      }

      val tab = that.getActionBar().newTab().setText(selection.label).setTabListener(tabListener)

      that.getActionBar().addTab(tab)
    }
    that.getActionBar().setSelectedNavigationItem(1)


  }


  private def setPlayerVisibility(playerOpen: Boolean): Unit = {
    if (playerOpen) {
      Log.d("chakra", "Player Opened!!")
      playerFrame.setY(0)
      selectionFrame.setVisibility(GONE)
    } else {
      selectionFrame.setVisibility(VISIBLE)
      playerFrame.setY(frameDivideY)
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
