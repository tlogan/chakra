package com.feelchakra

import android.app.Activity
import android.app.Fragment
import android.os.Bundle
import android.app.ActionBar
import android.app.FragmentTransaction
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.view._
import android.widget._
import android.widget.LinearLayout.LayoutParams._
import android.view.ViewGroup.LayoutParams._ 

import scala.collection.immutable.List
import guava.scala.android.Database

import android.graphics.Color

import android.util.Log 
import scala.util.{Success,Failure}
object MainActivity {

   val mainActorConnected = 1;
   val selectionChanged = 2;
   
   val selectionFrameId = 23;
   val playerFrameId = 56;

}

class MainActivity extends Activity {

  private var _selectionFrame: FrameLayout = _
  private var _playerFrame: FrameLayout = _

  private val that = this
  Log.d("chakra", "started")

  private val handler = new Handler(new Handler.Callback() {
    override def handleMessage(msg: Message): Boolean = {
      msg.obj match {
        case selectionList: List[Selection] if (msg.what == MainActivity.mainActorConnected) => 
          that.onMainActorConnected(selectionList); true
        case selection: Selection if (msg.what == MainActivity.selectionChanged) => 
          that.onSelectionChanged(selection); true
        case _ => false
      }
    }
  })

  private val mainActorRef = MainActor.mainActorRef
  
  override def onCreate(savedInstanceState: Bundle): Unit = {
    super.onCreate(savedInstanceState)

    setContentView {
      new LinearLayout(this) {
        setOrientation(LinearLayout.VERTICAL)
        addView {
          _selectionFrame = new FrameLayout(that) {
            setId(MainActivity.selectionFrameId)
            setLayoutParams {
              new LinearLayout.LayoutParams(MATCH_PARENT, 0, 6)
            }
            
          }; _selectionFrame
        }
        addView {
          _playerFrame = new FrameLayout(that) {
            setId(MainActivity.playerFrameId)
            setLayoutParams {
              new LinearLayout.LayoutParams(MATCH_PARENT, 0, 1)
            }
            setBackgroundColor(Color.parseColor("#ffa500"))
          }; _playerFrame
        }
      }
    }

    mainActorRef ! MainActor.SetMainActivityHandler(handler)
    mainActorRef ! MainActor.SetMainActivityDatabase(new Database(this))

  }

  private def onMainActorConnected(selectionList: List[Selection]): Unit = {

    that.getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_TABS)
    that.getActionBar().setDisplayShowTitleEnabled(true)
    that.getActionBar().setDisplayShowHomeEnabled(true)
    that.getActionBar().removeAllTabs();

    selectionList foreach { selection => 
      val tabListener = new ActionBar.TabListener() {
        override def onTabSelected(tab: ActionBar.Tab, ft: FragmentTransaction): Unit = {
          mainActorRef ! MainActor.SetSelection(selectionList(tab.getPosition()))
        }

        override def onTabUnselected(tab: ActionBar.Tab, ft: FragmentTransaction): Unit = {
        }

        override def onTabReselected(tab: ActionBar.Tab, ft: FragmentTransaction): Unit = {
        }
      }

      val tab = that.getActionBar().newTab().setText(selection.label).setTabListener(tabListener)

      that.getActionBar().addTab(tab)
    }

  }

  private def onSelectionChanged(selection: Selection): Unit = {
    val transaction = getFragmentManager().beginTransaction()

    selection match {
      case TrackSelection => 
        transaction.replace(_selectionFrame.getId(), new TrackSelectionFragment)
      case StationSelection =>
        transaction.replace(_selectionFrame.getId(), new Fragment() {
          override def onCreate(savedState: Bundle): Unit = {
            super.onCreate(savedState)
          }
        })
    }

    transaction.commit()
    
  }


  override def onCreateOptionsMenu(menu: Menu): Boolean  = {
    //getMenuInflater().inflate(R.menu.selection, menu);

    super.onCreateOptionsMenu(menu);
  }

  override def onOptionsItemSelected(item: MenuItem): Boolean = {
    false;
  }

}
