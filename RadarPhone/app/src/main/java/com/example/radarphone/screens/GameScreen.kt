package com.example.radarphone.screens

import android.Manifest
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.example.radarphone.R
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun GameScreen(navController: NavController, placeName: String?, lat: Double?, lng: Double?) {
    val configuration = LocalConfiguration.current
    val changeSize = (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE)

    val screenPadding = if (changeSize) {
        16.dp
    } else {
        80.dp
    }

    val context = LocalContext.current
    val fusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(LocalContext.current)

    var currentDistance by remember { mutableStateOf(0f) }
    var myLat by remember { mutableStateOf(0.0) }
    var myLng by remember { mutableStateOf(0.0) }
    var timeLeft by remember { mutableIntStateOf(600) } // 10 minutes in seconds
    var gameEnded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationProviderClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    myLat = location.latitude
                    myLng = location.longitude
                }
            }
        }
    }

    // Background image
    Image(
        painter = painterResource(id = R.drawable.mainscreen),
        contentDescription = "Background Image",
        modifier = Modifier.fillMaxSize(),
        contentScale = ContentScale.Crop
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(screenPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Text(text = "Place: $placeName", fontSize = 18.sp, color = Color.White)

        // Display the distance
        Text(text = "Distance: ${currentDistance.toInt()} m", fontSize = 10.sp, color = Color.White)

        Spacer(modifier = Modifier.weight(1f))
        // Radar
        Radar(
            modifier = Modifier.fillMaxWidth().weight(4f),
            myLat = myLat,
            myLng = myLng,
            targetLat = lat ?: 0.0,
            targetLng = lng ?: 0.0,
            currentDistance = currentDistance
        )
        // Timer and Back Button in a row
        Spacer(modifier = Modifier.weight(1f))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Timer at the bottom left
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                // Update distance every 10 seconds
                fusedLocationProviderClient.lastLocation.addOnSuccessListener { location ->
                    if (location != null) {
                        val results = FloatArray(1)
                        Location.distanceBetween(
                            myLat,
                            myLng,
                            lat!!,
                            lng!!,
                            results
                        )
                        currentDistance = results[0]
                    }
                }
            }

            LaunchedEffect(Unit) {
                while (timeLeft > 0 && isActive && !gameEnded) {
                    delay(1000L) // Delay for 1 second
                    timeLeft--
                    if (timeLeft % 10 == 0) {
                        if (ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.ACCESS_FINE_LOCATION
                            ) == PackageManager.PERMISSION_GRANTED
                        ) {
                            // Update distance every 10 seconds
                            fusedLocationProviderClient.lastLocation.addOnSuccessListener { location ->
                                if (location != null) {
                                    val results = FloatArray(1)
                                    Location.distanceBetween(
                                        myLat,
                                        myLng,
                                        lat!!,
                                        lng!!,
                                        results
                                    )
                                    currentDistance = results[0]
                                    if (currentDistance <= 2) {
                                        gameEnded = true
                                        Toast.makeText(
                                            context,
                                            "Victory!",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        navController.popBackStack()
                                    }
                                }
                            }
                        }
                    }
                }
                if (timeLeft == 0 && !gameEnded) {
                    gameEnded = true
                    Toast.makeText(context, "Defeat!", Toast.LENGTH_SHORT).show()
                    navController.popBackStack()
                }
            }

            Text(
                text = "Time left: ${timeLeft / 60}:${(timeLeft % 60).toString().padStart(2, '0')}",
                color = Color.White,
                fontSize = 16.sp
            )

            // Back Button at the bottom right
            Button(
                onClick = {
                    gameEnded = true
                    Toast.makeText(context, "Defeat!", Toast.LENGTH_SHORT).show()
                    navController.popBackStack()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Magenta,
                    contentColor = Color.White
                )
            ) {
                Text("Quit")
            }
        }
    }
}

@Composable
fun Radar(
    modifier: Modifier = Modifier,
    myLat: Double,
    myLng: Double,
    targetLat: Double,
    targetLng: Double,
    currentDistance: Float
) {
    var sweepAngle by remember { mutableFloatStateOf(0f) }
    val context = LocalContext.current
    var targetAngle by remember { mutableFloatStateOf(0f) }

    val sensorManager = context.getSystemService(android.content.Context.SENSOR_SERVICE) as SensorManager
    val accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    val magnetometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

    var accelerometerValues by remember { mutableStateOf(FloatArray(3)) }
    var magnetometerValues by remember { mutableStateOf(FloatArray(3)) }

    val sensorEventListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            when (event.sensor.type) {
                Sensor.TYPE_ACCELEROMETER -> accelerometerValues = event.values.clone()
                Sensor.TYPE_MAGNETIC_FIELD -> magnetometerValues = event.values.clone()
            }
            val rotationMatrix = FloatArray(9)
            val inclinationMatrix = FloatArray(9)
            val orientationAngles = FloatArray(3)

            if (SensorManager.getRotationMatrix(rotationMatrix, inclinationMatrix, accelerometerValues, magnetometerValues)) {
                SensorManager.getOrientation(rotationMatrix, orientationAngles)
                val azimuth = Math.toDegrees(orientationAngles[0].toDouble()).toFloat()
                val bearing = calculateBearing(myLat, myLng, targetLat, targetLng)
                targetAngle = (bearing - azimuth + 360) % 360
            }
        }

        override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}
    }

    LaunchedEffect(Unit) {
        while (true) {
            sweepAngle = (sweepAngle + 2f) % 360f
            delay(20L) // Adjust the speed of the sweep here
        }
    }

    LaunchedEffect(Unit) {
        sensorManager.registerListener(sensorEventListener, accelerometerSensor, SensorManager.SENSOR_DELAY_GAME)
        sensorManager.registerListener(sensorEventListener, magnetometerSensor, SensorManager.SENSOR_DELAY_GAME)
    }

    DisposableEffect(Unit) {
        onDispose {
            sensorManager.unregisterListener(sensorEventListener)
        }
    }

    Canvas(modifier = modifier) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        val centerX = canvasWidth / 2
        val centerY = canvasHeight / 2
        val radius = minOf(centerX, centerY) * 0.9f // Use 90% of the smaller dimension

        // Draw the circles
        drawCircle(
            color = Color.Green,
            radius = radius,
            center = Offset(centerX, centerY),
            style = Stroke(width = 2f)
        )
        drawCircle(
            color = Color.Green,
            radius = radius * 0.66f,
            center = Offset(centerX, centerY),
            style = Stroke(width = 2f)
        )
        drawCircle(
            color = Color.Green,
            radius = radius * 0.33f,
            center = Offset(centerX, centerY),
            style = Stroke(width = 2f)
        )

        // Draw the lines
        drawLine(
            color = Color.Green,
            start = Offset(centerX, 0f),
            end = Offset(centerX, canvasHeight),
            strokeWidth = 2f
        )
        drawLine(
            color = Color.Green,
            start = Offset(0f, centerY),
            end = Offset(canvasWidth, centerY),
            strokeWidth = 2f
        )

        // Draw the sweeping line
        val sweepRadians = Math.toRadians(sweepAngle.toDouble()).toFloat()
        val sweepEndX = centerX + radius * cos(sweepRadians)
        val sweepEndY = centerY + radius * sin(sweepRadians)

        val path = Path().apply {
            moveTo(centerX, centerY)
            lineTo(sweepEndX, sweepEndY)
        }
        drawPath(
            path = path,
            color = Color.Green,
            style = Stroke(width = 3f)
        )

        // Draw the center point (user's location)
        drawCircle(
            color = Color.Red,
            radius = 8f,
            center = Offset(centerX, centerY)
        )

        // Draw the target point
        val targetRadians = Math.toRadians(targetAngle.toDouble()).toFloat()
        val targetRadius = when {
            currentDistance > 250 -> radius
            currentDistance in 100.0..250.0 -> radius * 0.66f
            else -> radius * 0.33f
        }
        val targetX = centerX + targetRadius * cos(targetRadians)
        val targetY = centerY + targetRadius * sin(targetRadians)

        drawCircle(
            color = Color.Yellow,
            radius = 10f,
            center = Offset(targetX, targetY)
        )
    }
}

fun calculateBearing(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Float {
    val dLon = Math.toRadians(lon2 - lon1)
    val y = sin(dLon) * cos(Math.toRadians(lat2))
    val x = cos(Math.toRadians(lat1)) * sin(Math.toRadians(lat2)) - sin(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * cos(dLon)
    return Math.toDegrees(atan2(y, x)).toFloat()
}