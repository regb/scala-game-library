package com.example.myapplication

import android.content.Context
import android.os.Bundle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.myapplication.ui.theme.MyApplicationTheme
import sgl.android.AndroidPlatformProxy
import sgl.android.BaseMainActivity
import sgl.android.GameView
import sgl.proxy.ProxiedGameApp

fun makeGameApp(context: Context, gameView: GameView): ProxiedGameApp {
    //return com.regblanc.sgl.snake.core.Wiring.wire(AndroidPlatformProxy(gameView))
    return com.regblanc.sgl.test.core.Wiring.wire(AndroidPlatformProxy(context, gameView))

}

class MainActivity : BaseMainActivity(::makeGameApp) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {

    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MyApplicationTheme {
        Greeting("Android")
    }
}

