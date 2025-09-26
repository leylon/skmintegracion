package com.skm.skmintegracion.hiopos.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// 1. Se crea el DataStore como una extensión del Context para que sea un singleton fácil de acceder.
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "hiopos_settings")

// 2. Se crea una clase para gestionar la lectura y escritura.
class HioposSettingsManager(private val context: Context) {

    // 3. Se definen las claves para cada campo que quieres guardar.
    private object PreferencesKeys {
        val DOC_TARJETA = stringPreferencesKey("doc_tarjeta")
        val ID_FORMA_PAGO_KEY = stringPreferencesKey("id_forma_pago_key")
        val ID_TARJETA_KEY = stringPreferencesKey("id_tarjeta_key")
        val ID_REF = stringPreferencesKey("id_ref")
        val N_TARJETA = stringPreferencesKey("n_tarjeta")
        val CUOTA = stringPreferencesKey("cuota")
        val ID_ENTIDAD = stringPreferencesKey("id_entidad")
        val SALE_ID = stringPreferencesKey("sale_id")
        val DOCUMENT_DATA = stringPreferencesKey("document_data")
    }

    // ... Aquí irán las funciones para guardar y leer
    suspend fun saveHioposData(response: HioposDataResponse) {
        context.dataStore.edit { settings ->
            settings[PreferencesKeys.DOC_TARJETA] = response.docTarjeta
            settings[PreferencesKeys.ID_FORMA_PAGO_KEY] = response.IdFormaPagoKey
            settings[PreferencesKeys.ID_TARJETA_KEY] = response.IdTarjetaKey
            settings[PreferencesKeys.ID_REF] = response.IdRef
            settings[PreferencesKeys.N_TARJETA] = response.Ntarjeta
            settings[PreferencesKeys.CUOTA] = response.Cuota
            settings[PreferencesKeys.ID_ENTIDAD] = response.IdEntidad
            settings[PreferencesKeys.SALE_ID] = response.SaleId
            settings[PreferencesKeys.DOCUMENT_DATA] = response.documentData
        }
    }
    suspend fun saveDocumentData(documentData: String) {
        context.dataStore.edit { settings ->
            settings[PreferencesKeys.DOCUMENT_DATA] = documentData
        }
    }
    // Dentro de la clase HioposSettingsManager

    /**
     * Recupera los datos guardados como un Flow.
     * El Flow notificará a los observadores cada vez que los datos cambien.
     */
    val hioposDataFlow: Flow<HioposDataResponse> = context.dataStore.data
        .map { preferences ->
            HioposDataResponse(
                docTarjeta = preferences[PreferencesKeys.DOC_TARJETA] ?: "",
                IdFormaPagoKey = preferences[PreferencesKeys.ID_FORMA_PAGO_KEY] ?: "",
                IdTarjetaKey = preferences[PreferencesKeys.ID_TARJETA_KEY] ?: "",
                IdRef = preferences[PreferencesKeys.ID_REF] ?: "",
                Ntarjeta = preferences[PreferencesKeys.N_TARJETA] ?: "",
                Cuota = preferences[PreferencesKeys.CUOTA] ?: "",
                IdEntidad = preferences[PreferencesKeys.ID_ENTIDAD] ?: "",
                SaleId = preferences[PreferencesKeys.SALE_ID] ?: "",
                documentData = preferences[PreferencesKeys.DOCUMENT_DATA] ?: ""
            )
        }
    val hioposDataDocument: Flow<String> =  context.dataStore.data
        .map {  preference ->
            preference[PreferencesKeys.DOCUMENT_DATA] ?: ""
        }
}