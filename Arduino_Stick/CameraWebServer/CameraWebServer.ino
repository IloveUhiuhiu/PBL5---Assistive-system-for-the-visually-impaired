  #include "esp_camera.h"
  #include <WiFi.h>
  #include <ArduinoWebsockets.h>
  #include <HardwareSerial.h>
  #include "DFRobotDFPlayerMini.h"

  HardwareSerial dfSerial(1);
  DFRobotDFPlayerMini dfPlayer;
  bool isDFPlayerReady = false;

  uint64_t chipid = ESP.getEfuseMac(); 
  String mac = String((uint16_t)(chipid >> 32), HEX) + String((uint32_t)chipid, HEX);

  // WiFi Credentials
  String ssid = "aaasss";
  String password = "asdasd123";
  String serverUrlWS = "ws://192.168.141.117:5000"; 

  #define CAMERA_MODEL_AI_THINKER
  #include "camera_pins.h"

  using namespace websockets;
  WebsocketsClient client;

  void connectWebSocket() {
      Serial.println("Connecting WebSocket...");
      if (client.connect(serverUrlWS)) {
          Serial.println("WebSocket connected!");
          client.onMessage(getDataOnWebSocket);
      } else {
          Serial.println("WebSocket failed! Retry in 3s");
          delay(3000);
      }
  }

  void setup() {
      Serial.begin(115200);
      Serial.println();

      WiFi.begin(ssid, password);
      Serial.print("WiFi connecting");
      while (WiFi.status() != WL_CONNECTED) {
          delay(500);
          Serial.print(".");
      }
      Serial.println("\nWiFi connected!");

      camera_config_t config;
      config.ledc_channel = LEDC_CHANNEL_0;
      config.ledc_timer = LEDC_TIMER_0;
      config.pin_d0 = Y2_GPIO_NUM;
      config.pin_d1 = Y3_GPIO_NUM;
      config.pin_d2 = Y4_GPIO_NUM;
      config.pin_d3 = Y5_GPIO_NUM;
      config.pin_d4 = Y6_GPIO_NUM;
      config.pin_d5 = Y7_GPIO_NUM;
      config.pin_d6 = Y8_GPIO_NUM;
      config.pin_d7 = Y9_GPIO_NUM;
      config.pin_xclk = XCLK_GPIO_NUM;
      config.pin_pclk = PCLK_GPIO_NUM;
      config.pin_vsync = VSYNC_GPIO_NUM;
      config.pin_href = HREF_GPIO_NUM;
      config.pin_sccb_sda = SIOD_GPIO_NUM;
      config.pin_sccb_scl = SIOC_GPIO_NUM;
      config.pin_pwdn = PWDN_GPIO_NUM;
      config.pin_reset = RESET_GPIO_NUM;
      config.xclk_freq_hz = 20000000;
      config.pixel_format = PIXFORMAT_JPEG;
      config.frame_size = FRAMESIZE_HVGA;
      config.jpeg_quality = 12;
      config.fb_count = 1;
      config.fb_location = CAMERA_FB_IN_PSRAM;

      esp_err_t err = esp_camera_init(&config);
      if (err != ESP_OK) {
          Serial.printf("Camera init failed: 0x%x\n", err);
          return;
      }
      Serial.println("Camera Ready!");
      delay(2000);
      dfSerial.begin(9600, SERIAL_8N1, 13, 14);
      Serial.println("OK");
      if (dfPlayer.begin(dfSerial)) {
          Serial.println("DFPlayer connected!");
          dfPlayer.volume(25);
          isDFPlayerReady = true;
      } else {
          Serial.println("DFPlayer không kết nối được!");
      }
      Serial.println("OK");
      connectWebSocket();
      Serial.println("OK"); 
  }

  void sendImage() {
  if (!client.available()) {
          Serial.println("WebSocket lost, reconnecting...");
          connectWebSocket();
          return;
      }
      camera_fb_t *fb = esp_camera_fb_get();
      if (!fb) {
          Serial.println("Error: Cannot get photo");
          return;
      }
      client.sendBinary((const char*)fb->buf, fb->len);
      Serial.println("Send image successfully");
      esp_camera_fb_return(fb);
  }

  void getDataOnWebSocket(WebsocketsMessage message) {
      Serial.print("Server response: ");
      Serial.println(message.data());
      if (!isDFPlayerReady) {
          Serial.println("DFPlayer not ready, skipping audio playback.");
          return;
      }
      if (message.data().length() == 0) {
          Serial.println("Empty message.");
          return;
      }
      String responseData = message.data();
      int index = responseData.indexOf(' ');
      String rs1 = responseData.substring(0,index);
      String rs2 = responseData.substring(index+1);
      index = rs2.indexOf(' ');
      int file_name = rs2.substring(0,index).toInt();
      int delay_time = rs2.substring(index+1).toInt();

      if (file_name > 0 && file_name <= 255) {
          Serial.printf("Playing audio: %d\n", file_name);
          dfPlayer.play(file_name);
          delay(delay_time);
      } else {
          Serial.printf("Invalid audio number: %d\n", file_name);
      }
  }
  void loop() {
    if (client.available()) {  
          client.poll(); 
      } else {
          Serial.println("WebSocket disconnected, reconnecting...");
          connectWebSocket();
      }
    sendImage();  
    delay(100);
  }





