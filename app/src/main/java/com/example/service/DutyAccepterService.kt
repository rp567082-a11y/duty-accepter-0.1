package com.example.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.accessibilityservice.GestureDescription
import android.content.Context
import android.graphics.Path
import android.graphics.Rect
import android.provider.Settings
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.example.data.DutyLogEntity
import com.example.data.DutyRepository
import com.example.data.DutyRuleEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DutyAccepterService : AccessibilityService() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private lateinit var repository: DutyRepository

    override fun onCreate() {
        super.onCreate()
        repository = DutyRepository.getInstance(applicationContext)
        Log.d(TAG, "DutyAccepterService created")
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        _isServiceConnected.value = true
        Log.d(TAG, "DutyAccepterService connected to Accessibility framework")

        serviceScope.launch {
            repository.addLog(
                DutyLogEntity(
                    eventType = "SERVICE_CONNECTED",
                    ruleTitle = "Accessibility Service Active",
                    packageName = packageName ?: "com.example.dutyaccepter",
                    matchedText = "Service On",
                    actionTaken = "Monitoring Window Events",
                    isSuccess = true,
                    statusMessage = "Accessibility service bound and listening"
                )
            )
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return

        val eventType = event.eventType
        if (eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED &&
            eventType != AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED
        ) {
            return
        }

        val eventPackageName = event.packageName?.toString() ?: ""

        serviceScope.launch {
            // Check Gate Access first
            val gateResult = repository.checkGateAccess()
            if (gateResult is DutyRepository.GateCheckResult.GateRequired) {
                // Subscription gate expired or required
                return@launch
            }

            val activeRules = repository.getActiveRules()
            if (activeRules.isEmpty()) return@launch

            val rootNode = rootInActiveWindow ?: event.source ?: return@launch

            for (rule in activeRules) {
                // Check package filter
                if (rule.targetPackage != "*" && 
                    !rule.targetPackage.equals(eventPackageName, ignoreCase = true) && 
                    !eventPackageName.contains(rule.targetPackage, ignoreCase = true)
                ) {
                    continue
                }

                // Search node tree for matching keyword (blank or '*' acts as universal match)
                val matchedKeywordNode = if (rule.keyword.isBlank() || rule.keyword == "*") {
                    rootNode
                } else {
                    findNodeWithText(rootNode, rule.keyword)
                }

                if (matchedKeywordNode != null) {
                    // Match found! Look for auto-click/slide button target
                    val clickTargetNode = if (rule.autoClickText.isBlank() || rule.autoClickText == "*") {
                        findAnyClickableTargetNode(rootNode)
                    } else {
                        findClickableTargetNode(rootNode, rule.autoClickText)
                    }

                    if (clickTargetNode != null) {
                        // Apply configured delay
                        if (rule.delayMs > 0) {
                            kotlinx.coroutines.delay(rule.delayMs)
                        }

                        val isSlideRequired = rule.autoClickText.contains("slide", ignoreCase = true) || 
                                             rule.autoClickText.contains("swipe", ignoreCase = true) ||
                                             rule.autoClickText.contains("drag", ignoreCase = true) ||
                                             rule.title.contains("ola", ignoreCase = true) ||
                                             rule.title.contains("porter", ignoreCase = true) ||
                                             rule.title.contains("slide", ignoreCase = true) ||
                                             rule.title.contains("swipe", ignoreCase = true) ||
                                             eventPackageName.contains("olacabs", ignoreCase = true) ||
                                             eventPackageName.contains("ola", ignoreCase = true)

                        var clickSuccess = false
                        if (isSlideRequired) {
                            clickSuccess = performSwipeGesture(clickTargetNode)
                        }

                        if (!clickSuccess) {
                            clickSuccess = clickTargetNode.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                        }

                        if (!clickSuccess) {
                            clickSuccess = performTapGesture(clickTargetNode)
                        }

                        if (!clickSuccess) {
                            // Fallback to swipe gesture if click returned false
                            clickSuccess = performSwipeGesture(clickTargetNode)
                        }

                        repository.addLog(
                            DutyLogEntity(
                                eventType = if (isSlideRequired) "AUTO_SLIDE_EXECUTED" else "AUTO_CLICK_EXECUTED",
                                ruleTitle = rule.title,
                                packageName = eventPackageName.ifEmpty { rule.targetPackage },
                                matchedText = "Keyword: '${rule.keyword}'",
                                actionTaken = if (isSlideRequired) "SLIDE_GESTURE on '${rule.autoClickText}'" else "ACTION_CLICK/TAP on '${rule.autoClickText}'",
                                isSuccess = clickSuccess,
                                statusMessage = if (clickSuccess) "Successfully executed duty accept action" else "Action attempt failed"
                            )
                        )

                        // Found and executed priority match, break to prevent multiple clicks per event
                        break
                    }
                }
            }
        }
    }

    private fun findNodeWithText(node: AccessibilityNodeInfo?, targetText: String): AccessibilityNodeInfo? {
        if (node == null) return null

        val nodeText = node.text?.toString() ?: ""
        val contentDesc = node.contentDescription?.toString() ?: ""
        val viewId = node.viewIdResourceName?.toString() ?: ""

        if (nodeText.contains(targetText, ignoreCase = true) ||
            contentDesc.contains(targetText, ignoreCase = true) ||
            (viewId.isNotEmpty() && viewId.contains(targetText, ignoreCase = true))
        ) {
            return node
        }

        for (i in 0 until node.childCount) {
            val child = node.getChild(i)
            val result = findNodeWithText(child, targetText)
            if (result != null) return result
        }

        return null
    }

    private fun findClickableTargetNode(root: AccessibilityNodeInfo?, buttonText: String): AccessibilityNodeInfo? {
        if (root == null) return null

        // First look for exact or partial button text
        val textMatch = findNodeWithText(root, buttonText)
        if (textMatch != null) {
            var curr: AccessibilityNodeInfo? = textMatch
            while (curr != null) {
                if (curr.isClickable) return curr
                curr = curr.parent
            }
            return textMatch
        }

        // Fallback: search for common action words (ACCEPT, SLIDE, SWIPE, CONFIRM, RIDE, ORDER, OLA, BOOK)
        val defaultKeywords = listOf("ACCEPT", "SLIDE", "SWIPE", "CONFIRM", "TAKE", "DUTY", "ORDER", "BOOK", "RIDE")
        for (kw in defaultKeywords) {
            val fallbackMatch = findNodeWithText(root, kw)
            if (fallbackMatch != null) {
                var curr: AccessibilityNodeInfo? = fallbackMatch
                while (curr != null) {
                    if (curr.isClickable) return curr
                    curr = curr.parent
                }
                return fallbackMatch
            }
        }

        return null
    }

    private fun findAnyClickableTargetNode(node: AccessibilityNodeInfo?): AccessibilityNodeInfo? {
        if (node == null) return null
        if (node.isClickable) return node

        for (i in 0 until node.childCount) {
            val result = findAnyClickableTargetNode(node.getChild(i))
            if (result != null) return result
        }

        return null
    }

    private fun performTapGesture(node: AccessibilityNodeInfo): Boolean {
        val bounds = Rect()
        node.getBoundsInScreen(bounds)

        if (bounds.isEmpty) return false

        val tapX = bounds.centerX().toFloat()
        val tapY = bounds.centerY().toFloat()

        val tapPath = Path().apply {
            moveTo(tapX, tapY)
        }

        val gestureBuilder = GestureDescription.Builder()
        val stroke = GestureDescription.StrokeDescription(tapPath, 0, 50)
        gestureBuilder.addStroke(stroke)

        return dispatchGesture(gestureBuilder.build(), null, null)
    }

    private fun performSwipeGesture(node: AccessibilityNodeInfo): Boolean {

        val bounds = Rect()
        node.getBoundsInScreen(bounds)

        if (bounds.isEmpty) return false

        val startX = bounds.left.toFloat() + (bounds.width() * 0.15f)
        val endX = bounds.left.toFloat() + (bounds.width() * 0.85f)
        val startY = bounds.centerY().toFloat()

        val swipePath = Path().apply {
            moveTo(startX, startY)
            lineTo(endX, startY)
        }

        val gestureBuilder = GestureDescription.Builder()
        val stroke = GestureDescription.StrokeDescription(swipePath, 0, 300)
        gestureBuilder.addStroke(stroke)

        return dispatchGesture(gestureBuilder.build(), null, null)
    }

    override fun onInterrupt() {
        _isServiceConnected.value = false
        Log.w(TAG, "DutyAccepterService interrupted")
    }

    override fun onDestroy() {
        super.onDestroy()
        _isServiceConnected.value = false
        serviceScope.cancel()
    }

    companion object {
        private const val TAG = "DutyAccepterService"

        private val _isServiceConnected = MutableStateFlow(false)
        val isServiceConnected: StateFlow<Boolean> = _isServiceConnected.asStateFlow()

        fun isAccessibilityServiceEnabled(context: Context): Boolean {
            val expectedComponentName = "${context.packageName}/${DutyAccepterService::class.java.canonicalName}"
            val enabledServicesSetting = Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            ) ?: return false

            val colonSplitter = android.text.TextUtils.SimpleStringSplitter(':')
            colonSplitter.setString(enabledServicesSetting)

            while (colonSplitter.hasNext()) {
                val componentName = colonSplitter.next()
                if (componentName.equals(expectedComponentName, ignoreCase = true) ||
                    componentName.contains(DutyAccepterService::class.java.simpleName, ignoreCase = true)
                ) {
                    return true
                }
            }

            return _isServiceConnected.value
        }
    }
}
