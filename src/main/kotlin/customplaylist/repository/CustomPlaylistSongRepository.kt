package com.wafflestudio.seminar.spring2023.customplaylist.repository

import com.wafflestudio.seminar.spring2023.song.repository.SongEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.transaction.annotation.Isolation
import org.springframework.transaction.annotation.Transactional

interface CustomPlaylistSongRepository : JpaRepository<CustomPlaylistSongEntity, Long> {

    @Modifying
    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Query(
        value = "INSERT INTO custom_playlist_songs (custom_playlist_id, song_id) VALUES (:customPlaylistId, :songId)", nativeQuery = true
    )
    fun insertCustomPlaylistSong(@Param("customPlaylistId") customPlaylistId: Long, @Param("songId") songId: Long)

    fun findAllByCustomPlaylistId(customPlaylistId: Long): List<CustomPlaylistSongEntity>

}
