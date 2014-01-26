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

import scala.collection.immutable.List

object MainActivity {

   val mainActorConnected = 1;
   val selectionChanged = 2;
}

class MainActivity extends Activity {

  private var _fragmentContainer: FrameLayout = _

  private val that = this

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
          _fragmentContainer = new FrameLayout(that) {
            setId(23)
          }; _fragmentContainer
        }
      }
    }

    mainActorRef ! MainActor.SetMainActivityHandler(handler)

  }

  private def onMainActorConnected(selectionList: List[Selection]): Unit = {

    that.getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_TABS)
    that.getActionBar().setDisplayShowTitleEnabled(true)
    that.getActionBar().setDisplayShowHomeEnabled(true)
    that.getActionBar().removeAllTabs();

    val effect: Selection => Unit = selection => {

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
    selectionList.foreach(effect) 

  }

  private def onSelectionChanged(selection: Selection): Unit = {
    val transaction = getFragmentManager().beginTransaction()

    selection match {
      case TrackSelection => 
        transaction.replace(_fragmentContainer.getId(), new TrackSelectionFragment)
      case StationSelection =>
        transaction.replace(_fragmentContainer.getId(), new Fragment() {
          override def onCreate(savedState: Bundle): Unit = {
            super.onCreate(savedState)
          }
        })
    }

    transaction.addToBackStack(null).commit()
    
  }


  override def onCreateOptionsMenu(menu: Menu): Boolean  = {
    //getMenuInflater().inflate(R.menu.selection, menu);

    super.onCreateOptionsMenu(menu);
  }

  override def onOptionsItemSelected(item: MenuItem): Boolean = {
    false;
  }

}
