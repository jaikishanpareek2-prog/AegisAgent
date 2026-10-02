package com.aegis.agent.accessibility

class ScreenObserver{
 fun isAvailable()=AegisAccessibilityService.instance!=null
 fun observe():AegisAccessibilityService.ScreenSnapshot?=AegisAccessibilityService.instance?.snapshot()
 fun describeForAgent():String{val s=observe()?:return "Accessibility service not enabled.";return buildString{appendLine("Package: ${s.packageName?:"unknown"}");appendLine("Visible text:");s.visibleText.take(30).forEach{appendLine("  - $it")};appendLine("Clickable elements:");s.clickable.take(20).forEach{appendLine("  - ${it.text.ifBlank{it.contentDescription}} ${it.bounds}")}}}
}
