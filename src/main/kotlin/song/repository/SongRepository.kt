package com.wafflestudio.seminar.spring2023.song.repository

import com.wafflestudio.seminar.spring2023.customplaylist.repository.CustomPlaylistSongEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface SongRepository : JpaRepository<SongEntity, Long> {
    @Query("""
        SELECT s FROM songs s
        JOIN FETCH s.album al 
        JOIN FETCH s.artists sa
        JOIN FETCH sa.artist
        WHERE s.title LIKE %:keyword%
    """)
    fun findAllByTitleContainingWithJoinFetch(keyword: String): List<SongEntity>

    @Query("""
        SELECT s FROM songs s
        JOIN FETCH s.album al 
        JOIN FETCH s.artists sa
        JOIN FETCH sa.artist
        WHERE s.id IN :ids
    """)
    fun findAllByIdWithJoinFetch(ids: List<Long>): List<SongEntity>


}
