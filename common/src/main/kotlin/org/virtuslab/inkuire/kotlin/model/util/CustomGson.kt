package org.virtuslab.inkuire.kotlin.model.util

import com.google.gson.Gson
import org.virtuslab.inkuire.kotlin.model.SBound
import org.virtuslab.inkuire.kotlin.model.SProjection
import org.virtuslab.inkuire.kotlin.model.SVariance

object CustomGson {

    val instance by lazy {
        Gson().newBuilder()
            .registerTypeAdapter(SBound::class.java, BoundSerializer())
            .registerTypeAdapter(SProjection::class.java, ProjectionSerializer())
            .registerTypeAdapter(SVariance::class.java, VarianceSerializer())
            .create()
    }
}
