package com.logan.feelchakra

object MainActivity {
   
   val selectionFrameId = 23;
   val playerFrameId = 56;

}

class MainActivity extends Activity {

  private var _selectionFrame: FrameLayout = _
  private var _playerFrame: FrameLayout = _
  private val that = this

  private val trackSelectionFragment =  new TrackSelectionFragment;
  private val stationSelectionFragment = new StationSelectionFragment;


  private val mainActorRef = MainActor.mainActorRef


  private def createSelectionTabs(selectionList: List[Selection]): Unit = {
    that.getActionBar().setNavigationMode(NAVIGATION_MODE_TABS)
    that.getActionBar().setDisplayShowTitleEnabled(true)
    that.getActionBar().setDisplayShowHomeEnabled(true)
    that.getActionBar().removeAllTabs();

    selectionList foreach { selection => 
      val tabListener = new TabListener() {
        override def onTabSelected(tab: Tab, ft: FragmentTransaction): Unit = {
          mainActorRef ! MainActor.SetSelection(selectionList(tab.getPosition()))
        }

        override def onTabUnselected(tab: Tab, ft: FragmentTransaction): Unit = {
        }

        override def onTabReselected(tab: Tab, ft: FragmentTransaction): Unit = {
        }
      }

      val tab = that.getActionBar().newTab().setText(selection.label).setTabListener(tabListener)

      that.getActionBar().addTab(tab)
    }
  }

  private def setPlayerVisibility(playerOpen: Boolean): Unit = {
    if (playerOpen) {
      _selectionFrame.setLayoutParams {
        new LLLayoutParams(MATCH_PARENT, 0, 0)
      }
    } else {
      _selectionFrame.setLayoutParams {
        new LLLayoutParams(MATCH_PARENT, 0, 6)
      }
    }
  }

  private def replaceSelectionFragment(selection: Selection): Unit = {
    val transaction = getFragmentManager().beginTransaction()

    selection match {
      case TrackSelection => 
        transaction.replace(_selectionFrame.getId(), trackSelectionFragment)
      case StationSelection =>
        transaction.replace(_selectionFrame.getId(), stationSelectionFragment)
    }

    transaction.commit()
    
  }

  private val handler = new Handler(new HandlerCallback() {
    override def handleMessage(msg: Message): Boolean = {
      import OutputHandler._
      msg.obj match {
        case OnSelectionListChanged(selectionList) => 
          that.createSelectionTabs(selectionList); true
        case OnPlayerOpenChanged(playerOpen) => 
          that.setPlayerVisibility(playerOpen); true
        case OnSelectionChanged(selection) => 
          that.replaceSelectionFragment(selection); true
        case _ => false
      }
    }
  })
  
  override def onCreate(savedInstanceState: Bundle): Unit = {
    super.onCreate(savedInstanceState)

    setContentView {
      new LinearLayout(this) {
        setOrientation(VERTICAL)
        addView {
          _selectionFrame = new FrameLayout(that) {
            setId(MainActivity.selectionFrameId)
            setLayoutParams {
              new LLLayoutParams(MATCH_PARENT, 0, 6)
            }
            
          }; _selectionFrame
        }
        addView {
          _playerFrame = new FrameLayout(that) {
            setId(MainActivity.playerFrameId)
            setLayoutParams {
              new LLLayoutParams(MATCH_PARENT, 0, 1)
            }
          }; _playerFrame
        }
      }
    }

    val playerFragTrans = getFragmentManager().beginTransaction()
    playerFragTrans.replace(_playerFrame.getId(), new PlayerFragment).commit()

    val playerServiceIntent = new Intent(this, classOf[PlayerService])
    startService(playerServiceIntent);

    mainActorRef ! MainActor.SetMainActivityDatabase(new Database(this))

    mainActorRef ! MainActor.Subscribe(this.toString, handler)

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



}