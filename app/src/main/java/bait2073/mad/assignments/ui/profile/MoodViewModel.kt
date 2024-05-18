package bait2073.mad.assignments.ui.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MoodViewModel : ViewModel() {
    private val _selectedMoodLevel = MutableLiveData<Int>()
    val selectedMoodLevel: LiveData<Int> = _selectedMoodLevel

    fun setSelectedMoodLevel(moodLevel: Int) {
        _selectedMoodLevel.value = moodLevel
    }
}
