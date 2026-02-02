package com.gideongeng.Vitalis.FireStore.Repository

import android.content.Context
import androidx.lifecycle.LiveData
import com.gideongeng.Vitalis.Utils.UIstate
import com.gideongeng.Vitalis.data_Model.Nutrient
import com.gideongeng.Vitalis.data_Model.weight
import com.github.mikephil.charting.data.Entry

interface Repository {
    fun getWeight(result: (UIstate<ArrayList<Entry>>) -> Unit)
    fun changedWeight(weight: weight, result: (UIstate<String>) -> Unit)
    fun addFood(nutrient: Nutrient, result: (UIstate<Nutrient>) -> Unit)
    fun getNutrients(mealTimes: List<String>): LiveData<ArrayList<Float>>
    fun getcalories(mealTimes: List<String>): LiveData<ArrayList<Float>>
    fun changeHeight(height: String,context:Context)
    fun getheight(result: (UIstate<String>) -> Unit)

}
