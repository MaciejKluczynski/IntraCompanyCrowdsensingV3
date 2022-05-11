package com.kluczynski.maciej.intracompanycrowdsensingv3.domain

import android.annotation.SuppressLint
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.kluczynski.maciej.intracompanycrowdsensingv3.data.SensingRequestModel
import com.kluczynski.maciej.intracompanycrowdsensingv3.data.TimeOfTheDay
import com.kluczynski.maciej.intracompanycrowdsensingv3.data.UserPreferencesModel
import java.time.*
import java.util.*
import java.time.temporal.ChronoUnit

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
        startDate: LocalDate
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
        startDate: LocalDate,
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
                val copyOfTempSensingRequest = performCopyOfSensingRequest(tempSensingRequest)
                dayOfExamination.allocatedSensingRequests.add(copyOfTempSensingRequest)
                tempDate = tempDate.plusDays(getDaysInterval(tempSensingRequest.frequency))
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun allocateSensingRequestInSpecificDay(
        startDate: LocalDate,
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
                val copyOfTempSensingRequest = performCopyOfSensingRequest(tempSensingRequest)
                dayOfExamination.allocatedSensingRequests.add(copyOfTempSensingRequest)
                tempDate = tempDate.plusDays(1)
            }
        }

    }

    private fun performCopyOfSensingRequest(tempSensingRequest: SensingRequestModel): SensingRequestModel {
        return SensingRequestModel(
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
        tempDate: LocalDate,
        maxNumberOfQuestionsDaily: Int
    ): Boolean =
        examinationPlanDay.singleDateOfExaminationPlan >= tempDate &&
                examinationPlanDay.allocatedSensingRequests.size < maxNumberOfQuestionsDaily

    @RequiresApi(Build.VERSION_CODES.O)
    private fun canWeAllocateSensingRequestForSpecificDay(
        examinationPlanDay: ExaminationPlanModel,
        tempDate: LocalDate,
        maxNumberOfQuestionsDaily: Int,
        desiredDayOfTheWeek: String
    ): Boolean = examinationPlanDay.singleDateOfExaminationPlan >= tempDate &&
            examinationPlanDay.singleDateOfExaminationPlan.dayOfWeek.value == getDayNumber(
        desiredDayOfTheWeek
    ) &&
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
        startDate: LocalDate,
        userPreferredDays: MutableList<Int>,
        examinationPlan: MutableList<ExaminationPlanModel>,
        endDate: LocalDate
    ) {
        //wypelnienie listy datami na zadawanie pytan
        var tempDate = startDate
        while (endDate.isAfter(tempDate)) {
            if (tempDate.dayOfWeek.value in userPreferredDays) {
                examinationPlan.add(ExaminationPlanModel(tempDate, mutableListOf()))
            }
            tempDate = tempDate.plusDays(1)
        }
    }

    @SuppressLint("LongLogTag")
    @RequiresApi(Build.VERSION_CODES.O)
    fun allocateSensingRequests(
        userPreferences: UserPreferencesModel,
        sensingRequests: MutableList<SensingRequestModel>
    ): MutableList<ExaminationPlanModel> {
        //variables initialization
        var copyOfSensingRequests = sensingRequests.toMutableList()
        val sensingRequestsIds = userPreferences.ids
        copyOfSensingRequests = copyOfSensingRequests.filter { sensingRequest ->
            sensingRequest.sensing_request_id in sensingRequestsIds
        } as MutableList<SensingRequestModel>
        val copyOfSensingRequests2 = copyOfSensingRequests.toMutableList()
        val startDate =
            LocalDate.parse(userPreferences.start_date, dateManager.getSimpleDateOnlyFormat())
        val endDate =
            LocalDate.parse(userPreferences.end_date, dateManager.getSimpleDateOnlyFormat())
        val tempUserPreferredDays = userPreferences.preferred_days
        val userPreferredDays: MutableList<Int> = mutableListOf()
        val userPreferredTimeSlots = userPreferences.preferred_times
        val maxNumberOfQuestionsDaily = userPreferences.max_number_per_day
        val examinationPlan: MutableList<ExaminationPlanModel> = mutableListOf()
        val examinationLength = ChronoUnit.DAYS.between(startDate, endDate)
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
        doTheCalculationsOccurrenceSensingRequests(
            sensingRequests = copyOfSensingRequests2,
            liczbaWystapienPytan = liczbaWystapienPytan,
            examinationPlan = examinationPlan,
            examinationLength = examinationLength
        )

        //planowanie dokladnych godzin dla pytania
        examinationPlan.forEach { singleDayOfExaminationPlan ->
            val numberOfQuestionsPerSlotPerDay: MutableList<TimeSlotManager> = mutableListOf()
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
                val timeDifference = (getEndOfTimeSlot(timeSlotManager.timeSlot).hour - getBeginingOfTimeSlot(timeSlotManager.timeSlot).hour)* 60
                val timeInterval = timeDifference.toDouble() / (timeSlotManager.numberOfQuestionsPerSlot + 1)
                var i = 0
                while (i < timeSlotManager.numberOfQuestionsPerSlot) {

                    timeSlotManager.hoursToAllocate.add(
                        TimingOccupancy(
                            calculateTimeForQuestion(
                                timeSlotManager = timeSlotManager,
                                timeInterval = timeInterval,
                                numberOfQuestionForDay = i
                            ), false
                        )
                    )
                    i++
                }
            }
            allocateTimesToSensingRequests(
                singleDayOfExaminationPlan = singleDayOfExaminationPlan,
                numberOfQuestionsPerSlotPerDay = numberOfQuestionsPerSlotPerDay
            )
        }

        changeRandomDisplayTime(examinationPlan)
        //to juz dziala ;P
        insertQuestionWithZeroApperances(
            sensingRequests = copyOfSensingRequests2,
            examinationPlan = examinationPlan,
            maxNumberOfQuestionsDaily = maxNumberOfQuestionsDaily,
            liczbaWystapienPytan = liczbaWystapienPytan,
            examinationLength = examinationLength
        )
        //do poprawy
        increasePriorityOfRareQuestions(
            sensingRequests = copyOfSensingRequests2,
            examinationPlan = examinationPlan,
            liczbaWystapienPytan = liczbaWystapienPytan,
            examinationLength = examinationLength
        )

        Log.d("PLAN", examinationPlan.toString())
        return examinationPlan
    }

    private fun requirementsNotMet(liczbaWystapienPytan: MutableList<SensingRequestTimeOfOccurence>): Boolean {
        for (sensingRequest in liczbaWystapienPytan) {
            if (sensingRequest.differenceBetweenExpectedAndRealNumberOfOccurence < sensingRequest.expectedNumberOfOccurence) {
                return true
            }
        }
        return false
    }

    private fun increasePriorityOfRareQuestions(
        liczbaWystapienPytan: MutableList<SensingRequestTimeOfOccurence>,
        sensingRequests: MutableList<SensingRequestModel>,
        examinationPlan: MutableList<ExaminationPlanModel>,
        examinationLength: Long
    ) {
        if (requirementsNotMet(liczbaWystapienPytan)) {
            //petla zwiekszajaca priorytet zadko zadanych pytan arbitrarnie 10 (bo trzeba cos ustalic)
            // 10 razy wstawiamy najbardziej niedocenione pytanie w miejsce najbardziej docenionego
            for (iterator in 0..9) {
                val mostOftenSensingRequest =
                    liczbaWystapienPytan.maxByOrNull { it.numberOfOccurence }
                val leastOftenSenisngRequest =
                    liczbaWystapienPytan.maxByOrNull { it.differenceBetweenExpectedAndRealNumberOfOccurence }
                if (mostOftenSensingRequest != null && leastOftenSenisngRequest != null) {
                    insertSensingRequestIntoAnother(
                        mostOftenSensingRequest.sensingRequest,
                        leastOftenSenisngRequest.sensingRequest
                    )
                }
                //wyczysc listy z liczba wystapien
                clearNumberLists(liczbaWystapienPytan)
                //przelicz jeszcze raz liczbe wystapien pytan
                doTheCalculationsOccurrenceSensingRequests(
                    sensingRequests = sensingRequests,
                    liczbaWystapienPytan = liczbaWystapienPytan,
                    examinationPlan = examinationPlan,
                    examinationLength = examinationLength
                )
            }
        }
    }

    private fun insertQuestionWithZeroApperances(
        sensingRequests: MutableList<SensingRequestModel>,
        examinationPlan: MutableList<ExaminationPlanModel>,
        maxNumberOfQuestionsDaily: Int,
        liczbaWystapienPytan: MutableList<SensingRequestTimeOfOccurence>,
        examinationLength: Long
    ) {
        if (sensingRequests.size < examinationPlan.size * maxNumberOfQuestionsDaily) {
            liczbaWystapienPytan.filter { sensingRequestTimeOfOccurence ->
                sensingRequestTimeOfOccurence.numberOfOccurence == 0
            }.forEach { sensingRequestTimeOfOccurence ->
                //znajdz pytanie zadane najwieksza liczbe razy
                val maxSensingRequest = liczbaWystapienPytan.maxByOrNull { it.numberOfOccurence }
                //znajdz pierwsze wystapienie tego pytania w planie badania
                if (maxSensingRequest != null) {
                    outer@ for (day in examinationPlan) {
                        for (sensingRequest in day.allocatedSensingRequests) {
                            if (sensingRequest.content == maxSensingRequest.sensingRequest.content) {
                                insertSensingRequestIntoAnother(
                                    sensingRequest,
                                    sensingRequestTimeOfOccurence.sensingRequest
                                )
                                break@outer
                            }
                        }
                    }
                    //wyczysc listy z liczba wystapien
                    clearNumberLists(liczbaWystapienPytan)
                    //przelicz jeszcze raz liczbe wystapien pytan
                    doTheCalculationsOccurrenceSensingRequests(
                        sensingRequests = sensingRequests,
                        liczbaWystapienPytan = liczbaWystapienPytan,
                        examinationPlan = examinationPlan,
                        examinationLength = examinationLength
                    )
                }
            }
        }
    }

    private fun insertSensingRequestIntoAnother(
        sensingRequestToChange: SensingRequestModel,
        sensingRequestToInsert: SensingRequestModel
    ) {
        sensingRequestToChange.why_ask = sensingRequestToInsert.why_ask
        sensingRequestToChange.frequency = sensingRequestToInsert.frequency
        sensingRequestToChange.priority = sensingRequestToInsert.priority
        sensingRequestToChange.questionType = sensingRequestToInsert.questionType
        sensingRequestToChange.content = sensingRequestToInsert.content
        sensingRequestToChange.hint = sensingRequestToInsert.hint
        sensingRequestToChange.sensing_request_id = sensingRequestToInsert.sensing_request_id
    }

    private fun clearNumberLists(liczbaWystapienPytan: MutableList<SensingRequestTimeOfOccurence>) {
        liczbaWystapienPytan.clear()
    }

    private fun doTheCalculationsOccurrenceSensingRequests(
        sensingRequests: MutableList<SensingRequestModel>,
        liczbaWystapienPytan: MutableList<SensingRequestTimeOfOccurence>,
        examinationPlan: MutableList<ExaminationPlanModel>,
        examinationLength: Long
    ) {
        calculateOccurenceNumberSensingRequests(
            sensingRequests = sensingRequests,
            liczbaWystapienPytan = liczbaWystapienPytan,
            examinationPlan = examinationPlan
        )

        calculateExpectedAndDifferenceOccurence(
            liczbaWystapienPytan = liczbaWystapienPytan,
            examinationLength = examinationLength
        )
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun changeRandomDisplayTime(examinationPlan: MutableList<ExaminationPlanModel>) {
        examinationPlan.forEach { singleDayOfExamination ->
            singleDayOfExamination.allocatedSensingRequests.forEach { sensingRequest ->
                sensingRequest.time = sensingRequest.time?.plusMinutes(getRandomTimeVarriancy())
                Log.d("TIME", sensingRequest.time.toString())
            }
        }
    }

    private fun getRandomTimeVarriancy(): Long {
        var randomNumber = Random().nextInt(20)
        val sign = Random().nextInt(1)
        if (sign == 0)
            randomNumber *= -1
        return randomNumber.toLong()
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
        singleDayOfExaminationPlan.allocatedSensingRequests.forEach { sensingRequest ->
            val timeSlotManager =
                numberOfQuestionsPerSlotPerDay.first { it.timeSlot == sensingRequest.desired_time_of_the_day?.value }
            sensingRequest.time =
                timeSlotManager.hoursToAllocate.first { !it.ifOccupated }.time
            timeSlotManager.hoursToAllocate.first { !it.ifOccupated }.ifOccupated = true
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