package com.sriyanksiddhartha.ownchatapp.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.sriyanksiddhartha.ownchatapp.LoginActivity
import com.sriyanksiddhartha.ownchatapp.viewmodel.ChannelListViewModel
import dagger.hilt.android.AndroidEntryPoint
import io.getstream.chat.android.client.models.Filters
import io.getstream.chat.android.compose.ui.channels.ChannelsScreen
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import kotlinx.coroutines.flow.collect

@AndroidEntryPoint
class ChannelListActivity : ComponentActivity() {

    val viewModel: ChannelListViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        subscribeToEvents()

        setContent {
            ChatTheme {

                var showDialog: Boolean by remember {
                    mutableStateOf(false)
                }

                if (showDialog) {
                    CreateChannelDialog(
                        dismiss = { channelName ->
                            viewModel.createChannel(channelName)
                            showDialog = false
                        }
                    )
                }

                ChannelsScreen(
                    filters = Filters.`in`(
                        fieldName = "type",
                        values = listOf("gaming", "messaging", "commerce", "team", "livestream")
                    ),
                    title = "Channel List",
                    isShowingSearch = true,
                    onItemClick = { channel ->
                        startActivity(MessagesActivity.getIntent(this, channelId = channel.cid))
                    },
                    onBackPressed = { finish() },
                    onHeaderActionClick = {
                        showDialog = true
                    },
                    onHeaderAvatarClick = {
                        viewModel.logout()
                        finish()
                        startActivity(Intent(this, LoginActivity::class.java))
                    }
                )
            }
        }
    }

    @Composable
    private fun CreateChannelDialog(dismiss: (String) -> Unit) {

        var channelName by remember {
            mutableStateOf("")
        }

        AlertDialog(
            onDismissRequest = { dismiss(channelName) },
            title = {
                Text(text = "Enter Channel Name")
            },
            text = {
                TextField(
                    value = channelName,
                    onValueChange = {channelName = it}
                )
            },
            buttons = {
                Row(
                    modifier = Modifier.padding(all = 8.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { dismiss(channelName) }
                    ) {
                        Text(text = "Create Channel")
                    }
                }
            }
        )
    }

    private fun subscribeToEvents() {

        lifecycleScope.launchWhenStarted {

            viewModel.createChannelEvent.collect { event ->

                when (event) {

                    is ChannelListViewModel.CreateChannelEvent.Error -> {
                        val errorMessage = event.error
                        showToast(errorMessage)
                    }

                    is ChannelListViewModel.CreateChannelEvent.Success -> {
                        showToast("Channel Created!")
                    }
                }
            }
        }
    }

    private fun showToast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
}




