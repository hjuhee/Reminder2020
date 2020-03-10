package fi.oulu.reminder2020

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Message
import android.view.View
import android.widget.AdapterView
import androidx.core.app.NotificationCompat
import androidx.room.Room
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.toast
import org.jetbrains.anko.uiThread
import kotlin.random.Random

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        var fabOpened = false

        fab.setOnClickListener {

            if(!fabOpened){

                fabOpened = true
                fab_map.animate().translationY(-resources.getDimension(R.dimen.standard_66))
                fab_time.animate().translationY(-resources.getDimension(R.dimen.standard_116))

            }else{

                fabOpened = false
                fab_map.animate().translationY(0f)
                fab_time.animate().translationY(0f)


            }
        }

        fab_time.setOnClickListener {
            //toast("Mobile Computing")
            val intent =Intent(applicationContext, TimeActivity::class.java)
            startActivity(intent)
        }

        fab_map.setOnClickListener{

            val intent = Intent(applicationContext, MapActivity::class.java)
            startActivity(intent)
        }

        list.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            val selected = list.adapter.getItem(position) as Reminder

            val builder = AlertDialog.Builder(this@MainActivity)
            builder.setTitle("Delete reminder?")
                .setMessage(selected.message)
                .setPositiveButton("Delete"){_, _ ->

                    if(selected.time!=null){
                        val manager=getSystemService(Context.ALARM_SERVICE) as AlarmManager
                        val intent=Intent(this@MainActivity, ReminderReceiver::class.java)
                        val pending =PendingIntent.getBroadcast(this@MainActivity, selected.uid!!, intent, PendingIntent.FLAG_ONE_SHOT)
                        manager.cancel(pending)
                    }

                    doAsync {
                        val db=Room.databaseBuilder(applicationContext, AppDatabase::class.java, "reminder").build()
                        db.reminderDao().delete(selected.uid!!)
                        db.close()

                        refreshList()
                    }
                }
                .setNegativeButton("Cancel"){dialog, _->
                    dialog.dismiss()
                }
                .show()
        }
        doAsync {
            val db=Room.databaseBuilder(applicationContext, AppDatabase::class.java, "reminders").build()
            val reminders=db.reminderDao().getReminders()
            db.close()

            uiThread {
                if(reminders.isNotEmpty()){
                    val adapter=ReminderAdapter(applicationContext, reminders)
                    list.adapter = adapter
                }else{
                    toast("No reminders yet")
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        refreshList()
    }

    // Retrieve the latest actual list of Reminders and refresh UI
    private fun refreshList(){
        doAsync {

            val db = Room.databaseBuilder(applicationContext, AppDatabase::class.java, "reminders").build()
            val reminders = db.reminderDao().getReminders()
            db.close()

            uiThread {

                if(reminders.isNotEmpty()){
                    val adapter = ReminderAdapter(applicationContext, reminders)
                    list.adapter = adapter
                } else{
                    toast("No reminders yet")
                }
            }
        }
    }

    companion object{
        val CHANNEL_ID="REMINDER_CHANNEL_1D"
        var NotificationID=1567
        fun  showNotification(context: Context, message: String){
            var notificationBuilder=NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_alarm_24px)
                .setContentTitle("Reminder").setContentText(message)
                .setStyle(NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)

            var notificationManager=context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    context.getString(R.string.app_name),
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply { description=context.getString(R.string.app_name) }

                notificationManager.createNotificationChannel(channel)
            }

            val notificateion= NotificationID+ Random(NotificationID).nextInt(1, 30)
            notificationManager.notify(notificateion, notificationBuilder.build())
        }
    }
}
//LAB07
