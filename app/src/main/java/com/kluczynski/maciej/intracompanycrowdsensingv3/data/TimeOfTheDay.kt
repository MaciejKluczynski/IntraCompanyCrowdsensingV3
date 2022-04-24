package com.kluczynski.maciej.intracompanycrowdsensingv3.data

import com.kluczynski.maciej.intracompanycrowdsensingv3.domain.SensingRequestsAllocationAlgorithm

enum class TimeOfTheDay(val value:String){
    NONE(SensingRequestsAllocationAlgorithm.NONE),
    EARLY_MORNING(SensingRequestsAllocationAlgorithm.EARLY_MORNING),//6-9
    MORNING(SensingRequestsAllocationAlgorithm.MORNING),//9-12
    NOON(SensingRequestsAllocationAlgorithm.NOON),//12-15
    AFTERNOON(SensingRequestsAllocationAlgorithm.AFTERNOON),//15-18
    EVENING(SensingRequestsAllocationAlgorithm.EVENING)//18-21
}