package com.kluczynski.maciej.intracompanycrowdsensingv3.domain

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.kluczynski.maciej.intracompanycrowdsensingv3.data.SensingRequestModel
import com.kluczynski.maciej.intracompanycrowdsensingv3.data.UserPreferencesModel
import java.time.Duration
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

        while (sensingRequests.isNotEmpty()) {
            //bierzemy z listy element o najwyzszym priorytecie zawsze
            val tempSensingRequest = sensingRequests.maxWithOrNull(Comparator.comparingInt {it.priority })
            sensingRequests.remove(tempSensingRequest)
            requireNotNull(tempSensingRequest){"tempSensingRequest is null"}
            //warunek gdy nie ma zgodnosci z uzytkownikiem - alokacja w dolonej dacie
            if (tempSensingRequest.desired_day_of_the_week == null) {
                var tempDate = startDate
                for (i in examinationPlan) {
                    if (i.singleDateOfExaminationPlan >= tempDate && i.allocatedSensingRequests.size < maxNumberOfQuestionsDaily) {
                        i.allocatedSensingRequests.add(tempSensingRequest)
                        tempDate = getNextDate(tempDate, getDaysInterval(tempSensingRequest.frequency))
                    }
                }
            }
            //warunek gdy jest zgodnosc z uzytkownikiem - alokacja w zgodnym dniu tygodnia
            else {
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
    private fun calculateDaysBetweenDates(firstDate: Date, secondDate: Date):Long{
        val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd-M-yyyy HH:mm:ss")
        val dt1: LocalDateTime = LocalDate.parse(DateManager().convertDateToString(firstDate), formatter).atStartOfDay()
        val dt2: LocalDateTime = LocalDate.parse(DateManager().convertDateToString(secondDate), formatter).atStartOfDay()
        return Duration.between(dt1, dt2).toDays()
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
        val copyOfSensingRequests = sensingRequests.toMutableList()
        val startDate = dateFormat.parse(userPreferences.start_date)
        val endDate = dateFormat.parse(userPreferences.end_date)
        val tempUserPreferredDays = userPreferences.preferred_days
        val userPreferredDays: MutableList<Int> = mutableListOf()
        val userPreferredTimeSlots = userPreferences.preferred_times
        val maxNumberOfQuestionsDaily = userPreferences.max_number_per_day
        val examinationPlan: MutableList<ExaminationPlanModel> = mutableListOf()
        val examinationLength = calculateDaysBetweenDates(startDate,endDate)
        //preferowane dni przez uzytkownika - postac numeryczna
        for (i in tempUserPreferredDays) {
            //in java.util Monday = 1 and then ... Sunday = 7
            userPreferredDays.add(getDayNumber(i))
        }

        allocateDatesForExamination(startDate, userPreferredDays, examinationPlan, endDate)
        determineConsistencyBetweenSensingRequestAndUser(copyOfSensingRequests, userPreferredDays,userPreferredTimeSlots)

        planQuestionForDates(copyOfSensingRequests, examinationPlan, maxNumberOfQuestionsDaily,startDate)
        Log.d("TEST",examinationPlan.toString())
        Log.d("TEST",sensingRequests.toString())
        val liczbaWystapienPytan:MutableList<SensingRequestTimeOfOccurence> = mutableListOf()
        sensingRequests.forEach{ sensingRequest ->
            liczbaWystapienPytan.add(SensingRequestTimeOfOccurence(sensingRequest,0,0,0))
            examinationPlan.forEach { examinationDay ->
               val numberOfOccurence = examinationDay.allocatedSensingRequests.filter { it.content == sensingRequest.content }.size
                liczbaWystapienPytan[liczbaWystapienPytan.size].numberOfOccurence += numberOfOccurence
            }
        }
        liczbaWystapienPytan.forEach{
            it.expectedNumberOfOccurence = (examinationLength/getDaysInterval(it.sensingRequest.frequency)).toInt()
            it.differenceBetweenExpectedAndRealNumberOfOccurence = it.expectedNumberOfOccurence - it.numberOfOccurence
        }
        //todo petla zapobiegajaca zagladzaniu pytan o zerowej liczbie wystapien

        //todo petla zwiekszajaca priorytet zadko zadanych pytan arbitrarnie 10 (bo trzeba cos ustalic)

        //planowanie dokladnych godzin dla pytania z rozkladem losowym

        examinationPlan.forEach {
            val numberOfQuestionsPerSlotPerDay:MutableList<TimeSlotManager>

        }
        /*for i in plan_badania{

            liczba_pytan_na_slot_na_dany_dzien <- List<slot_czasowy:String,ilosc_pytan_na_slot:Int,godziny <- List<czas_pytania,czy_zajety>>
            //bierzemy sloty czasowe w ktorych mozemy dac pytania
            for j in tablica_preferowanych_slotow_czasowych{
                liczba_pytan_na_slot_na_dany_dzien.add(j,0)
            }
            //alokacja pytan dla zgodnych terminow - patrzymy ile na dany dzien jest sensing requests przydzielonych na dany slot czasowy
            for sensingRequest in i.sensing_requests{
                for j in liczba_pytan_na_slot_na_dany_dzien{
                    jesli(sensingRequest.time_slot == i.slot_czasowy){
                        i.ilosc_pytan_na_slot++,
                        break,
                    }
                }
            }
            //alokacja pytan dla ktorych nie ma zgodnosci co do slotu czasowego - patrzymy ile jest pytan z null jako przydzielonym time slotem
            for sensingRequest in i.sensing_requests{
                for j in liczba_pytan_na_slot_na_dany_dzien{
                    jesli(sensingRequest.time_slot == null){
                        tempSlot = liczba_pytan_na_slot_na_dany_dzien.where_ilosc_pytan_na_slot_min()
                        //przydzielamy pytaniu najmniej obciazony time_slot
                        sensingRequest.time_slot = tempSlot
                        liczba_pytan_na_slot_na_dany_dzien.where_slot_czasowy(tempSlot).ilosc_pytan++
                        break
                    }
                }

            }
            //planowanie konkretnych godzin wyswietlenia pytania
            for j in liczba_pytan_na_slot_na_dany_dzien{
                roznica_godzin = j.slot_czasowy.wez_godzine_konca - j.slot_czasowy.wez_godzine_poczatku
                interwal = roznica_godzin/(j.ilosc_pytan_na_slot+1)
                i = 0
                while(i<j.ilosc_pytan_na_slot){
                    j.godziny.add(j.slot_czasowy.wez_godzine_poczatku+(i+1)*interwal,false)
                }
            }
            //przypisanie konkretnych godzin do senisng requests
            for j in i.tresci_pytan_na_dany_dzien_ze_slotami{
                j.time = wez_pierwsza_niewzieta_godzine_ze_slotu(liczba_pytan_na_slot_na_dany_dzien,j.time_slot)
            }
        }*/


    }


}

data class SensingRequestTimeOfOccurence(
    val sensingRequest:SensingRequestModel,
    var numberOfOccurence:Int,
    var expectedNumberOfOccurence:Int,
    var differenceBetweenExpectedAndRealNumberOfOccurence:Int
    )

data class TimingOccupancy(
    val time:String,
    val ifOccupated:Boolean
)

data class TimeSlotManager(
    val timeSlot:String,
    val numberOfQuestionsPerSlot:Int,
    val hoursToAllocate:MutableList<TimingOccupancy>
)