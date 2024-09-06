package klev.db.auth

import klev.plugins.UUIDSerializer
import kotlinx.datetime.Clock.System.now
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import java.util.UUID
import kotlin.random.Random
import kotlin.time.DurationUnit
import kotlin.time.toDuration

@Serializable
data class OneTimePassword(
    @Serializable(with = UUIDSerializer::class) val id: UUID = UUID.randomUUID(),
    val email: String,
    val code: Int = generateCode(),
    val validUntil: Instant = now().plus(10.toDuration(DurationUnit.MINUTES)),
) {
    companion object {
        fun generateCode() =
            Random.nextInt(
                from = 100_000,
                until = 1_000_000,
            )
    }
}
