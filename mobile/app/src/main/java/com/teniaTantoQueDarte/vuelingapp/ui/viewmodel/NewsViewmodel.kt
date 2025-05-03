package com.teniaTantoQueDarte.vuelingapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.teniaTantoQueDarte.vuelingapp.model.NewModel
import com.teniaTantoQueDarte.vuelingapp.ui.screen.getSampleNews
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class NewsViewModel : ViewModel() {
    private val _news = MutableStateFlow<List<NewModel>>(getSampleNews())
    val news: StateFlow<List<NewModel>> = _news

    // Aquí se cargarían las noticias desde una API o base de datos
    fun loadNews() {
        // Por ahora usamos datos de ejemplo
        _news.value = getSampleNews()
    }
}