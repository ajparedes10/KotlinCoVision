package com.covision.covisionapp.structures

import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL


class DownloadUrl {

    @Throws(IOException::class)
    fun readUrl(myUrl: String): String {
        var data = ""
        var inputStream: InputStream? = null
        var urlConnection: HttpURLConnection? = null

        try {
            val url = URL(myUrl)
            urlConnection = url.openConnection() as HttpURLConnection
            urlConnection.connect()

            inputStream = urlConnection.inputStream
            val br = BufferedReader(InputStreamReader(inputStream))
            val sb = StringBuffer()

            var line = br!!.readLine()
            while ((line) != null){
                if(line!=null)sb!!.append(line)

                line = br!!.readLine()

            }


            data = sb!!.toString()
            br!!.close()

        } catch (e: MalformedURLException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            inputStream!!.close()
            urlConnection!!.disconnect()
        }

        return data
    }
}