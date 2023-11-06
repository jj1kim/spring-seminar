package com.wafflestudio.seminar.spring2023.playlist.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import java.time.LocalDateTime

interface PlaylistViewRepository : JpaRepository<PlaylistViewEntity, Long>{
    // 조회 수로 인정되는지 체크를 위해 사용
    fun existsByPlaylistIdAndUserIdAndCreatedAtAfterAndCreatedAtBefore(
            playlistId: Long,
            userId: Long,
            after: LocalDateTime,
            before: LocalDateTime,
    ): Boolean

    // 인기 순 정렬을 위해 사용
    fun findAllByPlaylistIdAndCreatedAtAfter(
            playlistId: Long,
            after: LocalDateTime
    ) : List<PlaylistViewEntity>
    @Modifying
    @Query("UPDATE playlist_views c SET c.id=:id, c.playlistId=:playlistId, c.userId=:userId, c.createdAt=:createdAt")
    fun updatePlaylistViewEntity(
            id: Long=0L,
            playlistId: Long,
            userId: Long,
            createdAt: LocalDateTime,
    )

    fun findByPlaylistIdAndUserId(
            playlistId: Long,
            userId: Long,
    ) : List<PlaylistViewEntity>
}
