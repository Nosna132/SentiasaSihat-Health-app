package bait2073.mad.assignments.ui.fitness

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.util.Calendar

class NotificationViewModel : ViewModel() {
    private val _selectedTime = MutableLiveData<String>()
    val selectedTime: LiveData<String> = _selectedTime

    fun setSelectedTime(time: String) {
        _selectedTime.value = time
    }
}