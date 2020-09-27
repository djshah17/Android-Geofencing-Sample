package com.example.androidgeofencingsample

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.text.TextUtils
import android.util.Log
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import androidx.core.app.NotificationCompat
import androidx.core.app.TaskStackBuilder
import kotlin.Exception


class MyGeofenceTransitionsIntentService : IntentService("GeofencingService") {

    override fun onHandleIntent(intent: Intent?) {
        try {
            val event: GeofencingEvent = GeofencingEvent.fromIntent(intent)

            if (event.hasError()) {
                val errorMessage = MyGeofenceErrorMessages.getErrorString(this, event.errorCode)
                Log.e("Geofencing Error: ",errorMessage)
                return
            }

            val transition = event.geofenceTransition

            if (transition == Geofence.GEOFENCE_TRANSITION_ENTER) {

                val geofences: List<Geofence> = event.triggeringGeofences

                val transitionDetails: String = geofenceTransitionDetails(transition, geofences)

                sendGeofencingNotification(transitionDetails)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun geofenceTransitionDetails(geofenceTransition: Int, triggerGeofences: List<Geofence> ): String {

        val geofenceTransitionString = transitionString(geofenceTransition)

        val triggerGeofencesIdsList = ArrayList<String>()

        for (geofence: Geofence in triggerGeofences) {
            triggerGeofencesIdsList.add(geofence.requestId)
        }

        val triggerGeofencesIdsString = TextUtils.join(", ", triggerGeofencesIdsList)
        return geofenceTransitionString + ": " + triggerGeofencesIdsString

    }

    private fun transitionString(transitionType: Int): String {
        return when (transitionType) {
            Geofence.GEOFENCE_TRANSITION_ENTER -> getString(R.string.geofence_transition_entered)
            Geofence.GEOFENCE_TRANSITION_EXIT -> getString(R.string.geofence_transition_exited)
            else -> getString(R.string.unknown_geofence_transition)
        }
    }

    fun sendGeofencingNotification(titleText: String) {
        try {

            val geoNotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager

            val geoChannelId = "AndroidGeofencingSample"

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val geoChannelName = getString(R.string.app_name)
                val geoChannel = NotificationChannel(geoChannelId, geoChannelName, NotificationManager.IMPORTANCE_HIGH)
                geoNotificationManager?.createNotificationChannel(geoChannel)
            }

            val geoNotificationIntent = Intent(this, MainActivity::class.java)
            val geoStackBuilder = TaskStackBuilder.create(this)
            geoStackBuilder.addParentStack(MainActivity::class.java)
            geoStackBuilder.addNextIntent(geoNotificationIntent)

            val geoNotificationPendingIntent = geoStackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)
            val geoDefaultSoundUri: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val geoNotificationBuilder = NotificationCompat.Builder(this)

            geoNotificationBuilder.setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher))
                .setColor(Color.RED)
                .setContentTitle(titleText)
                .setSound(geoDefaultSoundUri)
                .setContentIntent(geoNotificationPendingIntent)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                geoNotificationBuilder.setChannelId(geoChannelId)
            } else {
                geoNotificationBuilder.priority = Notification.PRIORITY_HIGH
            }

            geoNotificationBuilder.setAutoCancel(true)
            geoNotificationManager?.notify(0, geoNotificationBuilder.build())

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}