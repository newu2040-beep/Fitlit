package com.example.util

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class LiveStepState(
    val liveStepsToday: Int = 0,
    val liveCadenceSpm: Int = 0, // Steps per minute
    val liveDistanceKm: Float = 0f,
    val liveCaloriesBurned: Int = 0,
    val isHardwareSensorAvailable: Boolean = false,
    val isHardwareSensorActive: Boolean = false,
    val isSimulationActive: Boolean = false,
    val sensorName: String = "Internal Accelerometer / Pedometer",
    val lastStepTimestamp: Long = System.currentTimeMillis()
)

class RealtimeStepTracker(
    private val context: Context,
    private val scope: CoroutineScope,
    private val onStepCountUpdated: (newTotalSteps: Int, deltaSteps: Int) -> Unit
) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private val stepCounterSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
    private val stepDetectorSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)

    private val _stepState = MutableStateFlow(
        LiveStepState(
            isHardwareSensorAvailable = stepCounterSensor != null || stepDetectorSensor != null,
            sensorName = stepCounterSensor?.name ?: stepDetectorSensor?.name ?: "Pedometer Core Engine"
        )
    )
    val stepState: StateFlow<LiveStepState> = _stepState.asStateFlow()

    private var initialHardwareSteps: Float? = null
    private var simulationJob: Job? = null
    private var recentStepTimestamps = mutableListOf<Long>()

    fun startTracking(baseDatabaseSteps: Int) {
        // Initialize base steps from database
        val distKm = baseDatabaseSteps * 0.00078f
        val cals = (baseDatabaseSteps * 0.042f).toInt()
        
        _stepState.value = _stepState.value.copy(
            liveStepsToday = baseDatabaseSteps,
            liveDistanceKm = distKm,
            liveCaloriesBurned = cals
        )

        sensorManager?.let { sm ->
            var registered = false
            stepCounterSensor?.let { sensor ->
                registered = sm.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI) || registered
            }
            stepDetectorSensor?.let { sensor ->
                registered = sm.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI) || registered
            }

            _stepState.value = _stepState.value.copy(
                isHardwareSensorActive = registered
            )
        }
    }

    fun resetSteps() {
        stopSimulation()
        recentStepTimestamps.clear()
        initialHardwareSteps = null
        _stepState.value = _stepState.value.copy(
            liveStepsToday = 0,
            liveCadenceSpm = 0,
            liveDistanceKm = 0f,
            liveCaloriesBurned = 0,
            lastStepTimestamp = System.currentTimeMillis()
        )
    }

    fun stopTracking() {
        sensorManager?.unregisterListener(this)
        stopSimulation()
        _stepState.value = _stepState.value.copy(
            isHardwareSensorActive = false
        )
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event ?: return
        val now = System.currentTimeMillis()
        
        if (event.sensor.type == Sensor.TYPE_STEP_COUNTER) {
            val totalSensorSteps = event.values[0]
            if (initialHardwareSteps == null) {
                initialHardwareSteps = totalSensorSteps
            }
            val deltaFromSensor = (totalSensorSteps - (initialHardwareSteps ?: totalSensorSteps)).toInt()
            if (deltaFromSensor > 0) {
                recordStepIncrement(1, now)
                initialHardwareSteps = totalSensorSteps
            }
        } else if (event.sensor.type == Sensor.TYPE_STEP_DETECTOR) {
            if (event.values[0] == 1.0f) {
                recordStepIncrement(1, now)
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    fun recordManualWalkStepIncrement(count: Int = 1) {
        val now = System.currentTimeMillis()
        recordStepIncrement(count, now)
    }

    private fun recordStepIncrement(delta: Int, timestamp: Long) {
        recentStepTimestamps.add(timestamp)
        // Keep only steps within last 10 seconds to compute Cadence SPM
        val tenSecondsAgo = timestamp - 10000L
        recentStepTimestamps.removeAll { it < tenSecondsAgo }

        val currentSpm = if (recentStepTimestamps.size > 1) {
            (recentStepTimestamps.size * 6).coerceIn(40, 180)
        } else {
            0
        }

        val updatedSteps = _stepState.value.liveStepsToday + delta
        val updatedDist = updatedSteps * 0.00078f
        val updatedCals = (updatedSteps * 0.042f).toInt()

        _stepState.value = _stepState.value.copy(
            liveStepsToday = updatedSteps,
            liveCadenceSpm = currentSpm,
            liveDistanceKm = updatedDist,
            liveCaloriesBurned = updatedCals,
            lastStepTimestamp = timestamp
        )

        onStepCountUpdated(updatedSteps, delta)
    }

    fun toggleLiveWalkSimulation() {
        if (_stepState.value.isSimulationActive) {
            stopSimulation()
        } else {
            startSimulation()
        }
    }

    private fun startSimulation() {
        simulationJob?.cancel()
        _stepState.value = _stepState.value.copy(isSimulationActive = true)
        simulationJob = scope.launch(Dispatchers.Default) {
            while (isActive) {
                // Simulate natural walking cadence: ~110-120 steps per minute (~520ms per step)
                delay(540L)
                recordStepIncrement(1, System.currentTimeMillis())
            }
        }
    }

    private fun stopSimulation() {
        simulationJob?.cancel()
        simulationJob = null
        _stepState.value = _stepState.value.copy(
            isSimulationActive = false,
            liveCadenceSpm = 0
        )
    }
}
