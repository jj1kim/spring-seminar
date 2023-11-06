package com.wafflestudio.seminar.spring2023.admin.service

import com.wafflestudio.seminar.spring2023.song.repository.*
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.support.TransactionTemplate
import java.util.concurrent.Executors
import java.util.concurrent.Future

@Service
class AdminBatchServiceImpl(
        val albumRepository: AlbumRepository,
        val artistRepository: ArtistRepository,
        val txTemplate: TransactionTemplate,
) : AdminBatchService {
    private val threads = Executors.newFixedThreadPool(4) //멀티 스레드로 작업 분리 구현
    override fun insertAlbums(albumInfos: List<BatchAlbumInfo>) {
        val jobs: MutableList<Future<*>> = mutableListOf() //각 작업의 분리

        for (albumInfo in albumInfos) {
            val job = threads.submit{
                txTemplate.executeWithoutResult {
                    val albumentity= AlbumEntity(
                            title = albumInfo.title,
                            image = albumInfo.image,
                            artist = getOrCreateArtist(albumInfo.artist),
                            songs = mutableListOf()
                    )
                    val album = albumRepository.save(albumentity)

                    // Create SongEntities
                    for (songInfo in albumInfo.songs) {
                        val song = SongEntity(
                                title = songInfo.title,
                                artists = mutableListOf(),
                                duration = songInfo.duration,
                                album = albumentity
                        )

                        for (artistName in songInfo.artists) {
                            val artist = getOrCreateArtist(artistName)
                            val songArtist = SongArtistEntity(song = song, artist = artist)
                            song.artists.add(songArtist)
                        }
                        album.songs.add(song)
                    }
                }
            }
            jobs.add(job)
        }
        jobs.forEach { it.get() } //작업 목록 실행
    }

    private fun getOrCreateArtist(artistName: String): ArtistEntity {
        val existingArtist = artistRepository.findByName(artistName)
        return existingArtist ?: artistRepository.save(ArtistEntity(name = artistName))
    }
}


