package com.artix.callnote.ui

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.artix.callnote.data.Note
import com.artix.callnote.data.NoteRepository
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    NotesScreen(
                        repository = NoteRepository.get(this)
                    )
                }
            }
        }
        requestCorePermissions()
    }

    private fun requestCorePermissions() {
        permissionLauncher.launch(arrayOf(
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.READ_CALL_LOG
        ))
        if (!Settings.canDrawOverlays(this)) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            startActivity(intent)
        }
    }
}

@Composable
private fun NotesScreen(repository: NoteRepository) {
    var numberText by remember { mutableStateOf(TextFieldValue("")) }
    var noteText by remember { mutableStateOf(TextFieldValue("")) }
    val scope = rememberCoroutineScope()

    var notes by remember { mutableStateOf(emptyList<Note>()) }

    LaunchedEffect(Unit) {
        repository.observeAll().collectLatest { notes = it }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(text = "Save a note for a phone number", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = numberText,
            onValueChange = { numberText = it },
            label = { Text("Phone number") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = noteText,
            onValueChange = { noteText = it },
            label = { Text("Note") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Button(onClick = {
                val number = numberText.text.trim()
                val note = noteText.text.trim()
                if (number.isNotEmpty()) {
                    scope.launch { repository.save(number, note) }
                    noteText = TextFieldValue("")
                }
            }) { Text("Save") }
            Spacer(Modifier.width(12.dp))
            if (!Settings.canDrawOverlays(LocalContext.current)) {
                Text("Overlay permission needed to show notes during calls.")
            }
        }
        Spacer(Modifier.height(16.dp))
        Divider()
        Spacer(Modifier.height(8.dp))
        Text(text = "Saved notes", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        LazyColumn(modifier = Modifier.weight(1f)) {
            items(notes, key = { it.phoneNumber }) { n ->
                NoteRow(note = n, onDelete = {
                    scope.launch { repository.delete(n.phoneNumber) }
                })
            }
        }
    }
}

@Composable
private fun NoteRow(note: Note, onDelete: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = note.phoneNumber, style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(4.dp))
            Text(text = if (note.content.isNotBlank()) note.content else "(Empty note)")
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                TextButton(onClick = onDelete) { Text("Delete") }
            }
        }
    }
}
