package com.wafflestudio.seminar.spring2023._web.log

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor
import org.springframework.web.util.ContentCachingRequestWrapper

/**
 * 1. preHandle을 수정하여 logRequest
 * 2. preHandle, afterCompletion을 수정하여 logSlowResponse
 */
@Component
class LogInterceptor(
    private val logRequest: LogRequest,
    private val logSlowResponse: AlertSlowResponse,
) : HandlerInterceptor {

    private val startTimestampAttribute = "startTimestamp"

    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        val requestWrapper = ContentCachingRequestWrapper(request)
        val startTimestamp = System.currentTimeMillis()
        request.setAttribute(startTimestampAttribute, startTimestamp)
        val method = requestWrapper.method
        val path = requestWrapper.requestURI
        logRequest(Request(method, path))
        return true // Return true to continue the request handling
    }

    override fun afterCompletion(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any,
        ex: Exception?
    ) {
        val startTimestamp = request.getAttribute(startTimestampAttribute) as? Long
        if (startTimestamp != null) {
            val endTimestamp = System.currentTimeMillis()
            val elapsedTime = endTimestamp - startTimestamp

            // Check if the response took 3 seconds or more
            if (elapsedTime >= 3000) {
                val requestWrapper = ContentCachingRequestWrapper(request)
                val method = requestWrapper.method
                val path = requestWrapper.requestURI
                logSlowResponse(SlowResponse(method, path, elapsedTime))
            }
        }
    }
}