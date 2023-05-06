package tel.jeelpa.saipose.domain.remote

interface BaseMedia {
    val anilistId: Int
    val malId: Int?
    val title: String
    val coverImage: String
}