package com.wafflestudio.seminar.spring2023.customplaylist.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.transaction.annotation.Isolation
import org.springframework.transaction.annotation.Transactional

interface CustomPlaylistRepository : JpaRepository<CustomPlaylistEntity, Long> {

    fun findByUserId(userId: Long): List<CustomPlaylistEntity>
    fun countByUserId(userId: Long): Long

    @Query("""
    SELECT DISTINCT cp 
    FROM custom_playlists cp
    LEFT JOIN FETCH cp.songs 
    WHERE cp.id = :id AND cp.userId = :user_id
""")
    fun findByIdAndUserIdWithSongs(@Param("id") customPlaylistId: Long, @Param("user_id") userId: Long): CustomPlaylistEntity?


    @Modifying
    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Query(value = """
        UPDATE custom_playlists 
        SET song_cnt = song_cnt + 1 
        WHERE id = :id
        """, nativeQuery = true)
    fun incrementSongCount(@Param("id") customPlaylistId: Long): Int

}
