package com.example.sr13

/**
 * Interfejs LoginFirestoreInterface definiuje operacje do interakcji z bazą danych Firestore.
 * Wszystkie operacje są oznaczone jako 'suspend', co oznacza, że mogą być wywoływane
 * tylko w kontekście korutyny.
 */
interface FirestoreInterface {

    /**
     * Suspend function do dodawania nowego rekordu (logowania) do bazy danych Firestore.
     *
     * @param email Adres email użytkownika.
     * @param login Obiekt klasy Login, który ma zostać dodany do bazy danych.
     */
    suspend fun addLogin(email: String, login: Login)

    /**
     * Suspend function do pobierania danych logowania z bazy danych na podstawie adresu email.
     *
     * @param email Adres email użytkownika, którego dane logowania mają zostać pobrane.
     * @return Obiekt klasy Login odpowiadający danym logowania z bazy danych,
     * lub null, jeśli nie istnieje użytkownik o podanym adresie email.
     */
    suspend fun getLoginByEmail(email: String): Login?

    /**
     * Suspend function do aktualizacji istniejącego rekordu (logowania) w bazie danych Firestore.
     *
     * @param email Adres email użytkownika, którego dane logowania mają zostać zaktualizowane.
     * @param updatedLogin Obiekt klasy Login z zaktualizowanymi danymi.
     */
    suspend fun updateLogin(email: String, updatedLogin: Login)

    /**
     * Suspend function do usuwania istniejącego rekordu (logowania) z bazy danych na podstawie adresu email.
     *
     * @param email Adres email użytkownika, którego dane logowania mają zostać usunięte.
     */
    suspend fun deleteLogin(email: String)
}
