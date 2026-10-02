package com.aegis.agent.accessibility

import android.accessibilityservice.AccessibilityService
import android.graphics.Rect
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.aegis.agent.core.AppContainer

class AegisAccessibilityService:AccessibilityService(){
 override fun onServiceConnected(){super.onServiceConnected();instance=this;AppContainer.screenObserver?.onServiceConnected(this)}
 override fun onAccessibilityEvent(event:AccessibilityEvent?){if(event!=null)AppContainer.screenObserver?.onEvent(event,rootInActiveWindow)}
 override fun onInterrupt(){}
 override fun onDestroy(){AppContainer.screenObserver?.onServiceDisconnected();instance=null;super.onDestroy()}
 fun snapshot():ScreenSnapshot{val root=rootInActiveWindow;val texts=mutableListOf<String>();val clicks=mutableListOf<ClickableElement>();fun walk(n:AccessibilityNodeInfo?){if(n==null)return;n.text?.toString()?.takeIf{it.isNotBlank()}?.let{texts+=it};if(n.isClickable){val r=Rect();n.getBoundsInScreen(r);clicks+=ClickableElement(n.text?.toString().orEmpty(),n.contentDescription?.toString().orEmpty(),r.toString())};for(i in 0 until n.childCount)walk(n.getChild(i))};walk(root);return ScreenSnapshot(root?.packageName?.toString(),texts.distinct(),clicks)}
 data class ScreenSnapshot(val packageName:String?,val visibleText:List<String>,val clickable:List<ClickableElement>)
 data class ClickableElement(val text:String,val contentDescription:String,val bounds:String)
 companion object{@Volatile var instance:AegisAccessibilityService?=null;private set;fun isEnabled()=instance!=null;fun getRoot():AccessibilityNodeInfo?=instance?.rootInActiveWindow}
}
