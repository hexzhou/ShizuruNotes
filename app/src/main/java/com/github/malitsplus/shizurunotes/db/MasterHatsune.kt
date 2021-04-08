package com.github.malitsplus.shizurunotes.db

import android.text.format.DateUtils
import com.github.malitsplus.shizurunotes.R
import com.github.malitsplus.shizurunotes.common.I18N
import com.github.malitsplus.shizurunotes.common.Statics
import com.github.malitsplus.shizurunotes.data.HatsuneStage
import java.lang.IllegalArgumentException
import java.lang.RuntimeException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class MasterHatsune {
    fun getHatsune(): MutableList<HatsuneStage> {
        val hatsuneStageList = mutableListOf<HatsuneStage>()
        DBHelper.get().getHatsuneSchedule(null)?.forEach { schedule ->
            val hatsuneStage = HatsuneStage(
                schedule.event_id,
                parseDate(schedule.start_time),
                parseDate(schedule.end_time),
                schedule.title
            )
            DBHelper.get().getHatsuneBattle(schedule.event_id)?.forEach { battle ->
                DBHelper.get().getWaveGroupData(battle.wave_group_id_1)?.let {
                    hatsuneStage.battleWaveGroupMap[battle.quest_name] = it.getWaveGroup(true).also { w ->
                        if (hatsuneStage.enemyIcon == Statics.UNKNOWN_ICON) {
                            hatsuneStage.enemyIcon = w.enemyList[0].iconUrl
                        }
                    }
                }
            }
            DBHelper.get().getHatsuneSP(schedule.event_id)?.forEach { sp ->
                DBHelper.get().getWaveGroupData(sp.wave_group_id)?.let {
                    hatsuneStage.battleWaveGroupMap[I18N.getString(R.string.sp_mode_d, sp.mode)] = it.getWaveGroup(true)
                }
            }
            hatsuneStageList.add(hatsuneStage)
        }
        return hatsuneStageList
    }

    fun parseDate(date: String): LocalDateTime {
        val formatters = listOf(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss"),
            DateTimeFormatter.ofPattern("yyyy/MM/dd H:mm:ss"))
        for (formatter in formatters) {
            try {
                return LocalDateTime.parse(date, formatter)
            } catch (e: RuntimeException) {}
        }
        throw IllegalArgumentException()
    }
}