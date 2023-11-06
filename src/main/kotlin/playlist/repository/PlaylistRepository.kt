package com.wafflestudio.seminar.spring2023.playlist.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query


interface PlaylistRepository : JpaRepository<PlaylistEntity, Long> {

    @Modifying
    @Query("UPDATE playlists c SET c.viewCnt = c.viewCnt + 1 WHERE c.id = :id")
    fun IncreaseviewCnt(id:Long)

    @Query("""
        SELECT p FROM playlists p 
        JOIN FETCH p.songs ps
        WHERE p.id = :id
    """)
    fun findByIdWithSongs(id: Long): PlaylistEntity?

    @Modifying
    @Query("UPDATE playlists c SET c.LastHourViewCnt = c.LastHourViewCnt+:cnt WHERE c.id = :id")
    fun IncreaseLastHourViewCnt(id:Long,cnt : Int)

}
