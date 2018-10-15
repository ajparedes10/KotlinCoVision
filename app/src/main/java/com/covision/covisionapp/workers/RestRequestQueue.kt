package com.covision.covisionapp.workers

import android.content.Context

import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.Volley

class RestRequestQueue private constructor(context: Context) {

    private var queue: RequestQueue? = null

    // getApplicationContext() is key, it keeps you from leaking the
    // Activity or BroadcastReceiver if someone passes one in.
    val requestQueue: RequestQueue
        get() {
            if (queue == null) {
                queue = Volley.newRequestQueue(ctx.applicationContext)
            }
            return queue!!
        }

    init {
        ctx = context
        queue = requestQueue
    }

    fun <T> addToRequestQueue(req: Request<T>) {
        requestQueue.add(req)
    }

    companion object {
        private var instance: RestRequestQueue? = null
        private lateinit var ctx: Context

        @Synchronized
        fun getInstance(context: Context): RestRequestQueue {
            if (instance == null) {
                instance = RestRequestQueue(context)
            }
            return instance as RestRequestQueue
        }
    }
}
