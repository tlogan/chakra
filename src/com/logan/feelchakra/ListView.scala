package com.logan.feelchakra
import RichListView.listView2RichListView
import RichView.view2RichView
import RichContext.context2RichContext

object ListView {

  def createMain(context: Context, adapter: BaseAdapter): ListView = {
    val lv = new ListView(context) 
    lv.setBackgroundColor(BLACK)
    lv.setDivider(new ColorDrawable(BLACK))
    lv.setDividerHeight(context.dp(4))
    lv.setAdapter(adapter) 
    lv
  }

}
