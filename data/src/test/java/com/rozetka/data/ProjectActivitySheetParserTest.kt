package com.rozetka.data

import com.rozetka.data.repository.ProjectActivitySheetParser
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ProjectActivitySheetParserTest {

    private val parser = ProjectActivitySheetParser()

    @Test
    fun testCsvUrlConversion() {
        val pubhtmlUrl = "https://docs.google.com/spreadsheets/d/e/2PACX-1vRHgwt89VM13uuUy8dDvpjlb-BDM4ovsIKhoLDGIaI58NJtAawxABVW7sdVRDDH5UGaM2bsryni6Im7/pubhtml?gid=643150359&single=true"
        val expected = "https://docs.google.com/spreadsheets/d/e/2PACX-1vRHgwt89VM13uuUy8dDvpjlb-BDM4ovsIKhoLDGIaI58NJtAawxABVW7sdVRDDH5UGaM2bsryni6Im7/pub?gid=643150359&single=true&output=csv"
        assertEquals(expected, parser.convertToCsvUrl(pubhtmlUrl))
    }

    @Test
    fun testParseCsvMock() {
        val csvData = """
            ЦЕНТР ПРОЕКТНОЙ ДЕЯТЕЛЬНОСТИ,,,,,
            Расписание занятий по дисциплине «Проектная деятельность»,,,,,
            ,,,,,
            №,проектное направление,название проекта,ФИО преподавателя,контактная информация,понедельник,среда
            1,Дизайн,ArtProfit. Твори и зарабатывай [1],Кречетова Мария Александровна,,,"среда\n14:30-16:00"
            2,Дизайн,Дизайн-студия «CMD+ART»,Емельяненко Сергей Константинович,89035845610,"понедельник\n10:40-12:10",
        """.trimIndent()

        val items = parser.parseCsv(csvData)
        assertEquals(2, items.size)

        val item1 = items[0]
        assertEquals("1", item1.number)
        assertEquals("Дизайн", item1.direction)
        assertEquals("ArtProfit. Твори и зарабатывай [1]", item1.projectTitle)
        assertEquals("Кречетова Мария Александровна", item1.teacher)

        val item2 = items[1]
        assertEquals("Дизайн-студия «CMD+ART»", item2.projectTitle)
        assertEquals("Емельяненко Сергей Константинович", item2.teacher)
    }

    @Test
    fun testLiveParseUrl() = runBlocking {
        val pubhtmlUrl = "https://docs.google.com/spreadsheets/d/e/2PACX-1vRHgwt89VM13uuUy8dDvpjlb-BDM4ovsIKhoLDGIaI58NJtAawxABVW7sdVRDDH5UGaM2bsryni6Im7/pubhtml?gid=643150359&single=true"
        val items = parser.parseUrl(pubhtmlUrl)
        assertFalse("Parsed items from live spreadsheet should not be empty", items.isEmpty())
        assertTrue("Should contain Eltech", items.any { it.projectTitle.contains("Eltech", ignoreCase = true) })
    }
}
