package com.example.sr13.Patient

/**
 * Klasa reprezentująca lekarza.
 *
 * @property name Imię pacjenta.
 * @property lastname Nazwisko pacjenta.
 * @property age Wiek pacjenta.
 * @property imageResId Identyfikator zasobu obrazu pacjenta.
 */
data class Patient(val name: String, val lastname: String, val age: Int, val imageResId: Int)
