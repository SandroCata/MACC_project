package com.example.radarphone.screens

import android.Manifest
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.util.Log
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
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

//ISSUES:
//Parks do not give back lat and lng.
//Orientation of place to find not working correctly
//Distance seems to be correctly updating now

@Composable
fun GameScreen(navController: NavController, placeName: String?, lat: Double?, lng: Double?) {
    val configuration = LocalConfiguration.current
    val changeSize = (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE)

    val screenPadding = if (changeSize) {
        16.dp
    } else {
        80.dp
    }

    //Log.d("Place location", "$lat, $lng")

    val context = LocalContext.current
    val fusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(LocalContext.current)

    var currentDistance by remember { mutableFloatStateOf(0f) }
    var myLat by remember { mutableDoubleStateOf(0.0) }
    var myLng by remember { mutableDoubleStateOf(0.0) }
    var timeLeft by remember { mutableIntStateOf(600) } // 10 minutes in seconds
    var gameEnded by remember { mutableStateOf(false) }

    // Location updates
    val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000L)
        .setMinUpdateDistanceMeters(1f) // Update only if moved at least 1 meter
        .build()
    val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            locationResult.lastLocation?.let { location ->
                myLat = location.latitude
                myLng = location.longitude
                val results = FloatArray(1)
                Location.distanceBetween(
                    myLat,
                    myLng,
                    lat!!,
                    lng!!,
                    results
                )
                currentDistance = results[0]
                Log.d("Distance", "Distance: $currentDistance")
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

    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationProviderClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                null
            )
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            fusedLocationProviderClient.removeLocationUpdates(locationCallback)
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

            LaunchedEffect(Unit) {
                while (timeLeft > 0 && isActive && !gameEnded) {
                    delay(1000L) // Delay for 1 second
                    timeLeft--
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
    myLng:Double,
    targetLat: Double,
    targetLng: Double,
    currentDistance: Float
) {
    var sweepAngle by remember { mutableFloatStateOf(0f) }
    val context = LocalContext.current
    var targetAngle by remember { mutableFloatStateOf(0f) }

    // Gets the system's sensor service.
    val sensorManager =
        context.getSystemService(android.content.Context.SENSOR_SERVICE) as SensorManager
    // Gets the accelerometer (measures acceleration in 3 axes)
    val accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    // Gets the magnetometer (measures the Earth's magnetic field)
    val magnetometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

    // Store the latest sensor readings
    var accelerometerValues by remember { mutableStateOf(FloatArray(3)) }
    var magnetometerValues by remember { mutableStateOf(FloatArray(3)) }

    // Store the filtered sensor readings
    var filteredAccelerometerValues by remember { mutableStateOf(FloatArray(3)) }
    var filteredMagnetometerValues by remember { mutableStateOf(FloatArray(3)) }

    // This object listens for changes in the accelerometer and magnetometer.
    val sensorEventListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            when (event.sensor.type) {
                // store the new readings for these sensors events
                Sensor.TYPE_ACCELEROMETER -> {
                    accelerometerValues = event.values.clone()
                    // Apply low-pass filter to accelerometer readings
                    filteredAccelerometerValues = lowPassFilter(accelerometerValues, filteredAccelerometerValues)
                }

                Sensor.TYPE_MAGNETIC_FIELD -> {
                    magnetometerValues = event.values.clone()
                    // Apply low-pass filter to magnetometer readings
                    filteredMagnetometerValues = lowPassFilter(magnetometerValues, filteredMagnetometerValues)
                }
            }
            // A 3x3 matrix that represents the phone's orientation in space.
            val rotationMatrix = FloatArray(9)
            // A matrix that represents the inclination of the magnetic field.
            val inclinationMatrix = FloatArray(9)
            // float array of size 3. It will contain the orientation angles.
            val orientationAngles = FloatArray(3)

            // This is the key function. It takes the accelerometer and magnetometer readings and combines them to calculate the rotationMatrix.
            // This matrix describes how the phone is rotated relative to a fixed coordinate system
            if (SensorManager.getRotationMatrix(
                    rotationMatrix,
                    inclinationMatrix,
                    filteredAccelerometerValues,
                    filteredMagnetometerValues
                )
            ) {
                // This function takes the rotationMatrix and converts it into three angles:
                // orientationAngles[0] (Azimuth): The angle between the magnetic north direction and the y-axis of the device. It is the angle we need.
                // orientationAngles[1] (Pitch): The angle between the x-axis and the horizontal plane.
                // orientationAngles[2] (Roll): The angle between the y-axis and the horizontal plane.
                SensorManager.getOrientation(rotationMatrix, orientationAngles)
                // the phone's orientation relative to magnetic north (obtained from the sensors).
                //val azimuth = Math.toDegrees(orientationAngles[0].toDouble()).toFloat()
                val azimuth = Math.toDegrees(orientationAngles[0].toDouble()).toFloat()
                val bearing = calculateBearing(myLat, myLng, targetLat, targetLng)
                // the final angle of the target on the radar, which is the bearing adjusted by the azimuth
                targetAngle = (bearing - azimuth + 360) % 360
            }
        }

        override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}
    }

    LaunchedEffect(Unit) {
        while (true) {
            sweepAngle = (sweepAngle + 1f) % 360f // slower sweep
            delay(50L) // Adjust the speed of the sweep here
        }
    }

    LaunchedEffect(Unit) {
        sensorManager.registerListener(
            sensorEventListener,
            accelerometerSensor,
            SensorManager.SENSOR_DELAY_UI
        )
        sensorManager.registerListener(
            sensorEventListener,
            magnetometerSensor,
            SensorManager.SENSOR_DELAY_UI
        )
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

// The initial direction from the user to the target (calculated using latitude and longitude).
fun calculateBearing(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Float {
    val dLon = Math.toRadians(lon2 - lon1)
    val y = sin(dLon) * cos(Math.toRadians(lat2))
    val x = cos(Math.toRadians(lat1)) * sin(Math.toRadians(lat2)) - sin(Math.toRadians(lat1)) * cos(
        Math.toRadians(lat2)
    ) * cos(dLon)
    return Math.toDegrees(atan2(y, x)).toFloat()
}

// Low-pass filter function
fun lowPassFilter(input: FloatArray, output: FloatArray): FloatArray {
    val alpha = 0.2f // Adjust this value for more or less smoothing (0.0 to 1.0)
    if (output.isEmpty()) {
        return input
    }
    for (i in input.indices) {
        output[i] = output[i] + alpha * (input[i] - output[i])
    }
    return output
}