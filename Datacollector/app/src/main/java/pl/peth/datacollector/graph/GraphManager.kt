package pl.peth.datacollector.graph

import android.graphics.Color
import android.hardware.Sensor
import android.hardware.SensorEvent
import com.jjoe64.graphview.GraphView
import com.jjoe64.graphview.helper.StaticLabelsFormatter
import com.jjoe64.graphview.series.BarGraphSeries
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries


class GraphManager {
    private lateinit var graph: GraphView

    private var barSeries: BarGraphSeries<DataPoint> = BarGraphSeries()

    private val accelerometerX: LineGraphSeries<DataPoint> = LineGraphSeries()
    private val accelerometerY: LineGraphSeries<DataPoint> = LineGraphSeries()
    private val accelerometerZ: LineGraphSeries<DataPoint> = LineGraphSeries()

    private val gyroscopeX: LineGraphSeries<DataPoint> = LineGraphSeries()
    private val gyroscopeY: LineGraphSeries<DataPoint> = LineGraphSeries()
    private val gyroscopeZ: LineGraphSeries<DataPoint> = LineGraphSeries()

    private val lightVal: LineGraphSeries<DataPoint> = LineGraphSeries()

    private val proximityVal: LineGraphSeries<DataPoint> = LineGraphSeries()

    private var indexCounter: Double = 0.0

    constructor(target: GraphView) {
        this.graph = target
        initGraph()
    }

    private fun initGraph() {
        graph.getViewport()?.setScrollable(true)
        graph.getViewport()?.setScalable(true)
        graph.getViewport()?.setYAxisBoundsManual(true)
        graph.getViewport()?.setXAxisBoundsManual(true)

        accelerometerX.color = Color.RED
        accelerometerY.color = Color.GREEN
        accelerometerZ.color = Color.BLUE

        gyroscopeX.color = Color.RED
        gyroscopeY.color = Color.GREEN
        gyroscopeZ.color = Color.BLUE

        loadSeries(Sensor.TYPE_ACCELEROMETER)
    }

    fun resetBarSeries(sensorType: Int){
        barSeries = BarGraphSeries()
        barSeries.setSpacing(25);
        barSeries.setDrawValuesOnTop(true)
        barSeries.setValuesOnTopColor(Color.RED)
        loadSeries(sensorType)
        updateGraph()
    }

    fun addData(sensorType: Int, data: SensorEvent) {
        resetBarSeries(sensorType)

        if ((sensorType == Sensor.TYPE_ACCELEROMETER) || (sensorType == Sensor.TYPE_GYROSCOPE)) {
            barSeries.appendData(DataPoint(1.0, data.values[0].toDouble()), false, Int.MAX_VALUE)
            barSeries.appendData(DataPoint(2.0, data.values[1].toDouble()), false, Int.MAX_VALUE)
            barSeries.appendData(DataPoint(3.0, data.values[2].toDouble()), false, Int.MAX_VALUE)
        } else if ((sensorType == Sensor.TYPE_LIGHT) || (sensorType == Sensor.TYPE_PROXIMITY)) {
            barSeries.appendData(DataPoint(1.0, data.values[0].toDouble()), false, Int.MAX_VALUE)
        }

        updateGraph()

        /*
          when(sensorType){
                Sensor.TYPE_LIGHT -> {
                    lightVal.appendData(DataPoint(indexCounter.toDouble(), data.values[0].toDouble()), true, Int.MAX_VALUE)
                }
                Sensor.TYPE_PROXIMITY -> {
                    proximityVal.appendData(DataPoint(indexCounter.toDouble(), data.values[0].toDouble()), true, Int.MAX_VALUE)
                }
                Sensor.TYPE_GYROSCOPE -> {
                    gyroscopeX.appendData(DataPoint(indexCounter.toDouble(), data.values[0].toDouble()), true, Int.MAX_VALUE)
                    gyroscopeY.appendData(DataPoint(indexCounter.toDouble(), data.values[1].toDouble()), true, Int.MAX_VALUE)
                    gyroscopeZ.appendData(DataPoint(indexCounter.toDouble(), data.values[2].toDouble()), true, Int.MAX_VALUE)
                }
                Sensor.TYPE_ACCELEROMETER -> {
                    accelerometerX.appendData(DataPoint(indexCounter.toDouble(), data.values[0].toDouble()), true, Int.MAX_VALUE)
                    accelerometerY.appendData(DataPoint(indexCounter.toDouble(), data.values[1].toDouble()), true, Int.MAX_VALUE)
                    accelerometerZ.appendData(DataPoint(indexCounter.toDouble(), data.values[2].toDouble()), true, Int.MAX_VALUE)
                }
            }

            indexCounter += 1.0
        * */
    }

    private fun updateGraph() {
        graph.onDataChanged(true, true)
    }

    fun loadSeries(sensorType: Int) {
        updateGraphSettings(sensorType)
        resetSeries()
        graph.addSeries(barSeries)

        /*
        when (sensorType) {
            Sensor.TYPE_ACCELEROMETER -> {
                //graph.addSeries(accelerometerX)
                //graph.addSeries(accelerometerY)
                //graph.addSeries(accelerometerZ)
            }
            Sensor.TYPE_PROXIMITY -> {
                graph.addSeries(proximityVal)
            }
            Sensor.TYPE_LIGHT -> {
                graph.addSeries(lightVal)
            }
            Sensor.TYPE_GYROSCOPE -> {
                graph.addSeries(gyroscopeX)
                graph.addSeries(gyroscopeY)
                graph.addSeries(gyroscopeZ)
            }
        }
        */
    }

    private fun updateGraphSettings(sensorType: Int) {
        val staticLabelsFormatter = StaticLabelsFormatter(graph)

        if ((sensorType == Sensor.TYPE_ACCELEROMETER) || (sensorType == Sensor.TYPE_GYROSCOPE)) {
            graph.getViewport()?.setMinY(-15.0)
            graph.getViewport()?.setMaxY(15.0)
            graph.getViewport()?.setMinX(1.0)
            graph.getViewport()?.setMaxX(3.0)

            staticLabelsFormatter.setHorizontalLabels(arrayOf("X", "Y", "Z"))
        } else if ((sensorType == Sensor.TYPE_LIGHT)) {
            graph.getViewport()?.setMinY(-1000.0)
            graph.getViewport()?.setMaxY(1000.0)
            graph.getViewport()?.setMinX(1.0)
            graph.getViewport()?.setMaxX(1.0)

            staticLabelsFormatter.setHorizontalLabels(arrayOf("X", ""))
        }else if ((sensorType == Sensor.TYPE_PROXIMITY)){
            graph.getViewport()?.setMinY(-10.0)
            graph.getViewport()?.setMaxY(10.0)
            graph.getViewport()?.setMinX(1.0)
            graph.getViewport()?.setMaxX(1.0)

            staticLabelsFormatter.setHorizontalLabels(arrayOf("X", ""))
        }

        graph.getGridLabelRenderer().setLabelFormatter(staticLabelsFormatter);
        updateGraph()
    }

    private fun resetSeries() {
        graph.removeAllSeries()
        updateGraph()
    }
}