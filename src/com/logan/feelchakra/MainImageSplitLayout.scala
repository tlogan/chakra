package com.logan.feelchakra

import RichListView.listView2RichListView
import RichView.view2RichView
import RichContext.context2RichContext

class MainImageSplitLayout(context: Context, rightLayout: View) 
extends ImageSplitLayout(context, rightLayout) {

  rightLayout.setLayoutParams(new LLLayoutParams(MATCH_PARENT, context.dp(medDp)))

}
