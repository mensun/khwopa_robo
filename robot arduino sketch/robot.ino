#include <Adafruit_CC3000.h>
#include <SPI.h>
#include "utility/debug.h"
#include "utility/socket.h"

// These are the interrupt and control pins
#define ADAFRUIT_CC3000_IRQ   3  // MUST be an interrupt pin!
// These can be any two pins
#define ADAFRUIT_CC3000_VBAT  5
#define ADAFRUIT_CC3000_CS    10
// Use hardware SPI for the remaining pins
// On an UNO, SCK = 13, MISO = 12, and MOSI = 11
Adafruit_CC3000 cc3000 = Adafruit_CC3000(ADAFRUIT_CC3000_CS, ADAFRUIT_CC3000_IRQ, ADAFRUIT_CC3000_VBAT,
                                         SPI_CLOCK_DIVIDER); // you can change this clock speed

#define WLAN_SSID       "robot"           // cannot be longer than 32 characters!
#define WLAN_PASS       "12345678"
// Security can be WLAN_SEC_UNSEC, WLAN_SEC_WEP, WLAN_SEC_WPA or WLAN_SEC_WPA2
#define WLAN_SECURITY   WLAN_SEC_WPA2

#define LISTEN_PORT           23    // What TCP port to listen on for connections.

#define M10 40
#define M11 41
#define M20 42
#define M21 43



#define maxspeedleft 790
#define minspeedleft 400
#define maxspeedright 790
#define minspeedright 400


#define PWMPIN1 45
#define PWMPIN2 46


Adafruit_CC3000_Server robotServer(LISTEN_PORT);
int leftspeed=0,rightspeed=0;
int lspeed=0,rspeed=0;
int speed = 0;

bool forwardflag=true;
bool reverseflag=false;

void setup(void)
{
  TCCR5A = 0b10100010;  //For 20KHz pwm signal
  TCCR5B = 0b00011001;
  ICR5 = 0x031f;
  pinMode(M10, OUTPUT);
  pinMode(M11, OUTPUT);
  pinMode(M20, OUTPUT);
  pinMode(M21, OUTPUT);
  pinMode(PWMPIN1, OUTPUT);
  pinMode(PWMPIN2, OUTPUT);
  
    

  Serial.begin(115200);
 
  /* Initialise the module */
  Serial.println(F("\nInitializing..."));
  if (!cc3000.begin())
  {
    Serial.println(F("Couldn't begin()! Check your wiring?"));
    while(1);
  }
  
  if(!cc3000.setStaticIPAddress(cc3000.IP2U32(192,168,43,5),cc3000.IP2U32(255,255,255,0),cc3000.IP2U32(192,168,43,1),cc3000.IP2U32(8,8,8,8)))
  {
    delay(100); // ToDo: Insert a DHCP timeout!
    Serial.println("Failed to Set Ip address");
  }  
  

  if (!cc3000.connectToAP(WLAN_SSID, WLAN_PASS, WLAN_SECURITY)) {
    Serial.println(F("Failed!"));
    while(1);
  }
   
  Serial.println(F("Connected!"));
  

 
  
  // Start listening for connections
  robotServer.begin();
  
  Serial.println(F("Listening for connections..."));

}

void loop(void)
{
  
  //Serial.print(leftspeed);Serial.println(rightspeed);
  
  // Try to get a client which is connected.
  Adafruit_CC3000_ClientRef client = robotServer.available();
  if (client) {
     // Check if there is data available to read.
     if (client.available() > 0) {
       // Read a byte and write it to all clients.
       
       
       uint8_t ch = client.read();
       

       switch(ch)
       {
         case 0x00://stop
           setstop();
           
           Serial.print("STOP");
           break;
         case 0x01: //forward
                      
           break;
         case 0x02://reverse
           
           break;
         case 0x03://Brake
           setbrake();
           Serial.print("Brake");
           break;
         case 0x04:
           analogWrite(PWMPIN1,leftspeed);
           analogWrite(PWMPIN2,rightspeed);
           break;
         
         case 0x05:
           analogWrite(PWMPIN1,550);
           analogWrite(PWMPIN2,550);
           setsharpright();         
           break;
         case 0x06:
           analogWrite(PWMPIN1,550);
           analogWrite(PWMPIN2,550);
           setsharpleft();
           break;
         
         case 0x0A:
         case 0x0B:
         case 0x0C:
         case 0x0D:
         case 0x0E:
         case 0x0F:
         case 0x10:
         case 0x11:
         case 0x12:
         case 0x13:
         case 0x14:
         
           leftspeed=(int)((ch -10) *(maxspeedleft -minspeedleft)* 0.1) + minspeedleft;
           rightspeed=(int)((ch -10) *(maxspeedleft -minspeedright)* 0.1) + minspeedright;
           //Serial.println(ch);
           
           speed=(leftspeed+rightspeed)/2;
           analogWrite(PWMPIN1,leftspeed);
           analogWrite(PWMPIN2,rightspeed);
           
           forwardflag=true;
           reverseflag=false;
           setforward();
           break;
        
         
         case 0x16:
         case 0x17:
         case 0x18:
         case 0x19:
         case 0x1A:
         case 0x1B:
         case 0x1C:
         case 0x1D:
         case 0x1E:
         reverseflag=true;
         forwardflag=false;
           leftspeed=(int)((ch -20) *(maxspeedleft -minspeedleft)* 0.1) + minspeedleft;
           rightspeed=(int)((ch -20) *(maxspeedright -minspeedright)* 0.1) + minspeedright;
           
           analogWrite(PWMPIN1,leftspeed);
           analogWrite(PWMPIN2,rightspeed);
           speed= (leftspeed+rightspeed)/2;

           setreverse();
           
           break;
         case 0x1F://Left
         case 0x20:
         case 0x21:
         case 0x22:
         case 0x23:
         case 0x24:
         case 0x25:
         case 0x26:
         case 0x27:
         case 0x28:
         
           if(forwardflag==true)
           setforward();
           else
           setreverse();
             //leftspeed=(int)((ch -30) *(speed -minspeedleft)* 0.1) + minspeedleft;
            //rightspeed=leftspeed - (int)((ch - 30) *(leftspeed)* 0.1);
           
           lspeed=0;
           rspeed = 700;
           
           analogWrite(PWMPIN1,rspeed);
           analogWrite(PWMPIN2,lspeed);
           

             
             
           break;
         
         case 0x29://Right
         case 0x2A:
         case 0x2B:
         case 0x2C:
         case 0x2D:
         case 0x2E:
         case 0x2F:
         case 0x30:
         case 0x31:
         case 0x32:
         
           
           if(forwardflag==true)
           setforward();
           else
           setreverse();
           
           rspeed=0;
           lspeed =700;
           analogWrite(PWMPIN1,rspeed);
           analogWrite(PWMPIN2,lspeed);
           

           //rightspeed=(int)((ch -40) *(speed -minspeedright)* 0.1) + minspeedright;
           //leftspeed= rightspeed - (int)((ch -40) *(rightspeed)* 0.1);
           
           //Serial.println("RIGHT");
           //Serial.println(ch);
           break;
         default:
           Serial.println(ch);
           break;
       }
       
       
     }
  }
}


void setforward()
{
    digitalWrite(M10,0);
    digitalWrite(M11,1);
    digitalWrite(M20,0);
    digitalWrite(M21,1); 
    
}

void setreverse()
{
    digitalWrite(M10,1);
    digitalWrite(M11,0);
    digitalWrite(M20,1);
    digitalWrite(M21,0); 
    
}

void setsharpleft()
{
    digitalWrite(M10,0);
    digitalWrite(M11,1);
    digitalWrite(M20,1);
    digitalWrite(M21,0); 
  
}

void setsharpright()
{
    digitalWrite(M10,1);
    digitalWrite(M11,0);
    digitalWrite(M20,0);
    digitalWrite(M21,1); 

}

void setbrake()
{

    digitalWrite(M10,1);
    digitalWrite(M11,1);
    digitalWrite(M20,1);
    digitalWrite(M21,1); 
    
}

void setstop()
{
    digitalWrite(M10,0);
    digitalWrite(M11,0);
    digitalWrite(M20,0);
    digitalWrite(M21,0);
   
          
}


/**************************************************************************/
/*!
    @brief  Tries to read the IP address and other connection details
*/
/**************************************************************************/
bool displayConnectionDetails(void)
{
  uint32_t ipAddress, netmask, gateway, dhcpserv, dnsserv;
  
  if(!cc3000.getIPAddress(&ipAddress, &netmask, &gateway, &dhcpserv, &dnsserv))
  {
    Serial.println(F("Unable to retrieve the IP Address!\r\n"));
    return false;
  }
  else
  {
    Serial.print(F("\nIP Addr: ")); cc3000.printIPdotsRev(ipAddress);
    Serial.print(F("\nNetmask: ")); cc3000.printIPdotsRev(netmask);
    Serial.println();
    return true;
  }
}

