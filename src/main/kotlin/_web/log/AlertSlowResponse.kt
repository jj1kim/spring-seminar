package com.wafflestudio.seminar.spring2023._web.log

import org.slf4j.LoggerFactory
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Future

interface AlertSlowResponse {
    operator fun invoke(slowResponse: SlowResponse): Future<Boolean>
}

data class SlowResponse(
    val method: String,
    val path: String,
    val duration: Long,
)

/**
 * 스펙:
 *  1. 3초 이상 걸린 응답을 "[API-RESPONSE] GET /api/v1/playlists/7, took 3132ms, PFCJeong" 꼴로 로깅 (logging level은 warn)
 *  2. 3초 이상 걸린 응답을 "[API-RESPONSE] GET /api/v1/playlists/7, took 3132ms, PFCJeong" 꼴로 슬랙 채널에 전달 (http method, path, 걸린 시간, 본인의 깃허브 아이디)
 *  3. RestTemplate을 사용하여 아래와 같이 요청을 날린다.
 *      curl --location 'https://slack.com/api/chat.postMessage' \
 *           --header 'Authorization: Bearer $slackToken' \
 *           --header 'Content-Type: application/json' \
 *           --data '{ "text":"[API-RESPONSE] GET /api/v1/playlists/7, took 3132ms, PFCJeong", "channel": "#spring-assignment-channel"}'
 *  4. 위 요청의 응답은 "{ "ok": true }"로 온다. invoke 함수는 이 "ok" 응답 값을 반환.
 *  5. 슬랙 API의 성공 여부와 상관 없이, 우리 서버의 응답은 정상적으로 내려가야 한다.
 */
@Component
class AlertSlowResponseImpl() : AlertSlowResponse {
    private val restTemplate: RestTemplate = RestTemplate()
    private val logger = LoggerFactory.getLogger(javaClass)
    override operator fun invoke(slowResponse: SlowResponse): Future<Boolean> {
        val githubId = "jj1kim"
        val logMessage = "[API-RESPONSE] ${slowResponse.method} ${slowResponse.path}, took ${slowResponse.duration} ms, $githubId"
        logger.info(logMessage)
        return CompletableFuture.completedFuture(true)
    }

    fun sendMessageToSlack(message: String, channel: String): Boolean {
        val url = "https://slack.com/api/chat.postMessage"
        val slackToken = " xoxb-5766809406786-6098325284464-zP8LXXRQtHaKHeirX3U1OkOd"

        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        headers.set("Authorization", "Bearer $slackToken")

        val requestBody = mapOf(
            "text" to message,
            "channel" to channel
        )

        val request = HttpEntity(requestBody, headers)

        val responseEntity: ResponseEntity<Map<*, *>> = restTemplate.postForEntity(url, request, Map::class.java)

        return responseEntity.body?.get("ok") == true
    }
}
