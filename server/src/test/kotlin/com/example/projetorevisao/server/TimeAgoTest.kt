package com.example.projetorevisao.server

import com.example.projetorevisao.server.repository.timeAgoLabel
import org.junit.Assert.assertEquals
import org.junit.Test

class TimeAgoTest {
    private val now = 10_000_000_000L
    private fun ago(minutes: Long) = timeAgoLabel(now - minutes * 60_000, now)

    @Test
    fun labelsFollowTheAgeOfTheReport() {
        assertEquals("Agora mesmo", ago(0))
        assertEquals("há 5 min", ago(5))
        assertEquals("há 3 h", ago(3 * 60))
        assertEquals("ontem", ago(30 * 60))
        assertEquals("há 4 dias", ago(4 * 24 * 60))
    }
}
