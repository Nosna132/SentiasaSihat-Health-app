package bait2073.mad.assignments.ui.fitness

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.util.Calendar

class NotificationViewModel : ViewModel() {
    private val _selectedTime = MutableLiveData<String>()
    val selectedTime: LiveData<String> = _selectedTime

    fun getCurrentTime(): String {
        val currentTime = Calendar.getInstance()
        val hour = currentTime.get(Calendar.HOUR_OF_DAY)
        val minute = currentTime.get(Calendar.MINUTE)
        val amPm = if (hour < 12) "AM" else "PM"
        val hourFormatted = if (hour == 0) 12 else if (hour > 12) hour - 12 else hour
        return String.format("%02d:%02d %s", hourFormatted, minute, amPm)
    }

    fun setSelectedTime(time: String) {
        _selectedTime.value = time
    }
}