package com.kluczynski.maciej.intracompanycrowdsensingv3.data

enum class TimeOfTheDay(val value:String){
    NONE("x"),
    EARLY_MORNING("EARLY_MOR"),//6-9
    MORNING("MOR"),//9-12
    NOON("NOON"),//12-15
    AFTERNOON("AFTERNOON"),//15-18
    EVENING("EVE")//18-21
}