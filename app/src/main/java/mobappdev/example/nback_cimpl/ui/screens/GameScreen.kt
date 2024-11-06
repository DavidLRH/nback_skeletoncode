package mobappdev.example.nback_cimpl.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import mobappdev.example.nback_cimpl.ui.viewmodels.FakeVM
import mobappdev.example.nback_cimpl.ui.viewmodels.GameType
import mobappdev.example.nback_cimpl.ui.viewmodels.GameViewModel

@Composable
fun GameScreen(vm: GameViewModel) {
    val gameState by vm.gameState.collectAsState()
    val score by vm.score.collectAsState()

    if (gameState.gameEnded) {
        HomeScreen(vm = vm)
    } else {

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            Text("N-Back Level: ${vm.nBack}, Event Number: ${gameState.eventNumber}")

            Text("Score: $score", modifier = Modifier.padding(vertical = 16.dp))




            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if(gameState.gameType == GameType.Visual){
                repeat(3) { row ->
                    Row(horizontalArrangement = Arrangement.SpaceEvenly) {
                        repeat(3) { col ->
                            Box(
                                modifier = Modifier
                                    .size(120.dp)
                                    .padding(2.dp)
                                    .background(
                                        if (gameState.eventValue == row * 3 + col) Color.Green else Color.Gray
                                    )
                            )
                        }
                    }
                }
            }else{repeat(3) { row ->
                    Row(horizontalArrangement = Arrangement.SpaceEvenly) {
                        repeat(3) { col ->
                            Box(
                                modifier = Modifier
                                    .size(120.dp)
                                    .padding(2.dp)
                                    .background(Color.Gray)
                            )
                        }
                    }
                }


                }            }

            Button(
                onClick = { vm.checkMatch() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (gameState.isMatchButtonError) Color.Red else Color.Green,
                    contentColor = Color.White
                )
            )
            { Text("Match") }



            Button(
                onClick = { vm.goToHomeScreen() },
                modifier = Modifier.padding(top = 100.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Red,
                    contentColor = Color.White
                )
            ) {
                Text("Go to Home")
            }
        }
    }
}

@Preview
@Composable
fun GameScreenPreview() {
    // Since I am injecting a VM into my homescreen that depends on Application context, the preview doesn't work.
    Surface() {
        GameScreen(FakeVM())
    }
}