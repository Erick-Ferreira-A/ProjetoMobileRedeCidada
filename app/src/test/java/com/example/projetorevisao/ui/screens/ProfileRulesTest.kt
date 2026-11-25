package com.example.projetorevisao.ui.screens

import com.example.projetorevisao.data.session.ageFromBirthDate
import com.example.projetorevisao.ui.theme.RcTone
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.util.Calendar

class ProfileRulesTest {

    private fun day(y: Int, m: Int, d: Int) = Calendar.getInstance().apply { set(y, m - 1, d) }

    @Test
    fun masks_formatWhileTyping() {
        assertEquals("24/05/1998", formatBirthDate("24051998"))
        assertEquals("24/05", formatBirthDate("2405"))
        assertEquals("(11) 98765-4321", formatPhone("11987654321"))
        assertEquals("(11) 3456-7890", formatPhone("1134567890"))
        assertEquals("05866-000", formatCep("05866000"))
        assertEquals("24/05/1998", formatBirthDate("24/05/1998 extra 123"))
    }

    @Test
    fun age_countsBirthdayCorrectly() {
        assertEquals(28, ageFromBirthDate("24/05/1998", day(2026, 11, 24)))
        assertEquals(27, ageFromBirthDate("25/11/1998", day(2026, 11, 24)))
        assertNull(ageFromBirthDate("31/13/1998", day(2026, 11, 24)))
        assertNull(ageFromBirthDate("24/05", day(2026, 11, 24)))
    }

    @Test
    fun trustLevels_followScoreBands() {
        assertEquals("Muito confiável", trustLevelOf(940).label)
        assertEquals("Confiável", trustLevelOf(800).label)
        assertEquals(RcTone.WARNING, trustLevelOf(600).tone)
        assertEquals(RcTone.CRITICAL, trustLevelOf(200).tone)
    }
}
