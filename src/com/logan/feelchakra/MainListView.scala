package com.logan.feelchakra
import RichListView.listView2RichListView
import RichView.view2RichView
import RichContext.context2RichContext

class MainListView(context: Context, adapter: BaseAdapter) extends ListView(context) {

  setBackgroundColor(BLACK)
  setDivider(new ColorDrawable(BLACK))
  setDividerHeight(context.dp(4))
  setAdapter(adapter) 

}
