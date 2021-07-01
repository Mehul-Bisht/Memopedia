package com.example.memopedia.util

import com.example.memopedia.util.Constants.MINUTE_MILLIS
import com.example.memopedia.util.Constants.HOUR_MILLIS
import com.example.memopedia.util.Constants.DAY_MILLIS
import java.text.SimpleDateFormat
import java.util.*

class DateTimeFormatter {

    companion object{

        fun getTimeAgo(time: Long) : String?{

            val now: Long = System.currentTimeMillis()
            if(time > now || time < 0){
                return null
            }

            val diff = now - time

            return if(diff < MINUTE_MILLIS){
                "just now"
            } else if(diff < 2 * MINUTE_MILLIS){
                "1 min"
            } else if(diff < 50 * MINUTE_MILLIS){
                "${(diff / MINUTE_MILLIS)} mins"
            } else if(diff < 90 * MINUTE_MILLIS){
                "1 h"
            } else if(diff < 24 * HOUR_MILLIS){
                "${(diff / HOUR_MILLIS)} hrs"
            } else if(diff < 48 * HOUR_MILLIS){
                "yesterday, ${getTime(time)}"
            } else {
                //"${(diff / DAY_MILLIS)} days ago"
                getDate(time)
            }
        }

        private fun getDate(time: Long) : String{

            val sdfDate = SimpleDateFormat("MM")

            val month = Date(time)
            val displayMonth = sdfDate.format(month)

            val monthName : String = when(displayMonth){
                "1" -> { "Jan" }
                "2" -> { "Feb" }
                "3" -> { "Mar" }
                "4" -> { "Apr" }
                "5" -> { "May" }
                "6" -> { "Jun" }
                "7" -> { "July" }
                "8" -> { "Aug" }
                "9" -> { "Sept" }
                "10" -> { "Oct" }
                "11" -> { "Nov" }
                "12" -> { "Dec" }
                else -> { "no month" }
            }

            val day = Date(time)
            val displayDate = sdfDate.format(day)

            val formattedDate = "$displayDate $monthName at ${getTime(time)}"

            return formattedDate
        }

        private fun getTime(time: Long) : String{

            val sdfTime = SimpleDateFormat("hh:mm aa")
            val Time = Date(time)
            val displayTime = sdfTime.format(Time)

            return displayTime
        }

    }

}