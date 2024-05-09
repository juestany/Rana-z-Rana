package com.example.sr13.doctor

/**
 * Klasa reprezentująca lekarza.
 *
 * @property name Imię lekarza.
 * @property lastname Nazwisko lekarza.
 * @property title Tytuł lekarza.
 * @property imageResId Identyfikator zasobu obrazu lekarza.
 */
data class Doctor(val name: String, val lastname: String, val title: String, val imageResId: Int)
