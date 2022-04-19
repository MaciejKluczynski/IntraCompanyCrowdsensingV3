package com.kluczynski.maciej.intracompanycrowdsensingv3.data

import java.util.*

data class UserPreferencesModel(
        val ids:List<String>,
        val preferred_times:List<String>,
        val preferred_days:List<String>,
        val max_number_per_day:Int,
        val start_date:String,
        val end_date:String
)