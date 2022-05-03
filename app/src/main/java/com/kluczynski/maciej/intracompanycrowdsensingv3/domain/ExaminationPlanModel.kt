package com.kluczynski.maciej.intracompanycrowdsensingv3.domain

import com.kluczynski.maciej.intracompanycrowdsensingv3.data.SensingRequestModel
import java.time.LocalDate
import java.util.*

data class ExaminationPlanModel(
    val singleDateOfExaminationPlan: LocalDate,
    val allocatedSensingRequests: MutableList<SensingRequestModel>
)
