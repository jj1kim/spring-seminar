package com.wafflestudio.seminar.spring2023.admin.service

import com.wafflestudio.seminar.spring2023.song.repository.*
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AdminBatchServiceImpl(
    val albumRepository: AlbumRepository,
    val artistRepository: ArtistRepository
) : AdminBatchService {

    @Transactional(readOnly = true)
    override fun insertAlbums(albumInfos: List<BatchAlbumInfo>) {
        val batchSize = 100 // Adjust as needed
        val albumsToSave = mutableListOf<AlbumEntity>()

        for (albumInfo in albumInfos) {
            // Create the AlbumEntity and add it to the list
            val album = AlbumEntity(
                title = albumInfo.title,
                image = albumInfo.image,
                artist = getOrCreateArtist(albumInfo.artist),
                songs = mutableListOf()
            )
            albumsToSave.add(album)

            // Create SongEntities
            for (songInfo in albumInfo.songs) {
                val song = SongEntity(
                    title = songInfo.title,
                    artists = mutableListOf(),
                    duration = songInfo.duration,
                    album = album
                )

                for (artistName in songInfo.artists) {
                    val artist = getOrCreateArtist(artistName)
                    val songArtist = SongArtistEntity(song = song, artist = artist)
                    song.artists.add(songArtist)
                }

                album.songs.add(song)
            }

            // Save albums in batches
            if (albumsToSave.size >= batchSize) {
                albumRepository.saveAll(albumsToSave)
                albumsToSave.clear()
            }
        }

        if (albumsToSave.isNotEmpty()) {
            albumRepository.saveAll(albumsToSave)
        }
    }

    private fun getOrCreateArtist(artistName: String): ArtistEntity {
        val existingArtist = artistRepository.findByName(artistName)
        return existingArtist ?: artistRepository.save(ArtistEntity(name = artistName))
    }
}


