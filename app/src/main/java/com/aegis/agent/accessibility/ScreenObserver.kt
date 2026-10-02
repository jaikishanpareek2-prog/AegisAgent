package com.aegis.agent.accessibility

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

class ScreenObserver{
 fun isAvailable()=AegisAccessibilityService.instance!=null
 fun observe():AegisAccessibilityService.ScreenSnapshot?=AegisAccessibilityService.instance?.snapshot()
 fun onServiceConnected(service:AccessibilityService){}
 fun onServiceDisconnected(){}
 fun onEvent(event:AccessibilityEvent,root:AccessibilityNodeInfo?){}
 fun describeForAgent():String{val s=observe()?:return "Accessibility service not enabled.";return "Package: "+(s.packageName?:"unknown")+"\nVisible text: "+s.visibleText.take(30).joinToString(" | ")}
}
