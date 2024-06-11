package com.solvek.electricitynotifier

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.solvek.electricitynotifier.EnApp.Companion.enApp
import com.solvek.electricitynotifier.ui.theme.ElectricityNotifierTheme

class MainActivity : ComponentActivity() {
    private val model by lazy { enApp }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

//        schedulePeriodic()
//        model.log("Scheduled")
        setContent {
            val content by model.log.collectAsState()
            val enabled by model.enabled.collectAsState()
            ElectricityNotifierTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    LogContent(
                        content = content,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        enabled,
                        model::toggleAvailability,
                        model::electricityOn,
                        model::electricityOff
                    )
                }
            }
        }
    }
}

@Composable
fun LogContent(
    content: String,
    modifier: Modifier = Modifier,
    isEnabled: Boolean,
    toggleAvailability: ()->Unit,
    forceOn: () -> Unit,
    forceOff: () -> Unit) {
    Column(modifier = modifier) {
        Button(
            onClick = toggleAvailability,
            colors = ButtonDefaults.buttonColors(containerColor =
                if (isEnabled){
                    Color(1, 50, 32)
                }
                else {
                    Color.Red
                }
            ),
            modifier = Modifier.fillMaxWidth()
        ){
            Text(stringResource(if (isEnabled) R.string.disable else R.string.enable))
        }
        Text(
            text = content,
            modifier = modifier
                .weight(1.0f)
                .verticalScroll(rememberScrollState())
//            .clickable {
//                val actuator = Actuator()
//                GlobalScope.launch {
//                    actuator.notify(true)
//                }
//            }
        )
        Row(Modifier.fillMaxWidth()) {
            Button(onClick = forceOn, Modifier.weight(1f)){
                Text(stringResource(R.string.force_on))
            }
            Button(onClick = forceOff, Modifier.weight(1f)){
                Text(stringResource(R.string.force_off))
            }
        }
    }
}

//@Preview(showBackground = true)
//@Composable
//fun GreetingPreview() {
//    ElectricityNotifierTheme {
//        LogContent("Android"){
//
//        }
//    }
//}