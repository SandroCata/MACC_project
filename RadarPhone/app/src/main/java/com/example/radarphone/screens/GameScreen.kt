package com.example.radarphone.screens

import android.Manifest
import android.content.Context
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
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontWeight
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
import kotlin.math.roundToInt
import kotlin.math.sin

@Composable
fun GameScreen(navController: NavController, placeName: String?, lat: Double?, lng: Double?, address: String?) {
    val configuration = LocalConfiguration.current
    val changeSize = (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE)

    val screenPadding = if (changeSize) {
        16.dp
    } else {
        80.dp
    }

    val myCustomFontFamily = androidx.compose.ui.text.font.FontFamily(
        Font(R.font.atmo, FontWeight.Normal))

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
        .setMinUpdateDistanceMeters(1f) // Update only if moved at least 5 meter
        .build()
    val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            locationResult.lastLocation?.let { location ->myLat = location.latitude
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
                if (currentDistance <= 8) {
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

    // Request location updates only if permission is granted
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

        Text(text = "The hunt begins :)", fontSize = 15.sp, color = Color.White, fontFamily = myCustomFontFamily)

        Spacer(modifier = Modifier.weight(1f))

        Text(text = "Place: $placeName", fontSize = 18.sp, color = Color.Yellow)
        Spacer(modifier = Modifier.height(10.dp))
        Text(text = "Address: $address", fontSize = 15.sp, color = Color.Yellow)
        Spacer(modifier = Modifier.height(10.dp))
        Text(text = "Distance: ${currentDistance.roundToInt()} m", fontSize = 15.sp, color = Color.Yellow)
        Spacer(modifier = Modifier.weight(1f))

        // Radar
        Radar(
            modifier = Modifier
                .fillMaxWidth()
                .weight(4f),
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

            LaunchedEffect(gameEnded) {
                while (timeLeft > 0 && isActive && !gameEnded) {
                    delay(1000L) // Delay for 1 second
                    timeLeft--
                }
                if (timeLeft == 0 && !gameEnded) {
                    gameEnded = true
                    Toast.makeText(context, "Game Over!", Toast.LENGTH_SHORT).show()
                    navController.popBackStack()
                }
            }

            Text(
                text= "Time left: ${timeLeft / 60}:${(timeLeft % 60).toString().padStart(2, '0')}",
                color = Color.White,
                fontSize = 18.sp
            )

            // Back Button at the bottom right
            Button(
                onClick = {
                    gameEnded = true
                    Toast.makeText(context, "Game Over!", Toast.LENGTH_SHORT).show()
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
    var azimuth by remember { mutableFloatStateOf(0f) }

    // Calibration variables
    var isCalibrating by remember { mutableStateOf(false) }
    var calibrationData by remember { mutableStateOf(mutableListOf<FloatArray>()) }
    var calibrationOffsets by remember { mutableStateOf(FloatArray(3) { 0f }) }
    var isCalibrated by remember { mutableStateOf(false) }
    var calibrationMessage by remember { mutableStateOf("") }
    // New state for manual calibration trigger
    var triggerCalibration by remember { mutableStateOf(false) }

    // Sensor setup
    val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    val accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    val magnetometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

    var accelerometerValues by remember { mutableStateOf(FloatArray(3)) }
    var magnetometerValues by remember { mutableStateOf(FloatArray(3)) }

    val sensorEventListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            when (event.sensor.type) {
                Sensor.TYPE_ACCELEROMETER -> {
                    accelerometerValues = event.values.clone()
                }

                Sensor.TYPE_MAGNETIC_FIELD -> {
                    magnetometerValues = event.values.clone()
                    if (isCalibrating) {
                        calibrationData.add(magnetometerValues.clone())
                    }
                }
            }
            // Apply calibration offsets
            if (isCalibrated) {
                magnetometerValues[0] -= calibrationOffsets[0]
                magnetometerValues[1] -= calibrationOffsets[1]
                magnetometerValues[2] -= calibrationOffsets[2]
            }

            val rotationMatrix = FloatArray(9)
            val inclinationMatrix = FloatArray(9)
            val orientationAngles = FloatArray(3)

            if (SensorManager.getRotationMatrix(
                    rotationMatrix,
                    inclinationMatrix,
                    accelerometerValues,
                    magnetometerValues
                )
            ) {
                SensorManager.getOrientation(rotationMatrix, orientationAngles)
                azimuth = Math.toDegrees(orientationAngles[0].toDouble()).toFloat()
            }
        }

        override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}
    }

    //targetAngle is now a state variable
    var targetAngle by remember { mutableFloatStateOf(0f) }
    var previousSweepAngle by remember { mutableFloatStateOf(0f) }

    fun updateTargetAngle() {
        val bearing = calculateBearing(myLat, myLng, targetLat, targetLng)
        // Calculate the relative target angle based on the initial azimuth
        val relativeAngle = (bearing - azimuth + 360) % 360
        // Transform the relative angle to the canvas coordinate system
        targetAngle = relativeAngle
        //Log.d("TargetAngle", "$targetAngle")
    }

    // Sweep angle animation
    LaunchedEffect(Unit) {
        while (true) {
            previousSweepAngle = sweepAngle
            sweepAngle = (sweepAngle + 1.5f) % 360f // Increased speed
            // Detect sweep completion
            if (previousSweepAngle > sweepAngle && previousSweepAngle > 180f) {
                //updateTargetAngle()
            }
            delay(10L) // Reduced delay
        }
    }
    // Register sensor listeners only once
    LaunchedEffect(Unit) {
        sensorManager.registerListener(
            sensorEventListener,
            accelerometerSensor,
            SensorManager.SENSOR_DELAY_GAME
        )
        sensorManager.registerListener(
            sensorEventListener,
            magnetometerSensor,
            SensorManager.SENSOR_DELAY_GAME
        )
    }
    // Unregister sensor listeners when the composable is disposed
    DisposableEffect(Unit) {
        onDispose {
            sensorManager.unregisterListener(sensorEventListener)
        }
    }
    // Update target angle when location or target changes
    LaunchedEffect(myLat, myLng, targetLat, targetLng) {
        updateTargetAngle()
    }

    // Manual calibration logic
    LaunchedEffect(triggerCalibration) {
        if (triggerCalibration) {
            isCalibrating = true
            calibrationData.clear()
            calibrationMessage = "Calibrating... Move in 8 shape"
            delay(6000) // Collect data for 6 seconds
            isCalibrating = false
            if (calibrationData.isNotEmpty()) {
                val (offsets, success) = calculateCalibrationOffsets(calibrationData)
                calibrationOffsets = offsets
                isCalibrated = success
                calibrationMessage = if (success) {
                    "Compass calibrated"
                } else {
                    "Calibration failed. Move in 8 shape"
                }
            }
            triggerCalibration = false // Reset the trigger
        }
    }
    // Stop calibration when the composable is disposed
    DisposableEffect(Unit) {
        onDispose {
            isCalibrating = false
        }
    }

    Button(onClick = { triggerCalibration = true }) {
        Text(text="Calibrate Compass")
    }
    Spacer(modifier = Modifier.height(30.dp))
    RadarCanvas(modifier, targetAngle, currentDistance, sweepAngle)
    if (isCalibrating) {
        Text(text = calibrationMessage, color = Color.White)
    } else {
        Text(text = calibrationMessage, color = Color.White)
    }
}


@Composable
fun RadarCanvas(
    modifier: Modifier,
    targetAngle: Float,
    currentDistance: Float,
    sweepAngle: Float
) {
    Canvas(modifier = modifier) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        val centerX = canvasWidth / 2
        val centerY = canvasHeight / 2
        val radius = minOf(centerX, centerY) * 0.9f

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
            currentDistance in 120.0..250.0 -> radius * 0.66f
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
    val x = cos(Math.toRadians(lat1)) * sin(Math.toRadians(lat2)) - sin(Math.toRadians(lat1)) * cos(
        Math.toRadians(lat2)
    ) * cos(dLon)
    return Math.toDegrees(atan2(y, x)).toFloat()
}

fun calculateCalibrationOffsets(calibrationData: List<FloatArray>): Pair<FloatArray, Boolean> {
    val xValues = calibrationData.map { it[0] }
    val yValues = calibrationData.map { it[1] }
    val zValues = calibrationData.map { it[2] }

    val xMin = xValues.minOrNull() ?: 0f
    val xMax = xValues.maxOrNull() ?: 0f
    val yMin = yValues.minOrNull() ?: 0f
    val yMax = yValues.maxOrNull() ?: 0f
    val zMin = zValues.minOrNull() ?: 0f
    val zMax = zValues.maxOrNull() ?: 0f

    val xRange = xMax - xMin
    val yRange = yMax - yMin
    val zRange = zMax - zMin

    val threshold = 10f // Adjust this value as needed
    return if (xRange > threshold && yRange > threshold && zRange > threshold) {
        val xOffset = (xMin + xMax) / 2f
        val yOffset = (yMin + yMax) / 2f
        val zOffset = (zMin + zMax) / 2f
        Pair(floatArrayOf(xOffset, yOffset, zOffset), true)
    } else {
        Pair(floatArrayOf(0f, 0f, 0f), false)
    }
}