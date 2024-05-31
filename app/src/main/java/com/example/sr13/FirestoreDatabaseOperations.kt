package com.example.sr13
import android.util.Log
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.tasks.await

/**
 * Klasa LoginFirestoreDatabaseOperations implementuje interfejs LoginFirestoreInterface
 * i zawiera metody do dodawania, pobierania, aktualizowania i usuwania danych logowania
 * w bazie danych Firestore.
 *
 * @property db - Referencja do obiektu FirebaseFirestore, służąca do interakcji z bazą danych Firestore.
 */
class FirestoreDatabaseOperations(private val db: FirebaseFirestore) : FirestoreInterface {

    /**
     * Funkcja do dodawania nowego rekordu (logowania) do bazy danych Firestore.
     * Wykorzystuje mechanizm korutyn do wykonywania operacji asynchronicznych.
     *
     * @param email Adres email użytkownika.
     * @param login Obiekt klasy Login, który ma zostać dodany do bazy danych.
     */
    override suspend fun addLogin(email: String, login: Login) {
        try {
            db.collection("logins").document(email).set(login).await()
        } catch (e: Exception) {
            // Obsługa błędów
        }
    }

    /**
     * Funkcja do pobierania danych logowania z bazy danych Firestore na podstawie adresu email.
     * Wykorzystuje mechanizm korutyn do wykonywania operacji asynchronicznych.
     *
     * @param email Adres email użytkownika, którego dane logowania mają zostać pobrane.
     * @return Obiekt klasy Login odpowiadający danym logowania z bazy danych,
     * lub null, jeśli nie istnieje użytkownik o podanym adresie email.
     */
    override suspend fun getLoginByEmail(email: String): Login? {
        val snapshot = db.collection("logins")
            .whereEqualTo(FieldPath.documentId(), email)
            .get()
            .await()

        return snapshot.documents.firstOrNull()?.toObject<Login>()
    }

    /**
     * Funkcja do aktualizacji istniejącego rekordu (logowania) w bazie danych Firestore.
     * Wykorzystuje mechanizm korutyn do wykonywania operacji asynchronicznych.
     *
     * @param email Adres email użytkownika, którego dane logowania mają zostać zaktualizowane.
     * @param updatedLogin Obiekt klasy Login z zaktualizowanymi danymi.
     */
    override suspend fun updateLogin(email: String, updatedLogin: Login) {
        try {
            db.collection("logins").document(email).set(updatedLogin).await()
        } catch (e: Exception) {
            // Obsługa błędów
        }
    }

    /**
     * Funkcja do usuwania istniejącego rekordu (logowania) z bazy danych Firestore na podstawie adresu email.
     * Wykorzystuje mechanizm korutyn do wykonywania operacji asynchronicznych.
     *
     * @param email Adres email użytkownika, którego dane logowania mają zostać usunięte.
     */
    override suspend fun deleteLogin(email: String) {
        try {
            db.collection("logins").document(email).delete().await()
        } catch (e: Exception) {
            // Obsługa błędów
        }
    }
}
