package com.wafflestudio.seminar.spring2023.playlist.service

import com.wafflestudio.seminar.spring2023.playlist.repository.PlaylistRepository
import com.wafflestudio.seminar.spring2023.playlist.repository.PlaylistViewRepository
import com.wafflestudio.seminar.spring2023.playlist.service.SortPlaylist.Type
import org.springframework.stereotype.Service
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.support.TransactionTemplate
import java.time.LocalDateTime
import java.util.concurrent.Executors
import java.util.concurrent.Future

@Service
class PlaylistViewServiceImpl(
    val playlistRepository: PlaylistRepository,
    val playlistviewRepository: PlaylistViewRepository,
    txManager: PlatformTransactionManager,
) : PlaylistViewService, SortPlaylist {

    private val executors = Executors.newFixedThreadPool(8)
    private val txTemplate = TransactionTemplate(txManager)

    /**
     * 스펙:
     *  1. 같은 유저-같은 플레이리스트의 조회 수는 1분에 1개까지만 허용한다.
     *  2. PlaylistView row 생성, PlaylistEntity의 viewCnt 업데이트가 atomic하게 동작해야 한다. (둘 다 모두 성공하거나, 둘 다 모두 실패해야 함)
     *  3. 플레이리스트 조회 요청이 동시에 다수 들어와도, 요청이 들어온 만큼 조회 수가 증가해야한다. (동시성 이슈가 없어야 함)
     *  4. 성공하면 true, 실패하면 false 반환
     *
     * 조건:
     *  1. Synchronized 사용 금지.
     *  2. create 함수 처리가, 플레이리스트 조회 API 응답시간에 영향을 미치면 안된다.
     *  3. create 함수가 실패해도, 플레이리스트 조회 API 응답은 성공해야 한다.
     *  4. Future가 리턴 타입인 이유를 고민해보며 구현하기.
     */
    override fun create(playlistId: Long, userId: Long, at: LocalDateTime): Future<Boolean> {
        return executors.submit<Boolean> { // 조건 2,3을 만족시키기 위해 이 작업을 다른 스레드에 위임하는 코드입니다.
            txTemplate.execute { // 스펙 2를 만족시키기 위해 트랜잭션을 적용하는 코드입니다.  @Transactional 어노테이션은 다른 스레드에서 동작하지 않기 때문에, 직접 코드로 트랜잭션을 처리하는 것입니다.
                if (true) { // TODO (1) true를 수정해서 스펙 1을 만족시켜야 합니다. playlistViewRepository에 정의된 함수를 이용하시면 됩니다.
                    return@execute false
                }


                // TODO (2) playlistViewEntity를 저장합니다.

                // TODO (3) playlistEntity의 viewCnt를 업데이트 합니다. playlistRepository에 정의된 함수를 이용하시면 됩니다.

                true
            }
        }
    }

    override fun invoke(playlists: List<PlaylistBrief>, type: Type, at: LocalDateTime): List<PlaylistBrief> {
        return when (type) {
            Type.DEFAULT -> playlists
            Type.VIEW -> {
                val playlistIds = playlists.map { it.id }
                val viewCounts = playlistRepository.findAllById(playlistIds).associateBy({ it.id }, { it.viewCnt })
                playlists.sortedByDescending { viewCounts[it.id] }
            }

            Type.HOT -> {
                // TODO() (4) 인자로 들어온 playlists와 관련된 최근 1시간 동안의 playlistView를 전부 조회한 후에, (playlist-최근 1시간 조회수) 꼴의 맵을 만들고, 그에 따라 정렬해주세요. playlistViewRepository에 정의된 함수를 사용하세요.
                playlists
            }
        }
    }
}