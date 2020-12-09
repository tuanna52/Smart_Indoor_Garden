#include <ESP8266WiFi.h>                                                    // esp8266 library
#include "FirebaseESP8266.h"                                                // firebase library
#include <DHT.h>                                                            // dht11 temperature and humidity sensor library


#define FIREBASE_HOST "smart-garden-12b2f.firebaseio.com"                          // the project name address from firebase id
#define FIREBASE_AUTH "yzRyH6Lq8BdT8B7uhzmAU5QhZY76BgJy3oYsZL8s"            // the secret key generated from firebase

#define WIFI_SSID "EE-Hub-nU3W"                                             // input your home or public wifi name 
#define WIFI_PASSWORD "flag-may-OPTIC"                                    //password of wifi ssid
 
#define DHTPIN D2                                                           // what digital pin we're connected to
#define DHTTYPE DHT11                                                       // select dht type as DHT 11 or DHT22
DHT dht(DHTPIN, DHTTYPE);        

int digitalPin = 3;            // moisteure sensor pin
const int motorPin = D3;
int LedPin = 16;
int lightSensorPin = A0;
int analogValue = 0;

//Define FirebaseESP8266 data object
FirebaseData firebaseData;
FirebaseJson json;

int pump_status = 0;
int pump_activation = 0;
int led_status = 0;
int led_activation = 0;

unsigned long _time;

void setup() {
  Serial.begin(9600);
  delay(1000);                
  WiFi.begin(WIFI_SSID, WIFI_PASSWORD);                                     //try to connect with wifi
  Serial.print("Connecting to ");
  Serial.print(WIFI_SSID);
  while (WiFi.status() != WL_CONNECTED) {
    Serial.print(".");
    delay(500);
  }
  Serial.println();
  Serial.print("Connected to ");
  Serial.println(WIFI_SSID);
  Serial.print("IP Address is : ");
  Serial.println(WiFi.localIP());                                            //print local IP address
  Firebase.begin(FIREBASE_HOST, FIREBASE_AUTH);                              // connect to firebase
  dht.begin();    //Start reading dht sensor

  Serial.begin(9600);
  pinMode(digitalPin, INPUT);
  pinMode(LedPin, OUTPUT);
  pinMode(motorPin, OUTPUT);

  _time = millis();
}

void loop() {
  if ( (unsigned long) (millis() - _time) > 9800)
  {
    float h = dht.readHumidity();                                              // Reading temperature or humidity takes about 250 milliseconds!
    float t = dht.readTemperature(); // Read temperature as Celsius (the default)
    float m = (digitalRead(digitalPin));
    if (isnan(h) || isnan(t)) {                                                // Check if any reads failed and exit early (to try again).
      Serial.println(F("Failed to read from DHT sensor!"));
      return;
    }
  
     /*
     * High == Dry
     * Low == Wet
     */
    String firemoist = "";
    String fireStatus = "";  
    if (digitalRead(digitalPin) == HIGH){
      Serial.print("Moisture: ");  Serial.print(m);  firemoist = String("Wet");
    }
    else {
      Serial.print("Moisture: ");  Serial.print(m); firemoist = String("Dry");
    }
    
    analogValue = analogRead(lightSensorPin);
  
    Serial.print("LightIntensity: ");  Serial.print(analogValue);
    String fireLight = String(analogValue) + String("%");
    Serial.print("  Humidity: ");  Serial.print(h);
    String fireHumid = String(h) + String("%");                                         //convert integer humidity to string humidity 
    Serial.print("%  Temperature: ");  Serial.print(t);  Serial.println("°C ");
    String fireTemp = String(t) + String("°C");                                                     //convert integer temperature to string temperature
  
    json.set("temperature",t);
    json.set("humidity",h);
    json.set("moisture",firemoist);
    json.set("light",analogValue);
  
    if (Firebase.pushJSON(firebaseData, "/data", json)) {
    Serial.println(firebaseData.dataPath());
    Serial.println(firebaseData.pushName());
    Serial.println(firebaseData.dataPath() + "/"+ firebaseData.pushName());
    }
    else {
      Serial.println(firebaseData.errorReason());
    }
    
    _time = millis();
  }

  if (Firebase.getInt(firebaseData, "/pump/status")) {
    if (firebaseData.dataType() == "int") {
      pump_status = firebaseData.intData();
    }
  } else {
    Serial.println(firebaseData.errorReason());
  }

  if (Firebase.getInt(firebaseData, "/pump/activation")) {
    if (firebaseData.dataType() == "int") {
      pump_activation = firebaseData.intData();
    }
  } else {
    Serial.println(firebaseData.errorReason());
  }

  if (Firebase.getInt(firebaseData, "/led/status")) {
    if (firebaseData.dataType() == "int") {
      led_status = firebaseData.intData();
    }
  } else {
    Serial.println(firebaseData.errorReason());
  }

  if (Firebase.getInt(firebaseData, "/led/activation")) {
    if (firebaseData.dataType() == "int") {
      led_activation = firebaseData.intData();
    }
  } else {
    Serial.println(firebaseData.errorReason());
  }

  if (pump_status == 0) {
    if (digitalRead(digitalPin) == HIGH) {
      digitalWrite(motorPin, HIGH);         // tun on motor
    }
    else if (digitalRead(digitalPin) == LOW) {
      digitalWrite(motorPin, LOW);          // turn off mottor
    }
  }
  else {
    digitalWrite(motorPin, pump_activation);
  }

  if (led_status == 0) {
    if (analogValue < 50) {            
      digitalWrite(LedPin, HIGH);
    }
    else  digitalWrite(LedPin, LOW);
  }
  else {
    digitalWrite(LedPin, led_activation);
  }

  delay(200);
}
