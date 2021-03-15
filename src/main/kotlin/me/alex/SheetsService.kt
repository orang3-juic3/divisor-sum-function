package me.alex

import com.google.api.client.auth.oauth2.Credential
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.client.util.store.FileDataStoreFactory
import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.SheetsScopes
import java.io.File
import java.io.InputStream
import java.io.InputStreamReader
import java.util.*

object SheetsService {
    private const val appName : String = "DivisorSumFunction"
    private val factory: GsonFactory = GsonFactory.getDefaultInstance()

    val sheetsService: Sheets by lazy { Sheets.Builder(GoogleNetHttpTransport.newTrustedTransport(),
            factory, authorise())
            .setApplicationName(appName)
            .build() }
    private fun authorise() : Credential {
        val input : InputStream = this.javaClass.getResourceAsStream("/credentials.json")
        val clientSecrets = GoogleClientSecrets.load(factory, InputStreamReader(input))
        val scopes: List<String> = Collections.singletonList(SheetsScopes.SPREADSHEETS)
        val flow = GoogleAuthorizationCodeFlow.Builder(GoogleNetHttpTransport.newTrustedTransport(), factory, clientSecrets, scopes)
                .setDataStoreFactory(FileDataStoreFactory(File("tokens")))
                .setAccessType("offline")
                .build()
        return AuthorizationCodeInstalledApp(flow, LocalServerReceiver()).authorize("user")
    }

}