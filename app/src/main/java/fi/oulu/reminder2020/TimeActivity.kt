package fi.oulu.reminder2020

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.room.Room
import kotlinx.android.synthetic.main.activity_time.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.toast
import java.util.*

class TimeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_time)

        time_create.setOnClickListener{


            val calendar = GregorianCalendar(
                datePicker.year,
                datePicker.month,
                datePicker.dayOfMonth,
                timePicker.currentHour,
                timePicker.currentMinute
            )

            Log.d("LAB7", "picked year is "+datePicker.year)
            Log.d("LAB7", "picked month is "+datePicker.month)

            if((et_message.text.toString() != "") &&
                (calendar.timeInMillis > System.currentTimeMillis())){

                val reminder = Reminder(
                    uid = null,
                    time = calendar.timeInMillis,
                    location = null,
                    message = et_message.text.toString()
                )

                doAsync {

                    val db = Room.databaseBuilder(
                        applicationContext,
                        AppDatabase::class.java,
                        "reminders"
                    ).build()
                    val uid=db.reminderDao().insert(reminder).toInt()

                    db.close()

                    setAlarm(
                        uid,
                        calendar.timeInMillis,
                        reminder.message
                    )

                    finish()
                }
            } else{
                toast("Wrong data")
            }


        }
    }

    private fun setAlarm(uid: Int, time: Long, message: String){

        val intent = Intent(this, ReminderReceiver::class.java)
        intent.putExtra("uid", uid)
        intent.putExtra("message", message)

        val pending = PendingIntent.getBroadcast(this, uid, intent, PendingIntent.FLAG_ONE_SHOT)

        val manager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        manager.setExact(AlarmManager.RTC, time, pending)

        runOnUiThread { toast("Reminder is created") }
    }
}
