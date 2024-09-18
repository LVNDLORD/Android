package com.example.parliament

import ParliamentMembersData
import android.annotation.SuppressLint

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.parliament.ui.theme.ParliamentTheme


class MainActivity : ComponentActivity() {
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ParliamentTheme {
                GreetingPreview()
            }
        }
    }
}

@Composable
fun DisplayMember(modifier: Modifier = Modifier) {
    var selectedMember by remember { mutableStateOf(ParliamentMembersData.members[0]) }
    val randomIndex = java.util.Random().nextInt(ParliamentMembersData.members.size)


    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(30.dp)
            .size(400.dp)
    ) {


        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 56.dp)
                .size(600.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            Image(
                painter = painterResource(R.drawable.portrait_placeholder),
                contentDescription = null,
                modifier = Modifier.size(200.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))

            Text("${stringResource(R.string.name)}: ${selectedMember.firstname} ${selectedMember.lastname}")
            HorizontalDivider(modifier = Modifier.height(16.dp))

            if (selectedMember.minister) {
                Text(text = stringResource(R.string.minister))
                HorizontalDivider(modifier = Modifier.height(16.dp))
            }

            Text(text = "${stringResource(R.string.party)}: ${(selectedMember.party.uppercase())}")
            HorizontalDivider(modifier = Modifier.height(16.dp))

            Text(text = "${stringResource(R.string.seat_number)}: ${selectedMember.seatNumber}")
            HorizontalDivider(modifier = Modifier.height(16.dp))

        }
        Button(
            onClick = {
                selectedMember = ParliamentMembersData.members[randomIndex]
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
                .fillMaxWidth()
                .height(48.dp)
            // Adjust height as needed

        ) {
            Text(text = "Pick random member")
        }
    }


}


@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    DisplayMember(
        modifier = Modifier
            .fillMaxSize()
            .wrapContentSize(Alignment.Center)
    )
}
