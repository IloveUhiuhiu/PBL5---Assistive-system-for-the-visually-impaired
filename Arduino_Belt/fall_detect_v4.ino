#include <Arduino.h>
#include <Adafruit_MPU6050.h>
#include <Adafruit_Sensor.h>
#include <Wire.h>
#include <WiFi.h>
#include <HTTPClient.h>
#include <TinyGPS++.h>
#include <HardwareSerial.h>

#define GPS_RX 16   // RX của ESP32 (Kết nối TX của GPS)
#define GPS_TX 17  // TX của ESP32 (Kết nối RX của GPS)

TinyGPSPlus gps;
HardwareSerial gpsSerial(2);
uint64_t chipid = ESP.getEfuseMac();
String mac = String((uint16_t)(chipid >> 32), HEX) + String((uint32_t)chipid, HEX);
String ssid = "aaasss";
String password = "asdasd123";
String urlUpdateGPS = "http://192.168.14.28:8888/updategps/" + mac;
String urlDetectFall = "http://192.168.14.28:8888/detectfall/" + mac;

#include <vector>
#include <algorithm>
#include <numeric>
#include <cmath>

#include "linear_svc.h"
#include "minmax_scaler.h"
#include "extract_features.h"
using namespace std;

Adafruit_MPU6050 mpu;
MinMaxScaler scaler;
LinearSVC svm_model;
#define WINDOW_SIZE 60  // 20 mẫu x 3 trục
int buffer_index = 0;
int number_of_sample = 0;
bool is_first = true;

vector<float> ax_buffer(WINDOW_SIZE), ay_buffer(WINDOW_SIZE), az_buffer(WINDOW_SIZE);
vector<float> gx_buffer(WINDOW_SIZE), gy_buffer(WINDOW_SIZE), gz_buffer(WINDOW_SIZE);

const int feature_dim = 10;

unsigned long lastDetectFall = 0;   // lưu thời gian trước đó
const long intervalDetectFall = 50;            // khoảng thời gian để chờ đợi (50 ms)

unsigned long lastSendGPS = 0;
const long intervalSendGPS = 10000;

void sendGPSData() {
    while (gpsSerial.available() > 0) {
        gps.encode(gpsSerial.read());
    }

    if (gps.location.isUpdated()) {
        float latitude = gps.location.lat();
        float longitude = gps.location.lng();

        String jsonPayload = "{\"latitude\": " + String(latitude, 7) + 
                             ", \"longitude\": " + String(longitude, 7) + "}";
        Serial.println("Sending GPS data: ");
        Serial.println(jsonPayload);

        HTTPClient http;
        http.begin(urlUpdateGPS);
        http.setTimeout(3000);
        http.addHeader("Content-Type", "application/json");

        int httpResponseCode = http.PUT(jsonPayload);
        if (httpResponseCode > 0) {
            Serial.println("GPS Data sent successfully");
        } else {
            Serial.printf("Failed to send GPS. HTTP error: %d\n", httpResponseCode);
        }
        http.end();
    } else {
        Serial.println("No valid GPS signal yet...");
    }
}

void sendDetectFall() {
    HTTPClient http;
    http.begin(urlDetectFall);
    http.setTimeout(3000);
    http.addHeader("Content-Type", "application/json");
    String jsonPayload = "{\"message\": \"fall\"}";
    int httpResponseCode = http.PUT(jsonPayload);
    if (httpResponseCode > 0) {
        Serial.println("Detect Fall Data sent successfully");
    } else {
        Serial.printf("Failed to send Detect Fall. HTTP error: %d\n", httpResponseCode);
    }
    http.end();
}

void reconnectWiFi() {
  if (WiFi.status() != WL_CONNECTED) {
    Serial.println("WiFi lost. Reconnecting...");
    WiFi.begin(ssid, password);
    unsigned long startAttempt = millis();
    while (WiFi.status() != WL_CONNECTED && millis() - startAttempt < 10000) {
      delay(500);
      Serial.print(".");
    }
    if (WiFi.status() == WL_CONNECTED) {
      Serial.println("Reconnected to WiFi.");
    } else {
      Serial.println("Failed to reconnect.");
    }
  }
}

void setup() {
  Serial.begin(115200);
  delay(1000);

  
  WiFi.begin(ssid, password);
  Serial.print("WiFi connecting");

  while (WiFi.status() != WL_CONNECTED) {
        delay(500);
        Serial.print(".");
  }
  Serial.println("\nWiFi connected!");
  
  gpsSerial.begin(9600, SERIAL_8N1, GPS_RX, GPS_TX); 


  if (!mpu.begin()) {
    Serial.println("Không tìm thấy MPU6050!");
    while (1);
  }
  mpu.setAccelerometerRange(MPU6050_RANGE_8_G);
  mpu.setGyroRange(MPU6050_RANGE_250_DEG);
  mpu.setFilterBandwidth(MPU6050_BAND_21_HZ);

  Serial.println("MPU6050 sẵn sàng!");
}

void loop() {
  sensors_event_t a, g, temp;

  unsigned long currentMillis = millis();  // lấy thời gian hiện tại
  if (currentMillis - lastDetectFall >= intervalDetectFall) {
    mpu.getEvent(&a, &g, &temp);
    Serial.println(urlDetectFall);
    ax_buffer[buffer_index] = a.acceleration.x ;
    ay_buffer[buffer_index] = a.acceleration.y ;
    az_buffer[buffer_index] = a.acceleration.z ;
    gx_buffer[buffer_index] = g.gyro.x ;
    gy_buffer[buffer_index] = g.gyro.y ;
    gz_buffer[buffer_index] = g.gyro.z ;
    Serial.print("Giá trị cảm biến: ");
    Serial.print(String(a.acceleration.x) + ", "); 
    Serial.print(String(a.acceleration.y) + ", "); 
    Serial.print(String(a.acceleration.z) + ", "); 
    Serial.print(String(g.gyro.x) + ", "); 
    Serial.print(String(g.gyro.y) + ", "); 
    Serial.print(String(g.gyro.z)); 
    Serial.println();

    buffer_index++;
    buffer_index %= WINDOW_SIZE;

    number_of_sample++;

    if (number_of_sample >= WINDOW_SIZE/2) {
      if (!is_first) {
        vector<float> features;

        extract_all_features(ax_buffer,ay_buffer,az_buffer,gx_buffer,gy_buffer,gz_buffer, buffer_index, features);
        features = scaler.transform(features);

        int prediction = svm_model.predict(features);

        if (prediction == 1) {
          sendDetectFall();
        }
        Serial.print("Dự đoán: ");
        Serial.println(prediction == 1 ? "Té ngã" : "Không té ngã");

        Serial.print("Features: ");
        for (float f : features) {
          Serial.print(f); Serial.print(" ");
        }
        Serial.println();

        buffer_index = 0;
      }
      is_first = false;
      number_of_sample = 0;
    }
    lastDetectFall = currentMillis;
  }

  if (currentMillis - lastSendGPS >= intervalSendGPS) {
    sendGPSData();
    lastSendGPS = currentMillis;
  }
}



