#include <DHT.h>
#include <SoftwareSerial.h>        //RX, TX 통신 라이브러리 추가

#define DHTPIN    6      // 아두이노의 8번 핀에 연결된 온습도 센서와 연결
#define DHTTYPE   DHT22   // DHT-22 센서의 유형으로 선택

DHT dht(DHTPIN, DHTTYPE); // DHT라이브러리를 통해 핀과 타입을 설정

SoftwareSerial Serial1(2, 3);        //RX ,TX 핀을 2, 3번 핀으로 지정
SoftwareSerial bluetooth(4, 5);

long pmcf10 = 0;
long pmcf25 = 0;
long pmcf100 = 0;
long pmat10 = 0;
long pmat25 = 0;
long pmat100 = 0;
char buf[50];

int A_CDS = A0;

void setup() {
  // put your setup code here, to run once:
  // Serial.begin(9600);
  Serial1.begin(9600);
  bluetooth.begin(9600);
  dht.begin();
}

void loop() {
  // put your main code here, to run repeatedly:

  delay(10000);
  
  int count = 0;
  unsigned char c;
  unsigned char high;
  
  Serial1.listen();
 
  while (Serial1.available()) {
    c = Serial1.read();           //RX, TX 통신을 통한 값을 c로 저장
    if ((count == 0 && c != 0x42) || (count == 1 && c != 0x4d)) {
      // Serial.println("check failed");
      break;
    }
    if (count > 15) {
      break;
    }
    else if (count == 4 || count == 6 || count == 8 || count == 10 || count == 12 || count == 14) {
      high = c;
    }
    else if (count == 5) {           //pm1.0의 수치값 계산
      pmcf10 = 256 * high + c;
      //bluetooth.print("Dust, PM1.0: ");
      bluetooth.print(pmcf10);
      bluetooth.print(",");
      // bluetooth.println(" ug/m3");
    }
    else if (count == 7) {         //pm2.5의 수치값 계산
      pmcf25 = 256 * high + c;
      //bluetooth.print("Dust, PM2.5: ");
      bluetooth.print(pmcf25);
      bluetooth.print(",");
      //bluetooth.println(" ug/m3");
    }
    else if (count == 9) {         //pm 10의 수치값 계산
      pmcf100 = 256 * high + c;
      //bluetooth.print("Dust, PM10: ");
      bluetooth.print(pmcf100);
      bluetooth.print(",");
      //bluetooth.println(" ug/m3");
    }
    count++;
  }
  int light = 1023 - analogRead(A_CDS);
    float temperature = dht.readTemperature();  // 온도 측정
    float humidity = dht.readHumidity();

    //bluetooth.print("Temperature: ");
    bluetooth.print(temperature);
    bluetooth.print(",");
    //bluetooth.println(" Celsius");

    // 시리얼 모니터에 습도 출력
    //bluetooth.print("Humidity: ");
    bluetooth.print(humidity);
    bluetooth.print(",");
    //bluetooth.println(" %RH");

    //bluetooth.print("Light: ");
    bluetooth.print(light);

    bluetooth.print("\n");
  while (Serial1.available()) Serial1.read();
}
