package com.example.sr13.Patient

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.sr13.R

/**
RecyclerView jest biblioteką, która tworzy listę(y).
Zasadniczo zapewnia okno o ustalonym rozmiarze do załadowania dużego zbioru danych.
Na początku tworzy widoki, z których będzie korzystać, gdy widoki wychodzą poza zakres (okno)
i istnieje taka potrzeba, wykorzystuje je ponownie.
źródło: https://myenv.net/course/getcourse/recyclerview-w-androidzie/

: Adapter jest głównym kodem odpowiedzialnym za RecyclerView.
Zawiera wszystkie ważne metody dotyczące implementacji RecyclerView.
Podstawowe metody dla udanej implementacji to:

1. onCreateViewHolder: zajmuje się załadowanie układu karty jako elementu dla RecyclerView.
2. onBindViewHolder: zajmuje się ustawianiem różnych danych i metod związanych
z kliknięciami w konkretne elementyRecyclerView.
3. getItemCount: zwraca długość RecyclerView.

 *
 * @property patients Lista pacjentów do wyświetlenia.
 */
class PatientAdapter(private val patients:  MutableList<Patient>) : RecyclerView.Adapter<PatientAdapter.PatientViewHolder>() {

    /**
     * Widok do przechowywania elementów interfejsu użytkownika dla każdego elementu w RecyclerView.
     *
     * @param itemView Widok pojedynczego elementu RecyclerView.
     */
    class PatientViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.imageView)
        val nameTextView: TextView = itemView.findViewById(R.id.nameTextView)
        val ageTextView: TextView = itemView.findViewById(R.id.ageTextView)
        val deleteButton: Button = itemView.findViewById(R.id.deleteButton)
    }

    /**
     * Tworzy nowy ViewHolder, który zawiera widok elementu w interfejsie użytkownika.
     *
     * innymi słowy tworzymy nowy widok (na podstawie layoutu XML)
     * dla danego typu elementu w RecyclerView. Następnie tworzymy i zwracamy nową instancję ViewHoldera,
     * która przechowuje ten widok.
     *
     * @param parent Grupa, do której zostanie dołączony nowo utworzony widok po utworzeniu.
     * @param viewType Typ widoku, jesli nasz recyclerView posiada wiele typów.
     * @return ViewHolder zawierający nowo utworzony widok.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PatientViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.patient_item, parent, false)
        return PatientViewHolder(itemView)
    }

    /**
     * Metoda onBindViewHolder jest odpowiedzialna za powiązanie danych z określonym elementem w RecyclerView.
     *
     * Kiedy RecyclerView potrzebuje wyświetlić nowy widok (np. gdy jest on tworzony lub przewijany),
     * to właśnie metoda onBindViewHolder jest wywoływana dla każdego elementu, który ma być wyświetlony.
     * W tym momencie przekazujemy dane do widoku danego elementu.
     *
     * Konkretnie, w metodzie onBindViewHolder aktualizujemy zawartość widoku (takiego jak TextView lub ImageView)
     * danymi z określonego elementu z listy danych. Dzięki temu możemy dynamicznie wyświetlać różne
     * dane dla różnych elementów RecyclerView w oparciu o ich pozycje w liście.
     *
     * @param holder ViewHolder, którego dane mają być zaktualizowane.
     * @param position Pozycja elementu w zestawie danych.
     */

    override fun onBindViewHolder(holder: PatientViewHolder, position: Int) {
        val currentPatient = patients[position]
        holder.nameTextView.text = currentPatient.name
        holder.ageTextView.text = currentPatient.age.toString()
        holder.imageView.setImageResource(currentPatient.imageResId)

        // Ustawienie obsługi zdarzenia kliknięcia przycisku "Delete"
        holder.deleteButton.setOnClickListener {
            removePatient(currentPatient)
        }
    }

    /**
     * Zwraca liczbę elementów w zestawie danych.
     *
     * @return Liczba elementów w zestawie danych.
     */
    override fun getItemCount() = patients.size


    /**
     * Metoda usuwająca pacjenta z listy i aktualizująca RecyclerView.
     *
     * @param patient Pacjent, który ma zostać usunięty z listy.
     */
    private fun removePatient(patient: Patient) {
        val position = patients.indexOf(patient)
        if (position != -1) {
            patients.removeAt(position)
            notifyItemRemoved(position)
            // Obsługa przypadku, gdy lista jest pusta
            if (patients.isEmpty()) {
                println("Pusta lista")
            }
        }
    }
}
