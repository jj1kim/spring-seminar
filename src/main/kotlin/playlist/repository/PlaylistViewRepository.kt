package com.wafflestudio.seminar.spring2023.playlist.repository

import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDateTime

interface PlaylistViewRepository : JpaRepository<PlaylistViewEntity, Long> {
    // 인기 순 정렬을 위해 사용
    fun findAllByPlaylistIdInAndCreatedAtAfter(
        playlistIds: List<Long>,
        after: LocalDateTime
    ) : List<PlaylistViewEntity>

    // 조회 수로 인정되는지 체크를 위해 사용
    fun existsByPlaylistIdAndUserIdAndCreatedAtAfterAndCreatedAtBefore(
        playlistId: Long,
        userId: Long,
        after: LocalDateTime,
        before: LocalDateTime,
    ): Boolean
}