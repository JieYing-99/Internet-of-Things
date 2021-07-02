package com.example.greenhouse

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import java.util.*
import android.widget.Toast
import com.google.firebase.FirebaseOptions
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.ktx.app
import com.google.firebase.ktx.initialize
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    //primary database
    val databaseRef = FirebaseDatabase.getInstance().reference

    val controlPath = "PI_04_CONTROL"
    var childPath = ""
    var hourPath = ""

    val lightSensor = "light"
    var lightStatus = "optimum"
    var lightValue = 0.0

    val humiditySensor = "rand2"
    var humidityStatus = "optimum"
    var humidityValue = 0.0

    val temperatureSensor = "rand1"
    var temperatureStatus = "optimum"
    var temperatureValue = 0.0


    //TODO: THRESHOLDS
    val lightUp = 300
    val lightDown = 100

    val humidityUp = 65
    val humidityDown = 50

    val temperatureUp = 30
    val temperatureDown = 23


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //secondary database
        val options = FirebaseOptions.Builder()
                .setProjectId("iot-greenhouse-taruc")
                .setApplicationId("1:465436002355:android:860cb6e5072a37e78cbb46")
                .setApiKey("AIzaSyB7FK1T4cf1QyQ6Kqh_hM37kd6DdO1HNAk")
                .setDatabaseUrl("https://iot-greenhouse-taruc.firebaseio.com")
                .setStorageBucket("iot-greenhouse-taruc.appspot.com")
                .build()

        Firebase.initialize(this@MainActivity, options, "secondary")

        val secondary = Firebase.app("secondary")

        val secondaryDatabase = Firebase.database(secondary)
        val secondaryDatabaseRef = secondaryDatabase.reference

        setBackgroundColor()

        //TODO: LIGHT STATUS
        secondaryDatabaseRef.child(lightSensor).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                lightValue = dataSnapshot.value.toString().toDouble()
                textViewReadingLight.text = String.format("%.1f", lightValue)
                if (lightValue >= lightUp) {
                    textViewLightStatus.text = "Too high"
                    //textViewLightActuator.text = "Shades OPEN"
                } else if (lightValue < lightDown){
                    textViewLightStatus.text = "Too low"
                    //textViewLightActuator.text = "Light ON"
                } else {
                    textViewLightStatus.text = "Optimum"
                    //textViewLightActuator.text = "..."
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
                Log.e("Error", "Failed to read value.", error.toException())
            }
        })

        //TODO: HUMIDITY & TEMPERATURE STATUS
        getPath()

        var lastchild: Query = databaseRef.child(childPath).child(hourPath).orderByKey().limitToLast(1)
        lastchild.addChildEventListener(object : ChildEventListener {

            override fun onChildAdded(dataSnapshot: DataSnapshot, previousChildName: String?) {

                //TODO: ADJUST HUMIDITY
                humidityValue = dataSnapshot.child(humiditySensor).value.toString().toDouble()
                if (humidityValue > 100) {
                    humidityValue = humidityValue/10
                } else if (humidityValue < 10) {
                    humidityValue = humidityValue*10
                }
                textViewReadingHumidity.text = String.format("%.1f", humidityValue)
                if (humidityValue >= humidityUp) {
                    textViewHumidityStatus.text = "Too high"
                    //textViewHumidityActuator.text = "Windows OPEN"
                } else if (humidityValue < humidityDown){
                    textViewHumidityStatus.text = "Too low"
                    //textViewHumidityActuator.text = "Water Sprinkler ON"
                } else {
                    textViewHumidityStatus.text = "Optimum"
                    //textViewHumidityActuator.text = "..."
                }

                //TODO: ADJUST TEMPERATURE
                temperatureValue = dataSnapshot.child(temperatureSensor).value.toString().toDouble()
                if (temperatureValue > 100) {
                    temperatureValue = temperatureValue/10
                } else if (temperatureValue < 10) {
                    temperatureValue = temperatureValue*10
                }
                textViewReadingTemperature.text = String.format("%.1f", temperatureValue)
                if (temperatureValue >= temperatureUp) {
                    textViewTemperatureStatus.text = "Too high"
                    //textViewTemperatureActuator.text = "Fan ON"
                } else if (temperatureValue < temperatureDown){
                    textViewTemperatureStatus.text = "Too low"
                    //textViewTemperatureActuator.text = "Heater ON"
                } else {
                    textViewTemperatureStatus.text = "Optimum"
                    //textViewTemperatureActuator.text = "..."
                }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                System.out.println("")
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                System.out.println("")
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                System.out.println("")
            }

            override fun onCancelled(databaseError: DatabaseError) {
                System.out.println("")
            }
        })


        //TODO: LIGHT: SHADES(oled) & FLASH LIGHT
        toggleButtonLight.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                Toast.makeText(this@MainActivity, "Automatic control for light intensity is on.", Toast.LENGTH_SHORT)
                        .show()
                switchLight.isEnabled = false
                switchShades.isEnabled = false

                secondaryDatabaseRef.child(lightSensor).addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        lightValue = dataSnapshot.value.toString().toDouble()
                        System.out.println("-----------------------------------------------------")
                        Log.e("Light value", String.format("%.2f", lightValue))

                        if (lightValue >= lightUp) {
                            Log.e("Light status","Too high")
                            databaseRef.child(controlPath).child("oledsc").setValue("1")
                            secondaryDatabaseRef.child("flash").setValue("0")
                            databaseRef.child(controlPath).child("lcdtxt").setValue("==Shades  OPEN==")
                            Log.e("Shades", "Opened")
                            Log.e("Lights", "Turned on")
                            textViewLightActuator.text = "Shades OPEN"
                        } else if (lightValue < lightDown){
                            Log.e("Light status","Too low")
                            databaseRef.child(controlPath).child("oledsc").setValue("0")
                            secondaryDatabaseRef.child("flash").setValue("1")
                            databaseRef.child(controlPath).child("lcdtxt").setValue("===Lights  ON===")
                            Log.e("Shades", "Closed")
                            Log.e("Lights", "Turned on")
                            textViewLightActuator.text = "Light ON"
                        } else {
                            Log.e("Light status", "Optimum")
                            databaseRef.child(controlPath).child("oledsc").setValue("0")
                            secondaryDatabaseRef.child("flash").setValue("0")
                            databaseRef.child(controlPath).child("lcdtxt").setValue("=Optimum  light=")
                            Log.e("Shades", "Closed")
                            Log.e("Lights", "Turned off")
                            textViewLightActuator.text = "..."
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        // Failed to read value
                        Log.e("Error", "Failed to read value.", error.toException())
                    }
                })

            } else {
                Toast.makeText(this@MainActivity, "Manual control for light intensity is on.", Toast.LENGTH_SHORT)
                        .show()
                switchLight.isEnabled = true
                switchShades.isEnabled = true
            }
        }

        switchLight.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                secondaryDatabaseRef.child("flash").setValue("1")
                textViewLightActuator.text = "Light ON"
            } else {
                secondaryDatabaseRef.child("flash").setValue("0")
                textViewLightActuator.text = "..."
            }
        }

        switchShades.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                databaseRef.child(controlPath).child("oledsc").setValue("1")
                textViewLightActuator.text = "Shades OPEN"
            } else {
                databaseRef.child(controlPath).child("oledsc").setValue("0")
                textViewLightActuator.text = "..."
            }
        }


        //TODO: HUMIDITY: WINDOWS & WATER SPRINKLER (LCD BACKGROUND COLOR)
        toggleButtonHumidity.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                Toast.makeText(this@MainActivity, "Automatic control for humidity is on.", Toast.LENGTH_SHORT)
                        .show()
                switchWaterSprinkler.isEnabled = false
                switchWindows.isEnabled = false

                getPath()

                var lastchildHumidity: Query = databaseRef.child(childPath).child(hourPath).orderByKey().limitToLast(1)
                lastchildHumidity.addChildEventListener(object : ChildEventListener {

                    override fun onChildAdded(dataSnapshot: DataSnapshot, previousChildName: String?) {

                        //TODO: ADJUST HUMIDITY
                        humidityValue = dataSnapshot.child(humiditySensor).value.toString().toDouble()
                        if (humidityValue > 100) {
                            humidityValue = humidityValue/10
                        } else if (humidityValue < 10) {
                            humidityValue = humidityValue*10
                        }
                        System.out.println("-----------------------------------------------------")
                        Log.e("Humidity value", String.format("%.2f", humidityValue))

                        if (humidityValue >= humidityUp) {
                            Log.e("Humidity status", "Too high")
                            databaseRef.child(controlPath).child("lcdbkR").setValue("10")
                            databaseRef.child(controlPath).child("lcdbkG").setValue("0")
                            databaseRef.child(controlPath).child("lcdbkB").setValue("0")
                            databaseRef.child(controlPath).child("lcdtxt").setValue("==Windows OPEN==")
                            textViewHumidityActuator.text = "Windows OPEN"
                            Log.e("Windows", "Opened")
                            Log.e("Water sprinkler", "Deactivated")
                        } else if (humidityValue < humidityDown) {
                            Log.e("Humidity status", "Too low")
                            databaseRef.child(controlPath).child("lcdbkR").setValue("0")
                            databaseRef.child(controlPath).child("lcdbkG").setValue("0")
                            databaseRef.child(controlPath).child("lcdbkB").setValue("10")
                            databaseRef.child(controlPath).child("lcdtxt").setValue("==Sprinkler ON==")
                            textViewHumidityActuator.text = "Water Sprinkler ON"
                            Log.e("Windows", "Closed")
                            Log.e("Water sprinkler", "Activated")
                        } else {
                            Log.e("Humidity status", "Optimum")
                            databaseRef.child(controlPath).child("lcdbkR").setValue("0")
                            databaseRef.child(controlPath).child("lcdbkG").setValue("10")
                            databaseRef.child(controlPath).child("lcdbkB").setValue("0")
                            databaseRef.child(controlPath).child("lcdtxt").setValue("Optimum Humidity")
                            textViewHumidityActuator.text = "..."
                            Log.e("Windows", "Closed")
                            Log.e("Water sprinkler", "Deactivated")
                        }
                    }

                    override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                        System.out.println("")
                    }

                    override fun onChildRemoved(snapshot: DataSnapshot) {
                        System.out.println("")
                    }

                    override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                        System.out.println("")
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        System.out.println("")
                    }
                })

            } else {
                Toast.makeText(this@MainActivity, "Manual control for humidity is on.", Toast.LENGTH_SHORT)
                        .show()
                switchWaterSprinkler.isEnabled = true
                switchWindows.isEnabled = true
            }
        }

        switchWaterSprinkler.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                databaseRef.child(controlPath).child("lcdbkR").setValue("0")
                databaseRef.child(controlPath).child("lcdbkG").setValue("0")
                databaseRef.child(controlPath).child("lcdbkB").setValue("10")
                databaseRef.child(controlPath).child("lcdtxt").setValue("Water ON")
                textViewHumidityActuator.text = "Water Sprinkler ON"
            } else {
                databaseRef.child(controlPath).child("lcdbkR").setValue("0")
                databaseRef.child(controlPath).child("lcdbkG").setValue("10")
                databaseRef.child(controlPath).child("lcdbkB").setValue("0")
                textViewHumidityActuator.text = "..."
            }
        }

        switchWindows.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                databaseRef.child(controlPath).child("lcdbkR").setValue("10")
                databaseRef.child(controlPath).child("lcdbkG").setValue("0")
                databaseRef.child(controlPath).child("lcdbkB").setValue("0")
                textViewHumidityActuator.text = "Windows OPEN"
            } else {
                databaseRef.child(controlPath).child("lcdbkR").setValue("0")
                databaseRef.child(controlPath).child("lcdbkG").setValue("10")
                databaseRef.child(controlPath).child("lcdbkB").setValue("0")
                textViewHumidityActuator.text = "..."
            }
        }


        //TODO: TEMPERATURE: FAN & HEATER(relay1)
        toggleButtonTemperature.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                Toast.makeText(this@MainActivity, "Automatic control for temperature is on.", Toast.LENGTH_SHORT)
                        .show()
                switchHeater.isEnabled = false
                switchFan.isEnabled = false

                getPath()

                var lastchildTemperature: Query = databaseRef.child(childPath).child(hourPath).orderByKey().limitToLast(1)
                lastchildTemperature.addChildEventListener(object : ChildEventListener {

                    override fun onChildAdded(dataSnapshot: DataSnapshot, previousChildName: String?) {

                        //TODO: ADJUST TEMPERATURE
                        temperatureValue = dataSnapshot.child(temperatureSensor).value.toString().toDouble()
                        if (temperatureValue > 100) {
                            temperatureValue  = temperatureValue / 10
                        } else if (temperatureValue < 10) {
                            temperatureValue  = temperatureValue * 10
                        }
                        System.out.println("-----------------------------------------------------")
                        Log.e("Temperature value", String.format("%.2f", temperatureValue))

                        if (temperatureValue >= temperatureUp) {
                            Log.e("Temperature status", "Too high")
                            secondaryDatabaseRef.child("fan").setValue("1")
                            databaseRef.child(controlPath).child("relay1").setValue("0")
                            databaseRef.child(controlPath).child("lcdtxt").setValue("=====Fan ON=====")
                            Log.e("Fans", "Turned on")
                            Log.e("Heater", "Turned off")
                            textViewTemperatureActuator.text = "Fan ON"
                        } else if (temperatureValue < temperatureDown) {
                            Log.e("Temperature status", "Too low")
                            secondaryDatabaseRef.child("fan").setValue("0")
                            databaseRef.child(controlPath).child("relay1").setValue("1")
                            databaseRef.child(controlPath).child("lcdtxt").setValue("===Heater ON====")
                            Log.e("Fan", "Turned off")
                            Log.e("Heater", "Turned on")
                            textViewTemperatureActuator.text = "Heater ON"
                        } else {
                            Log.e("Temperature status", "Optimum")
                            secondaryDatabaseRef.child("fan").setValue("0")
                            databaseRef.child(controlPath).child("relay1").setValue("0")
                            databaseRef.child(controlPath).child("lcdtxt").setValue("==Optimum Temp==")
                            Log.e("Fan", "Turned off")
                            Log.e("Heater", "Turned off")
                            textViewTemperatureActuator.text = "..."
                        }
                    }

                    override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                        System.out.println("")
                    }

                    override fun onChildRemoved(snapshot: DataSnapshot) {
                        System.out.println("")
                    }

                    override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                        System.out.println("")
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        System.out.println("")
                    }
                })

            } else {
                Toast.makeText(this@MainActivity, "Manual control for temperature is on.", Toast.LENGTH_SHORT)
                        .show()
                switchHeater.isEnabled = true
                switchFan.isEnabled = true
            }
        }

        switchHeater.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                databaseRef.child(controlPath).child("relay1").setValue("1")
                textViewTemperatureActuator.text = "Heater ON"
            } else {
                databaseRef.child(controlPath).child("relay1").setValue("0")
                textViewTemperatureActuator.text = "..."
            }
        }

        switchFan.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                secondaryDatabaseRef.child("fan").setValue("1")
                textViewTemperatureActuator.text = "Fan ON"
            } else {
                secondaryDatabaseRef.child("fan").setValue("0")
                textViewTemperatureActuator.text = "..."
            }
        }

    }


    fun getPath() {
        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH) + 1

        var monthStr = ""
        if (month < 10) {
            monthStr = "0" + month.toString()
        } else {
            monthStr = month.toString()
        }

        val day = c.get(Calendar.DAY_OF_MONTH)
        var dayStr = ""
        if (day < 10) {
            dayStr = "0" + day.toString()
        } else {
            dayStr = day.toString()
        }

        val hour = c.get(Calendar.HOUR_OF_DAY)
        var hourStr = ""
        if (hour < 10) {
            hourStr = "0" + hour.toString()
        } else {
            hourStr = hour.toString()
        }

        childPath = "PI_04_$year$monthStr$dayStr"
        hourPath = "$hourStr"

        Log.e("Child Path", "$childPath")
        Log.e("Hour Path", "$hourPath")
    }


    fun setBackgroundColor() {
        databaseRef.child(controlPath).child("lcdbkR").setValue("10") //255
        databaseRef.child(controlPath).child("lcdbkG").setValue("10") //255
        databaseRef.child(controlPath).child("lcdbkB").setValue("10") //0
    }

}

//internetofthingstaruc@gmail.com
//iot@taruc
