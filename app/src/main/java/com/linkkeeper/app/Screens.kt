package com.linkkeeper.app

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.UUID

private const val ALL = "همه"
private const val NEW_GROUP = "+ گروه جدید"
private val Bg = Color(0xFFF6F5F1)
private val Ink = Color(0xFF1B1B1A)
private val Muted = Color(0xFF55554F)
private val Accent = Color(0xFF2F5BEA)
private val Danger = Color(0xFFB3261E)

fun openUrl(context: Context, raw: String) {
    val s = raw.trim()
    val full = if (s.contains("://")) s else "https://$s"
    try {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(full)))
    } catch (e: Exception) {
        Toast.makeText(context, "امکان باز کردن این لینک نیست", Toast.LENGTH_SHORT).show()
    }
}

@Composable
private fun fieldColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = Color.White,
    unfocusedContainerColor = Color.White,
    focusedBorderColor = Accent,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Dropdown(label: String, options: List<String>, selected: String, onSelect: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = selected,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = fieldColors(),
            modifier = Modifier.menuAnchor().fillMaxWidth(),
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { opt ->
                DropdownMenuItem(
                    text = { Text(opt) },
                    onClick = {
                        onSelect(opt)
                        expanded = false
                    },
                )
            }
        }
    }
}

// ---------- Lock / first-run password ----------

@Composable
fun LockScreen(auth: PasswordStore, onUnlock: () -> Unit) {
    val setup = remember { !auth.isSet() }
    var pw by remember { mutableStateOf("") }
    var pw2 by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    val ltr = TextStyle(textDirection = TextDirection.Ltr)

    Column(
        modifier = Modifier.fillMaxSize().background(Bg).padding(24.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            if (setup) "تعیین رمز عبور" else "ورود به لینک‌های من",
            fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Ink,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            if (setup) "برای محافظت از لینک‌ها یک رمز عبور انتخاب کن." else "برای دیدن لینک‌ها رمز عبور را وارد کن.",
            fontSize = 14.sp, color = Muted,
        )
        Spacer(Modifier.height(20.dp))
        OutlinedTextField(
            value = pw,
            onValueChange = { pw = it; error = null },
            label = { Text("رمز عبور") },
            singleLine = true,
            textStyle = ltr,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            colors = fieldColors(),
            modifier = Modifier.fillMaxWidth(),
        )
        if (setup) {
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = pw2,
                onValueChange = { pw2 = it; error = null },
                label = { Text("تکرار رمز عبور") },
                singleLine = true,
                textStyle = ltr,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                colors = fieldColors(),
                modifier = Modifier.fillMaxWidth(),
            )
        }
        error?.let {
            Spacer(Modifier.height(8.dp))
            Text(it, color = Danger, fontSize = 13.sp)
        }
        Spacer(Modifier.height(20.dp))
        Button(
            onClick = {
                if (setup) {
                    when {
                        pw.length < 4 -> error = "رمز باید حداقل ۴ کاراکتر باشد"
                        pw != pw2 -> error = "تکرار رمز یکسان نیست"
                        else -> {
                            auth.set(pw)
                            onUnlock()
                        }
                    }
                } else if (auth.verify(pw)) {
                    onUnlock()
                } else {
                    error = "رمز عبور اشتباه است"
                    pw = ""
                }
            },
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Accent),
            modifier = Modifier.fillMaxWidth().height(52.dp),
        ) {
            Text(if (setup) "ذخیره و ورود" else "ورود", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}

// ---------- App state / navigation ----------

private sealed interface Screen {
    data object Home : Screen
    data class Form(val item: LinkItem?) : Screen
}

@Composable
fun LinksApp(store: LinkStore, onExit: () -> Unit) {
    val context = LocalContext.current
    val links = remember { mutableStateListOf<LinkItem>().apply { addAll(store.loadLinks()) } }
    val groups = remember { mutableStateListOf<String>().apply { addAll(store.loadGroups()) } }
    var screen by remember { mutableStateOf<Screen>(Screen.Home) }

    fun persist() = store.save(links.toList(), groups.toList())

    when (val s = screen) {
        Screen.Home -> HomeScreen(
            links = links,
            groups = groups,
            onOpen = { openUrl(context, it.url) },
            onEdit = { screen = Screen.Form(it) },
            onAdd = { screen = Screen.Form(null) },
            onExit = onExit,
        )
        is Screen.Form -> {
            BackHandler { screen = Screen.Home }
            FormScreen(
                item = s.item,
                groups = groups,
                onSave = { item ->
                    if (item.group !in groups) groups.add(item.group)
                    val idx = links.indexOfFirst { it.id == item.id }
                    if (idx >= 0) links[idx] = item else links.add(0, item)
                    persist()
                    screen = Screen.Home
                },
                onDelete = { item ->
                    links.removeAll { it.id == item.id }
                    persist()
                    screen = Screen.Home
                },
                onCancel = { screen = Screen.Home },
            )
        }
    }
}

// ---------- Home ----------

@Composable
fun HomeScreen(
    links: List<LinkItem>,
    groups: List<String>,
    onOpen: (LinkItem) -> Unit,
    onEdit: (LinkItem) -> Unit,
    onAdd: () -> Unit,
    onExit: () -> Unit,
) {
    var query by remember { mutableStateOf("") }
    var filter by remember { mutableStateOf<String?>(null) }
    val q = query.trim()
    val shown = links.filter { l ->
        (filter == null || l.group == filter) &&
            (q.isEmpty() || l.title.contains(q, true) || l.url.contains(q, true) || l.description.contains(q, true))
    }
    val byGroup = shown.groupBy { it.group }
    val orderedGroups = (groups + byGroup.keys).distinct().filter { byGroup.containsKey(it) }

    Box(Modifier.fillMaxSize().background(Bg)) {
        LazyColumn(
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 24.dp, bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("لینک‌های من", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Ink)
                    IconButton(onClick = onExit) {
                        Icon(
                            Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = "خروج کامل از برنامه",
                            tint = Danger,
                        )
                    }
                }
            }
            item {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = { Text("جستجو در لینک‌ها و توضیحات") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    singleLine = true,
                    colors = fieldColors(),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            item {
                Dropdown("گروه", listOf(ALL) + groups, filter ?: ALL) {
                    filter = if (it == ALL) null else it
                }
            }
            if (shown.isEmpty()) {
                item {
                    Text(
                        if (links.isEmpty()) "هنوز لینکی ذخیره نکرده‌ای. با دکمه + شروع کن." else "موردی پیدا نشد.",
                        color = Muted, fontSize = 14.sp,
                        modifier = Modifier.padding(top = 16.dp),
                    )
                }
            }
            orderedGroups.forEach { g ->
                val groupItems = byGroup.getValue(g)
                item(key = "header_$g") {
                    Row(Modifier.padding(top = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(g, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Ink)
                        Spacer(Modifier.padding(horizontal = 4.dp))
                        Text("${groupItems.size} لینک", fontSize = 13.sp, color = Muted)
                    }
                }
                items(groupItems, key = { it.id }) { LinkCard(it, onOpen, onEdit) }
            }
        }
        FloatingActionButton(
            onClick = onAdd,
            containerColor = Accent,
            contentColor = Color.White,
            modifier = Modifier.align(Alignment.BottomEnd).padding(20.dp),
        ) {
            Icon(Icons.Default.Add, contentDescription = "افزودن لینک")
        }
    }
}

@Composable
private fun LinkCard(item: LinkItem, onOpen: (LinkItem) -> Unit, onEdit: (LinkItem) -> Unit) {
    val shape = RoundedCornerShape(16.dp)
    Card(
        modifier = Modifier.fillMaxWidth().clip(shape).clickable { onOpen(item) },
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(Modifier.padding(start = 16.dp, top = 8.dp, end = 8.dp, bottom = 14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    item.title.ifBlank { item.url },
                    modifier = Modifier.weight(1f),
                    fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Ink,
                )
                IconButton(onClick = { onEdit(item) }) {
                    Icon(Icons.Default.Edit, contentDescription = "ویرایش", tint = Muted)
                }
            }
            Text(
                item.url,
                modifier = Modifier.fillMaxWidth().padding(end = 8.dp),
                style = TextStyle(textDirection = TextDirection.Ltr),
                textAlign = TextAlign.Right,
                fontSize = 13.sp, color = Accent,
            )
            if (item.description.isNotBlank()) {
                Spacer(Modifier.height(6.dp))
                Text(
                    item.description,
                    modifier = Modifier.padding(end = 8.dp),
                    fontSize = 14.sp, color = Muted, lineHeight = 22.sp,
                )
            }
        }
    }
}

// ---------- Add / edit ----------

@Composable
fun FormScreen(
    item: LinkItem?,
    groups: List<String>,
    onSave: (LinkItem) -> Unit,
    onDelete: (LinkItem) -> Unit,
    onCancel: () -> Unit,
) {
    var groupSel by remember { mutableStateOf(item?.group ?: groups.firstOrNull() ?: NEW_GROUP) }
    var newGroup by remember { mutableStateOf("") }
    var title by remember { mutableStateOf(item?.title ?: "") }
    var url by remember { mutableStateOf(item?.url ?: "") }
    var desc by remember { mutableStateOf(item?.description ?: "") }
    var confirmDelete by remember { mutableStateOf(false) }
    val clipboard = LocalClipboardManager.current
    val isNew = groupSel == NEW_GROUP
    val canSave = url.isNotBlank() && (!isNew || newGroup.isNotBlank())

    Column(
        modifier = Modifier.fillMaxSize().background(Bg).verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text(
            if (item == null) "افزودن لینک" else "ویرایش لینک",
            fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Ink,
            modifier = Modifier.padding(top = 4.dp),
        )
        Dropdown("گروه", listOf(NEW_GROUP) + groups, groupSel) { groupSel = it }
        if (isNew) {
            OutlinedTextField(
                value = newGroup,
                onValueChange = { newGroup = it },
                label = { Text("نام گروه جدید") },
                singleLine = true,
                colors = fieldColors(),
                modifier = Modifier.fillMaxWidth(),
            )
        }
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("عنوان") },
            singleLine = true,
            colors = fieldColors(),
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = url,
            onValueChange = { url = it },
            label = { Text("لینک") },
            singleLine = true,
            textStyle = TextStyle(textDirection = TextDirection.Ltr),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
            trailingIcon = {
                TextButton(onClick = { clipboard.getText()?.text?.let { url = it.trim() } }) {
                    Text("جای‌گذاری")
                }
            },
            colors = fieldColors(),
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = desc,
            onValueChange = { desc = it },
            label = { Text("توضیحات") },
            minLines = 4,
            colors = fieldColors(),
            modifier = Modifier.fillMaxWidth(),
        )
        Button(
            onClick = {
                onSave(
                    LinkItem(
                        id = item?.id ?: UUID.randomUUID().toString(),
                        group = if (isNew) newGroup.trim() else groupSel,
                        title = title.trim(),
                        url = url.trim(),
                        description = desc.trim(),
                    )
                )
            },
            enabled = canSave,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Accent),
            modifier = Modifier.fillMaxWidth().height(52.dp),
        ) {
            Text("ذخیره", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
        TextButton(onClick = onCancel, modifier = Modifier.fillMaxWidth()) {
            Text("انصراف", color = Muted)
        }
        if (item != null) {
            TextButton(onClick = { confirmDelete = true }, modifier = Modifier.fillMaxWidth()) {
                Text("حذف لینک", color = Danger)
            }
        }
    }

    if (confirmDelete && item != null) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("حذف لینک؟") },
            text = { Text("این لینک برای همیشه حذف می‌شود.") },
            confirmButton = {
                TextButton(onClick = { onDelete(item) }) { Text("حذف", color = Danger) }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = false }) { Text("انصراف") }
            },
        )
    }
}
