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

import scala.collection.immutable.List

import org.scaloid.common._

object MainActivity {

   val SelectionChanged = 1;
   val MainActorConnected = 2;
}

class MainActivity extends SActivity {

  private var _fragmentContainer: SFrameLayout = _

  private val that = this

  private val handler = new Handler(new Handler.Callback() {
    override def handleMessage(msg: Message): Boolean = {
      msg.obj match {
        case selection: Selection if (msg.what == MainActivity.SelectionChanged) => 
          that.onSelectionChanged(selection); true
        case selectionList: List[Selection] if (msg.what == MainActivity.MainActorConnected) => 
          that.onMainActorConnected(selectionList); true
        case _ => false
      }
    }
  })

  private val mainActorRef = MainActor.mainActorRef

  
  override def onCreate(savedInstanceState: Bundle): Unit = {
    super.onCreate(savedInstanceState)

    mainActorRef ! MainActor.SetMainActivityHandler(handler)

    contentView = new SVerticalLayout {
      _fragmentContainer = SFrameLayout().id = 23 
    }

  }

  def onSelectionChanged(selection: Selection): Unit = {
    toast("selection changed to  " + selection.label)

    getFragmentManager().beginTransaction().replace(_fragmentContainer.id, new Fragment {

      override def onCreate(savedState: Bundle) = {
        super.onCreate(savedState);
        toast("create fragment for " + selection.label)
      }

    }).addToBackStack(null).commit()
    
  }

  def onMainActorConnected(selectionList: List[Selection]): Unit = {

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

  override def onCreateOptionsMenu(menu: Menu): Boolean  = {
    //getMenuInflater().inflate(R.menu.selection, menu);

    super.onCreateOptionsMenu(menu);
  }

  override def onOptionsItemSelected(item: MenuItem): Boolean = {
    false;
  }

}
