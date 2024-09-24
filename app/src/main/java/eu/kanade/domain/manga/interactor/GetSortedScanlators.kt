package eu.kanade.domain.manga.interactor

import tachiyomi.data.DatabaseHandler
import tachiyomi.domain.manga.model.SortedScanlator

class GetSortedScanlators(
    private val handler: DatabaseHandler
) {
    suspend fun await(mangaId: Long): Set<SortedScanlator> {
        return handler.awaitList {
            sorted_scanlatorsQueries.getSortedScanlatorsByMangaId(mangaId)
        }.map {
            SortedScanlator(it.scanlator, it.rank)
        }.toSet()
    }
}
