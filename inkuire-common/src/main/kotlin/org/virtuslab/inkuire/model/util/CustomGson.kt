package org.virtuslab.inkuire.model.util

import com.google.gson.Gson
import org.virtuslab.inkuire.model.*

object CustomGson {

    val instance by lazy {
        Gson().newBuilder()
            .registerTypeAdapter(SBound::class.java, BoundSerializer())
            .registerTypeAdapter(SProjection::class.java, ProjectionSerializer())
            .registerTypeAdapter(SVariance::class.java, VarianceSerializer())
            .create()
    }
}