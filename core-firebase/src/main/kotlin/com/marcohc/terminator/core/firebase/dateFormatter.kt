package com.marcohc.terminator.core.firebase

import com.google.firebase.Timestamp
import org.threeten.bp.Instant
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneId
import org.threeten.bp.ZoneOffset
import java.util.Date

fun LocalDateTime.toMillis(): Long {
    return atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
}

fun Long.toLocalDateTime(): LocalDateTime {
    return Instant
        .ofEpochMilli(this)
        .atZone(ZoneId.systemDefault())
        .toLocalDateTime()
}

fun Date.toLocalDateTime(): LocalDateTime {
    return Instant.ofEpochMilli(time)
        .atZone(ZoneId.systemDefault())
        .toLocalDateTime()
}

fun Timestamp.toLocalDateTime(): LocalDateTime {
    return toDate().toLocalDateTime()
}

fun LocalDateTime.toTimestamp(): Timestamp {
    return Timestamp(Date(atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()))
}
