package com.example.sr13.firestore

import com.google.firebase.firestore.PropertyName

/**
 * Klasa Login reprezentuje dane logowania użytkownika.
 *
 * @property email Adres email użytkownika.
 * @property password Hasło użytkownika.
 * @property role Rola użytkownika (lekarz lub pacjent).
 *
 * @constructor Tworzy obiekt klasy Login z podanymi wartościami lub inicjuje
 * puste łańcuchy znaków (String) dla email i password oraz wartość "pacjent" dla roli.
 */
data class Login(
    @get:PropertyName("email") @set:PropertyName("email") var email: String = "",
    @get:PropertyName("password") @set:PropertyName("password") var password: String = "",
    @get:PropertyName("role") @set:PropertyName("role") var role: String = ""
)
