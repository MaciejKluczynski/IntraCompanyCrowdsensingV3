package com.kluczynski.maciej.intracompanycrowdsensingv3.domain

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.kluczynski.maciej.intracompanycrowdsensingv3.data.SensingRequestModel
import com.kluczynski.maciej.intracompanycrowdsensingv3.data.UserPreferencesModel
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

class SensingRequestsAllocationAlgorithm {

    @RequiresApi(Build.VERSION_CODES.O)
    private fun planQuestionForDates(sensingRequests: MutableList<SensingRequestModel>,
                                     examinationPlan: MutableList<ExaminationPlanModel>,
                                     maxNumberOfQuestionsDaily: Int,
                                     startDate: Date) {
        for (tempSensingRequest in sensingRequests) {
            if (tempSensingRequest.desired_day_of_the_week == null) {
                var tempDate = startDate
                for (i in examinationPlan) {
                    if (i.singleDateOfExaminationPlan >= tempDate && i.allocatedSensingRequests.size < maxNumberOfQuestionsDaily) {
                        i.allocatedSensingRequests.add(tempSensingRequest)
                        tempDate = getNextDate(tempDate, getDaysInterval(tempSensingRequest.frequency))
                    }
                }
            } else {
                var tempDate = startDate
                for (i in examinationPlan) {
                    if (i.singleDateOfExaminationPlan >= tempDate &&
                            i.singleDateOfExaminationPlan.day == getDayNumber(tempSensingRequest.desired_day_of_the_week!!.value) &&
                            i.allocatedSensingRequests.size < maxNumberOfQuestionsDaily) {
                        i.allocatedSensingRequests.add(tempSensingRequest)
                        tempDate = getNextDate(tempDate, getDaysInterval(tempSensingRequest.frequency))
                    }
                }
            }
        }

    }

    private fun getDaysInterval(interval: String): Long {
        return when (interval) {
            "EVERYDAY" -> 1
            "ONCE_A_WEEK" -> 7
            "ONCE_A_MONTH" -> 28
            else -> 0
        }
    }

    private fun getDayNumber(dayName: String): Int {
        return when (dayName) {
            "MONDAY" -> 1
            "TUESDAY" -> 2
            "WEDNESDAY" -> 3
            "THURSDAY" -> 4
            "FRIDAY" -> 5
            else -> 0
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getNextDate(tempDate: Date, plusDates: Long): Date {
        val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd-M-yyyy HH:mm:ss")
        val dt: LocalDateTime = LocalDate.parse(DateManager().convertDateToString(tempDate), formatter).atStartOfDay()
        val ldt = LocalDateTime.from(dt).plusDays(plusDates)
        return Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant())
    }

    private fun determineConsistencyBetweenSensingRequestAndUser(
            sensingRequests: MutableList<SensingRequestModel>,
            userPreferredDays: MutableList<Int>,
            userPreferredTimeSlots:List<String>
    ) {
        //petla sprawdzajaca czy da sie spelnic wymogi desired_day i preffered_time w sensing requests w stosunku do preferred day uzytkownika
        //jesli nie - alokacja do null'a
        for (sensing_request in sensingRequests) {
            val tempPreferowanyDzien = sensing_request.desired_day_of_the_week
            var tempPreferowanyDzienInt = 0
            val tempPreferowanySlot = sensing_request.desired_time_of_the_day
            if (tempPreferowanyDzien != null) {
                tempPreferowanyDzienInt = getDayNumber(tempPreferowanyDzien.value)
            }
            if (tempPreferowanyDzienInt !in userPreferredDays)
                sensing_request.desired_day_of_the_week = null
            if(tempPreferowanySlot!!.name !in userPreferredTimeSlots){
                sensing_request.desired_time_of_the_day = null
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun allocateDatesForExamination(startDate: Date, userPreferredDays: MutableList<Int>, examinationPlan: MutableList<ExaminationPlanModel>, endDate: Date) {
        //wypelnienie listy datami na zadawanie pytan
        var tempDate = startDate
        while (endDate.after(tempDate)) {
            if (tempDate.day in userPreferredDays) {
                examinationPlan.add(ExaminationPlanModel(tempDate, mutableListOf()))
            }
            tempDate = getNextDate(tempDate, 1)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun allocateSensingRequests(
            userPreferences: UserPreferencesModel,
            sensingRequests: MutableList<SensingRequestModel>
    ) {
        val dateFormat = DateManager().getSimpleDateOnlyFormat()
        //variables initialization
        val startDate = dateFormat.parse(userPreferences.start_date)
        val endDate = dateFormat.parse(userPreferences.end_date)
        val tempUserPreferredDays = userPreferences.preferred_days
        val userPreferredDays: MutableList<Int> = mutableListOf()
        val userPreferredTimeSlots = userPreferences.preferred_times
        val maxNumberOfQuestionsDaily = userPreferences.max_number_per_day
        val examinationPlan: MutableList<ExaminationPlanModel> = mutableListOf()
        //preferowane dni przez uzytkownika - postac numeryczna
        for (i in tempUserPreferredDays) {
            //in java.util Monday = 1 and then ... Sunday = 7
            userPreferredDays.add(getDayNumber(i))
        }

        allocateDatesForExamination(startDate, userPreferredDays, examinationPlan, endDate)
        determineConsistencyBetweenSensingRequestAndUser(sensingRequests, userPreferredDays,userPreferredTimeSlots)

        planQuestionForDates(sensingRequests, examinationPlan, maxNumberOfQuestionsDaily,startDate)
        Log.d("TEST",examinationPlan.toString())
        Log.d("TEST",sensingRequests.toString())

    }


}