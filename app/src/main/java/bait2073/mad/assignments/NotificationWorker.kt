package bait2073.mad.assignments

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.navigation.NavDeepLinkBuilder
import androidx.work.Worker
import androidx.work.WorkerParameters

class NotificationWorker(context: Context, params: WorkerParameters) : Worker(context, params) {

    override fun doWork(): Result {
        createNotificationChannel()
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val pendingIntent = NavDeepLinkBuilder(applicationContext)
            .setGraph(R.navigation.mobile_navigation)
            .setDestination(R.id.nav_fitnessmain)
            .createPendingIntent()

        val notification = NotificationCompat.Builder(applicationContext, "SentiasaSihat")
            .setContentTitle("SentiasaSihat Fitness Alarm")
            .setContentText("It's time to workout!")
            .setSmallIcon(R.drawable.appicon)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent) // Set the PendingIntent
            .build()

        notificationManager.notify(1001, notification)

        return Result.success()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "SentiasaSihat Fitness Notification"
            val description = "It's time to workout!"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel("SentiasaSihat", name, importance).apply {
                this.description = description
            }
            val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}