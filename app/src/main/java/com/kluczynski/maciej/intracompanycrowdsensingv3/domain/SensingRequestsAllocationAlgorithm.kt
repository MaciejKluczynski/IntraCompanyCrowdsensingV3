package com.kluczynski.maciej.intracompanycrowdsensingv3.domain

import android.annotation.SuppressLint
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.kluczynski.maciej.intracompanycrowdsensingv3.data.DayOfTheWeek
import com.kluczynski.maciej.intracompanycrowdsensingv3.data.SensingRequestModel
import com.kluczynski.maciej.intracompanycrowdsensingv3.data.TimeOfTheDay
import com.kluczynski.maciej.intracompanycrowdsensingv3.data.UserPreferencesModel
import java.time.*
import java.time.format.DateTimeFormatter
import java.util.*

class SensingRequestsAllocationAlgorithm {

    private val dateManager = DateManager()

    companion object {
        const val NONE = "x"
        const val MORNING = "MORNING"
        const val EARLY_MORNING = "EARLY_MORNING"
        const val NOON = "NOON"
        const val AFTERNOON = "AFTERNOON"
        const val EVENING = "EVENING"
        const val MONDAY = "MONDAY"
        const val TUESDAY = "TUESDAY"
        const val WEDNESDAY = "WEDNESDAY"
        const val THURSDAY = "THURSDAY"
        const val FRIDAY = "FRIDAY"
        const val EVERY_DAY = "EVERY_DAY"
        const val ONCE_A_WEEK = "ONCE_A_WEEK"
        const val ONCE_A_MONTH = "ONCE_A_MONTH"
        const val TEST_LOG_NAME = "TESTING_ALLOCATION_ALGORITHM"
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun planQuestionForDates(
        sensingRequests: MutableList<SensingRequestModel>,
        examinationPlan: MutableList<ExaminationPlanModel>,
        maxNumberOfQuestionsDaily: Int,
        startDate: Date
    ) {
        while (sensingRequests.isNotEmpty()) {
            //bierzemy z listy element o najwyzszym priorytecie zawsze
            val tempSensingRequest = provideHighestPrioritySensingRequest(sensingRequests)


            //warunek gdy jest zgodnosc z uzytkownikiem - alokacja w zgodnym dniu tygodnia
            tempSensingRequest.desired_day_of_the_week?.let {
                allocateSensingRequestInSpecificDay(
                    startDate = startDate,
                    examinationPlan = examinationPlan,
                    maxNumberOfQuestionsDaily = maxNumberOfQuestionsDaily,
                    tempSensingRequest = tempSensingRequest
                )
            } ?: run {
                allocateSensingRequestForDate(
                    startDate = startDate,
                    examinationPlan = examinationPlan,
                    maxNumberOfQuestionsDaily = maxNumberOfQuestionsDaily,
                    tempSensingRequest = tempSensingRequest
                )
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun allocateSensingRequestForDate(
        startDate: Date,
        examinationPlan: MutableList<ExaminationPlanModel>,
        maxNumberOfQuestionsDaily: Int,
        tempSensingRequest: SensingRequestModel
    ) {
        //warunek gdy nie ma zgodnosci z uzytkownikiem - alokacja w dolonej dacie
        var tempDate = startDate
        for (dayOfExamination in examinationPlan) {
            if (canWeAllocateSensingRequestForDate(
                    examinationPlanDay = dayOfExamination,
                    tempDate = tempDate,
                    maxNumberOfQuestionsDaily = maxNumberOfQuestionsDaily
                )
            ) {
                val copyOfTempSensingRequest = SensingRequestModel(
                    sensing_request_id = tempSensingRequest.sensing_request_id,
                    priority = tempSensingRequest.priority,
                    frequency = tempSensingRequest.frequency,
                    desired_time_of_the_day = tempSensingRequest.desired_time_of_the_day,
                    desired_day_of_the_week = tempSensingRequest.desired_day_of_the_week,
                    content = tempSensingRequest.content,
                    questionType = tempSensingRequest.questionType,
                    why_ask = tempSensingRequest.why_ask,
                    hint = tempSensingRequest.hint,
                    time = tempSensingRequest.time,
                )
                dayOfExamination.allocatedSensingRequests.add(copyOfTempSensingRequest)
                tempDate = getNextDate(tempDate, getDaysInterval(tempSensingRequest.frequency))
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun allocateSensingRequestInSpecificDay(
        startDate: Date,
        examinationPlan: MutableList<ExaminationPlanModel>,
        maxNumberOfQuestionsDaily: Int,
        tempSensingRequest: SensingRequestModel
    ) {
        var tempDate = startDate
        for (dayOfExamination in examinationPlan) {
            if (canWeAllocateSensingRequestForSpecificDay(
                    examinationPlanDay = dayOfExamination,
                    tempDate = tempDate,
                    maxNumberOfQuestionsDaily = maxNumberOfQuestionsDaily,
                    desiredDayOfTheWeek = tempSensingRequest.desired_day_of_the_week!!.value
                )
            ) {
                val copyOfTempSensingRequest = SensingRequestModel(
                    sensing_request_id = tempSensingRequest.sensing_request_id,
                    priority = tempSensingRequest.priority,
                    frequency = tempSensingRequest.frequency,
                    desired_time_of_the_day = tempSensingRequest.desired_time_of_the_day,
                    desired_day_of_the_week = tempSensingRequest.desired_day_of_the_week,
                    content = tempSensingRequest.content,
                    questionType = tempSensingRequest.questionType,
                    why_ask = tempSensingRequest.why_ask,
                    hint = tempSensingRequest.hint,
                    time = tempSensingRequest.time,
                )
                dayOfExamination.allocatedSensingRequests.add(copyOfTempSensingRequest)
                tempDate = getNextDate(tempDate, getDaysInterval(tempSensingRequest.frequency))
            }
        }

    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun provideHighestPrioritySensingRequest(
        sensingRequests: MutableList<SensingRequestModel>
    ): SensingRequestModel {
        val tempSensingRequest =
            sensingRequests.maxWithOrNull(Comparator.comparingInt { it.priority })
        sensingRequests.remove(tempSensingRequest)
        requireNotNull(tempSensingRequest) { "tempSensingRequest is null" }
        return tempSensingRequest
    }

    private fun canWeAllocateSensingRequestForDate(
        examinationPlanDay: ExaminationPlanModel,
        tempDate: Date,
        maxNumberOfQuestionsDaily: Int
    ): Boolean =
        examinationPlanDay.singleDateOfExaminationPlan >= tempDate &&
                examinationPlanDay.allocatedSensingRequests.size < maxNumberOfQuestionsDaily

    private fun canWeAllocateSensingRequestForSpecificDay(
        examinationPlanDay: ExaminationPlanModel,
        tempDate: Date,
        maxNumberOfQuestionsDaily: Int,
        desiredDayOfTheWeek: String
    ): Boolean = examinationPlanDay.singleDateOfExaminationPlan >= tempDate &&
            examinationPlanDay.singleDateOfExaminationPlan.day == getDayNumber(desiredDayOfTheWeek) &&
            examinationPlanDay.allocatedSensingRequests.size < maxNumberOfQuestionsDaily

    private fun getDaysInterval(interval: String): Long {
        return when (interval) {
            EVERY_DAY -> 1
            ONCE_A_WEEK -> 7
            ONCE_A_MONTH -> 28
            else -> 0
        }
    }

    private fun getDayNumber(dayName: String): Int {
        return when (dayName) {
            MONDAY -> 1
            TUESDAY -> 2
            WEDNESDAY -> 3
            THURSDAY -> 4
            FRIDAY -> 5
            else -> 0
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getBeginingOfTimeSlot(timeSlotName: String): LocalTime {
        return when (timeSlotName) {
            MORNING -> LocalTime.of(9, 0)
            EARLY_MORNING -> LocalTime.of(6, 0)
            NOON -> LocalTime.of(12, 0)
            AFTERNOON -> LocalTime.of(15, 0)
            EVENING -> LocalTime.of(18, 0)
            else -> LocalTime.of(24, 0)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getEndOfTimeSlot(timeSlotName: String): LocalTime {
        return when (timeSlotName) {
            MORNING -> LocalTime.of(12, 0)
            EARLY_MORNING -> LocalTime.of(9, 0)
            NOON -> LocalTime.of(15, 0)
            AFTERNOON -> LocalTime.of(18, 0)
            EVENING -> LocalTime.of(21, 0)
            else -> LocalTime.of(24, 0)
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun calculateDaysBetweenDates(firstDate: Date, secondDate: Date): Long {
        val formatter: DateTimeFormatter =
            DateTimeFormatter.ofPattern(DateManager.DATE_AND_TIME_FORMAT_PATTERN)
        val dt1: LocalDateTime =
            LocalDate.parse(dateManager.convertDateToString(firstDate), formatter).atStartOfDay()
        val dt2: LocalDateTime =
            LocalDate.parse(dateManager.convertDateToString(secondDate), formatter).atStartOfDay()
        return Duration.between(dt1, dt2).toDays()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getNextDate(tempDate: Date, plusDates: Long): Date {
        val formatter: DateTimeFormatter =
            DateTimeFormatter.ofPattern(DateManager.DATE_AND_TIME_FORMAT_PATTERN)
        val dt: LocalDateTime =
            LocalDate.parse(dateManager.convertDateToString(tempDate), formatter).atStartOfDay()
        val ldt = LocalDateTime.from(dt).plusDays(plusDates)
        return Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant())
    }

    private fun determineConsistencyBetweenSensingRequestAndUser(
        sensingRequests: MutableList<SensingRequestModel>,
        userPreferredDays: MutableList<Int>,
        userPreferredTimeSlots: List<String>
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
            if (tempPreferowanySlot!!.name !in userPreferredTimeSlots) {
                sensing_request.desired_time_of_the_day = null
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun allocateDatesForExamination(
        startDate: Date,
        userPreferredDays: MutableList<Int>,
        examinationPlan: MutableList<ExaminationPlanModel>,
        endDate: Date
    ) {
        //wypelnienie listy datami na zadawanie pytan
        var tempDate = startDate
        while (endDate.after(tempDate)) {
            if (tempDate.day in userPreferredDays) {
                examinationPlan.add(ExaminationPlanModel(tempDate, mutableListOf()))
            }
            tempDate = getNextDate(tempDate, 1)
        }
    }

    @SuppressLint("LongLogTag")
    @RequiresApi(Build.VERSION_CODES.O)
    fun allocateSensingRequests(
        userPreferences: UserPreferencesModel,
        sensingRequests: MutableList<SensingRequestModel>
    ): MutableList<ExaminationPlanModel> {
        val dateFormat = dateManager.getSimpleDateOnlyFormat()
        //variables initialization
        val copyOfSensingRequests = sensingRequests.toMutableList()
        val startDate = dateFormat.parse(userPreferences.start_date)
        val endDate = dateFormat.parse(userPreferences.end_date)
        val tempUserPreferredDays = userPreferences.preferred_days
        val userPreferredDays: MutableList<Int> = mutableListOf()
        val userPreferredTimeSlots = userPreferences.preferred_times
        val maxNumberOfQuestionsDaily = userPreferences.max_number_per_day
        val examinationPlan: MutableList<ExaminationPlanModel> = mutableListOf()
        val examinationLength = calculateDaysBetweenDates(startDate, endDate)
        //preferowane dni przez uzytkownika - postac numeryczna
        tempUserPreferredDays.forEach { userPreferredDay ->
            //in java.util Monday = 1 and then ... Sunday = 7
            userPreferredDays.add(getDayNumber(userPreferredDay))
        }
        allocateDatesForExamination(
            startDate = startDate,
            userPreferredDays = userPreferredDays,
            examinationPlan = examinationPlan,
            endDate = endDate
        )

        determineConsistencyBetweenSensingRequestAndUser(
            sensingRequests = copyOfSensingRequests,
            userPreferredDays = userPreferredDays,
            userPreferredTimeSlots = userPreferredTimeSlots
        )

        planQuestionForDates(
            sensingRequests = copyOfSensingRequests,
            examinationPlan = examinationPlan,
            maxNumberOfQuestionsDaily = maxNumberOfQuestionsDaily,
            startDate = startDate
        )

        val liczbaWystapienPytan: MutableList<SensingRequestTimeOfOccurence> = mutableListOf()
        calculateOccurenceNumberSensingRequests(
            sensingRequests = sensingRequests,
            liczbaWystapienPytan = liczbaWystapienPytan,
            examinationPlan = examinationPlan
        )

        calculateExpectedAndDifferenceOccurence(
            liczbaWystapienPytan = liczbaWystapienPytan,
            examinationLength = examinationLength
        )

        //planowanie dokladnych godzin dla pytania
        examinationPlan.forEach { singleDayOfExaminationPlan ->
            val numberOfQuestionsPerSlotPerDay: MutableList<TimeSlotManager> = mutableListOf()
            Log.d("SIZE",singleDayOfExaminationPlan.allocatedSensingRequests.toString())
            userPreferredTimeSlots.forEach { timeSlot ->
                numberOfQuestionsPerSlotPerDay.add(TimeSlotManager(timeSlot, 0, mutableListOf()))
            }
            singleDayOfExaminationPlan.allocatedSensingRequests.forEach { sensingRequest ->
                numberOfQuestionsPerSlotPerDay.filter { it.timeSlot == sensingRequest.desired_time_of_the_day?.value }
                    .forEach { timeSlotManager ->
                        timeSlotManager.numberOfQuestionsPerSlot += 1
                    }
            }
            //alokacja pytan dla ktorych nie ma zgodnosci co do slotu czasowego - patrzymy ile jest pytan z null jako przydzielonym time slotem
            singleDayOfExaminationPlan.allocatedSensingRequests.filter { sensingRequest ->
                sensingRequest.desired_time_of_the_day == null
            }.forEach { sensingRequest ->
                val tempTimeSlot = provideMinOverWeightedSlot(numberOfQuestionsPerSlotPerDay)
                sensingRequest.desired_time_of_the_day = tempTimeSlot
                //inkrementacja liczby pytan na dany slot
                tempTimeSlot?.let { tempTimeSlot ->
                    incrementeSensingRequestsPerSlot(
                        numberOfQuestionsPerSlotPerDay = numberOfQuestionsPerSlotPerDay,
                        timeSlot = tempTimeSlot.value
                    )
                }
            }

            //planowanie konkretnych godzin wyswietlenia pytania
            numberOfQuestionsPerSlotPerDay.forEach { timeSlotManager ->
                val timeDifference = 3 * 60//todo later adjust dinamically
                val timeInterval =
                    timeDifference.toDouble() / (timeSlotManager.numberOfQuestionsPerSlot + 1)
                var i = 0
                while (i < timeSlotManager.numberOfQuestionsPerSlot) {
                    val a = calculateTimeForQuestion(
                        timeSlotManager = timeSlotManager,
                        timeInterval = timeInterval,
                        numberOfQuestionForDay = i
                    )
                    Log.d("AAA", a.toString())
                    timeSlotManager.hoursToAllocate.add(TimingOccupancy(a, false))
                    i++
                }
            }
            allocateTimesToSensingRequests(
                singleDayOfExaminationPlan = singleDayOfExaminationPlan,
                numberOfQuestionsPerSlotPerDay = numberOfQuestionsPerSlotPerDay
            )
            Log.d("DAY", singleDayOfExaminationPlan.toString())
            examinationPlan.forEach { singleDayOfExaminationPlan ->
                Log.d("DAY2", singleDayOfExaminationPlan.toString())
            }
            numberOfQuestionsPerSlotPerDay.clear()
        }

        Log.d("PLAN", examinationPlan.toString())
        return examinationPlan

        //todo petla zapobiegajaca zagladzaniu pytan o zerowej liczbie wystapien
        //todo petla zwiekszajaca priorytet zadko zadanych pytan arbitrarnie 10 (bo trzeba cos ustalic)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun calculateTimeForQuestion(
        timeSlotManager: TimeSlotManager,
        timeInterval: Double,
        numberOfQuestionForDay: Int
    ) = getBeginingOfTimeSlot(timeSlotManager.timeSlot).plusMinutes(
        (timeInterval * (numberOfQuestionForDay + 1)).toLong()
    )

    private fun allocateTimesToSensingRequests(
        singleDayOfExaminationPlan: ExaminationPlanModel,
        numberOfQuestionsPerSlotPerDay: MutableList<TimeSlotManager>
    ) {
        //przypisanie konkretnych godzin do senisng requests
        /*singleDayOfExaminationPlan.allocatedSensingRequests.forEach { sensingRequest ->
            val timeSlotManager =
                numberOfQuestionsPerSlotPerDay.first { it.timeSlot == sensingRequest.desired_time_of_the_day?.value }
            sensingRequest.time =
                timeSlotManager.hoursToAllocate.first { !it.ifOccupated }.time
            timeSlotManager.hoursToAllocate.first { !it.ifOccupated }.ifOccupated = true
        }*/
        for (sensingRequest in singleDayOfExaminationPlan.allocatedSensingRequests){
            for (timeSlot in numberOfQuestionsPerSlotPerDay){
                if(sensingRequest.desired_time_of_the_day?.value == timeSlot.timeSlot){
                    sensingRequest.time = timeSlot.hoursToAllocate.first { !it.ifOccupated }.time
                    timeSlot.hoursToAllocate.first { !it.ifOccupated }.ifOccupated = true
                }
            }
        }
    }

    private fun calculateExpectedAndDifferenceOccurence(
        liczbaWystapienPytan: MutableList<SensingRequestTimeOfOccurence>,
        examinationLength: Long
    ) {
        liczbaWystapienPytan.forEach {
            it.expectedNumberOfOccurence =
                (examinationLength / getDaysInterval(it.sensingRequest.frequency)).toInt()
            it.differenceBetweenExpectedAndRealNumberOfOccurence =
                it.expectedNumberOfOccurence - it.numberOfOccurence
        }
    }

    private fun calculateOccurenceNumberSensingRequests(
        sensingRequests: MutableList<SensingRequestModel>,
        liczbaWystapienPytan: MutableList<SensingRequestTimeOfOccurence>,
        examinationPlan: MutableList<ExaminationPlanModel>
    ) {
        sensingRequests.forEach { sensingRequest ->
            liczbaWystapienPytan.add(SensingRequestTimeOfOccurence(sensingRequest, 0, 0, 0))
            examinationPlan.forEach { examinationDay ->
                val numberOfOccurence =
                    examinationDay.allocatedSensingRequests.filter { it.content == sensingRequest.content }.size
                liczbaWystapienPytan[liczbaWystapienPytan.size - 1].numberOfOccurence += numberOfOccurence
            }
        }
    }

    private fun incrementeSensingRequestsPerSlot(
        numberOfQuestionsPerSlotPerDay: MutableList<TimeSlotManager>,
        timeSlot: String
    ) {
        numberOfQuestionsPerSlotPerDay.filter { it.timeSlot == timeSlot }
            .forEach { timeSlotManager ->
                timeSlotManager.numberOfQuestionsPerSlot += 1
            }
    }

    private fun provideMinOverWeightedSlot(numberOfQuestionsPerSlotPerDay: MutableList<TimeSlotManager>) =
        numberOfQuestionsPerSlotPerDay.minByOrNull { it.numberOfQuestionsPerSlot }?.timeSlot?.let {
            TimeOfTheDay.valueOf(it)
        }

}

data class SensingRequestTimeOfOccurence(
    val sensingRequest: SensingRequestModel,
    var numberOfOccurence: Int,
    var expectedNumberOfOccurence: Int,
    var differenceBetweenExpectedAndRealNumberOfOccurence: Int
)

data class TimingOccupancy(
    val time: LocalTime,
    var ifOccupated: Boolean
)

data class TimeSlotManager(
    val timeSlot: String,
    var numberOfQuestionsPerSlot: Int = 0,
    val hoursToAllocate: MutableList<TimingOccupancy> = mutableListOf()
)