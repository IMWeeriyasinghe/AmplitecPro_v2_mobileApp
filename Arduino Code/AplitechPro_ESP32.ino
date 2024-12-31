#include "BluetoothSerial.h"

BluetoothSerial SerialBT;  // Initialize Bluetooth Serial object

// Define pins for audio input and output
const int audioInputPin = 36;
const int audioOutputPin = 25;

int volume = 50; // Default volume (0-100)
String receivedData = ""; // Buffer for incoming data

// Define ADC pin for the battery voltage divider
#define BATTERY_PIN 34  // Pin where the battery voltage is connected through the voltage divider

// Voltage divider constants
const float FULL_BATTERY_VOLTAGE = 4.2;   // Maximum battery voltage (fully charged)
const float EMPTY_BATTERY_VOLTAGE = 2.0; // Minimum battery voltage (fully discharged)

// Voltage divider ratio for 47 kΩ (R1) and 10 kΩ (R2)
const float VOLTAGE_DIVIDER_RATIO = (10.0 + 47.0) / 10.0; // Ratio: (R1 + R2) / R2 = 5.7

// Timing for battery status update
unsigned long previousMillis = 0; // Store the last time battery status was sent
const long interval = 5000; // Interval for sending battery data (1 minute)

void setup() {
  Serial.begin(115200);           // Initialize Serial Monitor
  SerialBT.begin("Amplitech PRO V2 Hearing Aid"); // Bluetooth device name
  pinMode(audioOutputPin, OUTPUT); // Set audio output pin as output
  pinMode(BATTERY_PIN, INPUT);    // Set battery pin as input
}

void loop() {
  unsigned long currentMillis = millis();

  // Check if Bluetooth data is available for volume control
  while (SerialBT.available()) {
    char receivedChar = SerialBT.read(); // Read one character at a time
    if (receivedChar == '\n') { // End of message
      volume = receivedData.toInt(); // Convert to integer
      volume = constrain(volume, 0, 100); // Constrain between 0 and 100
      Serial.print("Volume set to: ");
      Serial.println(volume);
      receivedData = ""; // Clear the buffer
    } else {
      receivedData += receivedChar; // Append character to buffer
    }
  }

  // Process audio signal for volume control
  int audioSignal = analogRead(audioInputPin);

  // Scale audio signal based on volume level
  int scaledSignal = map(audioSignal, 0, 4095, 0, 255); // Map input signal to 0-255 range
  scaledSignal = scaledSignal * volume / 100; // Scale based on volume (0-100)

  // Ensure the scaled signal is within the DAC's 0-255 range
  scaledSignal = constrain(scaledSignal, 0, 255);

  // Write to DAC output
  dacWrite(audioOutputPin, scaledSignal); // Output the processed audio signal
  delayMicroseconds(5); // Delay for stabilization

  // If it's time to send battery status
  if (currentMillis - previousMillis >= interval) {
    previousMillis = currentMillis; // Update the last time we sent the battery data

    // Read raw ADC value from the battery voltage pin
    int rawADC = analogRead(BATTERY_PIN);

    // Convert raw ADC value to actual battery voltage
    float batteryVoltage = (rawADC / 4095.0) * 3.3 * VOLTAGE_DIVIDER_RATIO;

    // Map battery voltage to percentage (3.0V = 0%, 4.2V = 100%)
    int batteryPercentage = (int)((batteryVoltage - EMPTY_BATTERY_VOLTAGE) / 
                                   (FULL_BATTERY_VOLTAGE - EMPTY_BATTERY_VOLTAGE) * 100);

    // Ensure percentage is between 0% and 100%
    batteryPercentage = constrain(batteryPercentage, 0, 100);

    // Print battery voltage and percentage to Serial Monitor
    Serial.print("Battery Voltage: ");
    Serial.print(batteryVoltage, 2); // 2 decimal places
    Serial.print(" V, Battery Percentage: ");
    Serial.print(batteryPercentage);
    Serial.println(" %");

    // Send the battery percentage and volume over Bluetooth
    SerialBT.print("Battery:");
    SerialBT.print(batteryPercentage);
    SerialBT.print("%, Volume:");
   SerialBT.print(volume);
    SerialBT.println("%");
  }
}