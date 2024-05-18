package bait2073.mad.assignments.ui.fitness

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class FitnessViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "Complete Each Workout"
    }
    val text: LiveData<String> = _text
}