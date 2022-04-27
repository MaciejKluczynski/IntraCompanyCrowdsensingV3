package com.kluczynski.maciej.intracompanycrowdsensingv3.domain

import com.kluczynski.maciej.intracompanycrowdsensingv3.data.SensingRequestModel
import java.util.*

data class ExaminationPlanModel(
    val singleDateOfExaminationPlan: Date,
    val allocatedSensingRequests: MutableList<SensingRequestModel>
)
