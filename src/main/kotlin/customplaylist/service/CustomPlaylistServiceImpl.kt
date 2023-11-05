package com.wafflestudio.seminar.spring2023.customplaylist.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.wafflestudio.seminar.spring2023.customplaylist.repository.CustomPlaylistEntity
import com.wafflestudio.seminar.spring2023.customplaylist.repository.CustomPlaylistRepository
import com.wafflestudio.seminar.spring2023.customplaylist.repository.CustomPlaylistSongEntity
import com.wafflestudio.seminar.spring2023.customplaylist.repository.CustomPlaylistSongRepository
import com.wafflestudio.seminar.spring2023.song.repository.SongRepository
import com.wafflestudio.seminar.spring2023.song.service.Artist
import com.wafflestudio.seminar.spring2023.song.service.Song
import jakarta.persistence.EntityNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import kotlin.jvm.optionals.getOrElse

/**
 * 스펙:
 *  1. 커스텀 플레이리스트 생성시, 자동으로 생성되는 제목은 "내 플레이리스트 #{내 커스텀 플레이리스트 갯수 + 1}"이다.
 *  2. 곡 추가 시  CustomPlaylistSongEntity row 생성, CustomPlaylistEntity의 songCnt의 업데이트가 atomic하게 동작해야 한다. (둘 다 모두 성공하거나, 둘 다 모두 실패해야 함)
 *
 * 조건:
 *  1. Synchronized 사용 금지.
 *  2. 곡 추가 요청이 동시에 들어와도 동시성 이슈가 없어야 한다.(PlaylistViewServiceImpl에서 동시성 이슈를 해결한 방법과는 다른 방법을 사용할 것)
 *  3. JPA의 변경 감지 기능을 사용해야 한다.
 */
@Service
class CustomPlaylistServiceImpl(
    private val customPlaylistRepository: CustomPlaylistRepository,
    private val customPlaylistSongRepository: CustomPlaylistSongRepository,
    private val songRepository: SongRepository,
    private val objectMapper: ObjectMapper
) : CustomPlaylistService {

    override fun get(userId: Long, customPlaylistId: Long): CustomPlaylist {
        val customPlaylistEntity = customPlaylistRepository.findById(customPlaylistId).getOrElse {
            throw EntityNotFoundException("CustomPlaylist not found")
        }
        val csongs = customPlaylistSongRepository.findAllByCustomPlaylistId(customPlaylistId)
        val songIds = csongs.map { it.song.id }
        val songs = songRepository.findAllByIdWithJoinFetch(songIds)

        return CustomPlaylist(
            id = customPlaylistEntity.id,
            title = customPlaylistEntity.title,
            songs = songs.map { song ->
                Song(
                    id = song.id,
                    title = song.title,
                    artists = song.artists.map { artistEntity ->
                        Artist(
                            id = artistEntity.id,
                            name = artistEntity.artist.name
                        )
                    },
                    album = song.album.title,
                    image = song.album.image,
                    duration = song.duration.toString()
                )
            }
        )
    }

    override fun gets(userId: Long): List<CustomPlaylistBrief> {
        val playlists = customPlaylistRepository.findByUserId(userId)
        val playlistBriefs = playlists.map { customPlaylistEntity ->
            objectMapper.convertValue(customPlaylistEntity, CustomPlaylistBrief::class.java)
        }

        return playlistBriefs
    }
    @Transactional
    override fun create(userId: Long): CustomPlaylistBrief {
        val cnt = customPlaylistRepository.countByUserId(userId)
        val newPlaylist = CustomPlaylistEntity(userId = userId, title = "내 플레이리스트 #${cnt+1}")
        val savedPlaylist = customPlaylistRepository.saveAndFlush(newPlaylist)
        return CustomPlaylistBrief(savedPlaylist.id, savedPlaylist.title, 0)
    }

    override fun patch(userId: Long, customPlaylistId: Long, title: String): CustomPlaylistBrief {
        val playlist = customPlaylistRepository.findById(customPlaylistId).getOrElse {
            throw CustomPlaylistNotFoundException()
        }
        if(playlist.userId != userId){
            throw CustomPlaylistNotFoundException()
        }
        playlist.title = title
        customPlaylistRepository.save(playlist)
        return CustomPlaylistBrief(playlist.id, playlist.title, playlist.songCnt)
    }

    @Transactional
    override fun addSong(userId: Long, customPlaylistId: Long, songId: Long): CustomPlaylistBrief {

        // Find the Custom Playlist
        val customPlaylist = customPlaylistRepository.findByIdAndUserIdWithSongs(customPlaylistId, userId)
            ?: throw CustomPlaylistNotFoundException()

        // Find the Song
        val song = songRepository.findById(songId).getOrElse {
            throw SongNotFoundException()}

        customPlaylistRepository.incrementSongCount(customPlaylistId)
        customPlaylistSongRepository.insertCustomPlaylistSong(customPlaylistId, songId)

        // Create a new CustomPlaylistSongEntity
        val customPlaylistSong = CustomPlaylistSongEntity(customPlaylist = customPlaylist, song = song)

        // Add the new song to the playlist
        customPlaylist.songs.add(customPlaylistSong)

        // Increment song count
        customPlaylist.songCnt++

        // You can return the updated CustomPlaylistBrief here
        return customPlaylist.toCustomPlaylistBrief()
    }
}