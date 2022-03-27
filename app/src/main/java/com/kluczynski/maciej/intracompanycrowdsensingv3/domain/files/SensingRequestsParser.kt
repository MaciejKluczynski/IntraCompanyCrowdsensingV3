package com.kluczynski.maciej.intracompanycrowdsensingv3.domain.files

import android.annotation.SuppressLint
import android.net.Uri
import com.google.gson.Gson
import com.kluczynski.maciej.intracompanycrowdsensingv3.data.SensingRequestModel
import com.kluczynski.maciej.intracompanycrowdsensingv3.domain.DateManager
import java.util.*
import kotlin.collections.ArrayList

class SensingRequestsParser(private var sensingRequestsResultFilePathProvider: SensingRequestsResultFilePathProvider,
                            var fileManager: FileManager,
                            var dateManager: DateManager
) {

    private fun getSensingRequestsListAsString(): String? {
        //wczytanie danych z pliku txt do zmiennej
        val uri: Uri = Uri.parse(sensingRequestsResultFilePathProvider.getFilePathFromSharedPrefs())
        return fileManager.readFileContent(uri)
    }

    @SuppressLint("SimpleDateFormat")
    fun findAskedSensingRequest(): SensingRequestModel {
        val sensingRequestsAsString = getSensingRequestsListAsString()
        //parsowanie tekstu na obiekty
        val gson = Gson()
        val sensingRequestList: List<SensingRequestModel> =
                gson.fromJson(sensingRequestsAsString, Array<SensingRequestModel>::class.java).toList()
        val times = ArrayList<Long>()

        for (i in sensingRequestList) {
            times.add(dateManager.convertDateToMs(i.time))
        }
        val differences = ArrayList<Long>()
        for (i in times) {
            if (dateManager.getCurrentTimeMs() - i < 0){
                differences.add(Long.MAX_VALUE)
            }
            else differences.add(dateManager.getCurrentTimeMs() - i)
        }

        val min = differences.minOrNull()
        val index = differences.indexOf(min)
        return sensingRequestList[index]
    }
}