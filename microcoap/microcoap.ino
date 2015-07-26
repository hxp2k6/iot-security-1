                                                     /*
* WARNING - UDP_TX_PACKET_MAX_SIZE is hardcoded by Arduino to 24 bytes
* This limits the size of possible outbound UDP packets
*/

#include <SPI.h>
#include <Ethernet.h>
#include <stdint.h>
#include <EthernetUdp.h>
#include "ArduinoJson.h"
#include "coap.h"
#include <Time.h>

char debug[1500];

#define PORT 5683
static uint8_t mac[] = {0x90, 0xA2, 0xDA, 0x0E, 0x0C, 0xFF};

EthernetClient client;
EthernetUDP udp;
uint8_t packetbuf[256];
uint8_t printbuf[256];
static uint8_t scratch_raw[32];
static coap_rw_buffer_t scratch_buf = {scratch_raw, sizeof(scratch_raw)};

IPAddress ip(192,168,1,241); //<<<<IP hardcoded

void setup()
{
    int i;
    Serial.begin(9600);
    while (!Serial) 
    {
        ; // wait for serial port to connect. Needed for Leonardo only
    }

    // start the Ethernet connection:
    Ethernet.begin(mac, ip);
    /*if (Ethernet.begin(mac) == 0)
    {
        Serial.println("Failed to configure Ethernet using DHCP");
        while(1);
    }*/
    Serial.print("My IP address: ");
    for (i=0;i<4;i++)
    {
        Serial.print(Ethernet.localIP()[i], DEC);
        
        Serial.print("."); 
    }
    Serial.println();
    //Serial.println(time);
    Serial.println();
    udp.begin(PORT);

    coap_setup();
    endpoint_setup();
}

void udp_send(const uint8_t *buf, int buflen)
{
    udp.beginPacket(udp.remoteIP(), udp.remotePort());
    while(buflen--)
        udp.write(*buf++);
    udp.endPacket();
}

void loop()
{
    int sz;
    int rc;
    coap_packet_t pkt;
    int i;
    
    String string;
    char c;
    char json[256];
    boolean pastHeaderFlag = 0;
    boolean jsonOptionFlag = 0;
    StaticJsonBuffer<256> jsonBuffer;
    
    if ((sz = udp.parsePacket()) > 0)
    {
        udp.read(packetbuf, sizeof(packetbuf));

        for (i=0;i<sz;i++)
        {  
            Serial.print(packetbuf[i], HEX);
            Serial.print(" ");
        }
     
        Serial.print(" ");
       
        
        //setTime(1357041600);
        Serial.print("now ");
        time_t time = now();
        Serial.println(time);
        Serial.print("day ");
        Serial.println(day());
        Serial.print("hour ");
        Serial.println(hour());
        Serial.print("minute ");
        Serial.println(minute());
        Serial.print("second ");
        Serial.println(second());
        /*Serial.print("Time now: ");
        Serial.println(time);
        // time = hour();
        Serial.print("Hour: ");
        Serial.println(hour(time));
        Serial.println(hour());
        Serial.print("Day: ");
        Serial.println(day());*/

        
        if (0 != (rc = coap_parse(&pkt, packetbuf, sz)))
        {
            Serial.print("Bad packet rc=");
            Serial.println(rc, DEC);
        }
        else
        {
            size_t rsplen = sizeof(packetbuf);
            coap_packet_t rsppkt;
            Serial.print(debug);

            //Serial.print("SIZE: ");
            //Serial.println(sz);
            
            string = "";
            // Esse + 3 
            int abreCounter = 0;
            int fechaCounter = 0;
            for (i=0;i<sz;i++){
              c = packetbuf[i];
              if(c == '{'){
                abreCounter++;
                if(packetbuf[i + 1] == '"'){
                  pastHeaderFlag = 1;
                  jsonOptionFlag = 1;
                }
              }

              if(pastHeaderFlag){
                string = string + c;
              }
              
              if(c == '}'){
                fechaCounter++;
                if(abreCounter == fechaCounter){
                  pastHeaderFlag = 0;
                }
              }
            }
            
            //Serial.println(string);
            
            if(jsonOptionFlag){
            
              //strncpy(json, string.c_str(), sizeof(json));
              //json[sizeof(json) - 1] = 0;
              //JsonObject& root = jsonBuffer.parseObject(json);
              
              string.toCharArray(json, sizeof(json));
              JsonObject& root = jsonBuffer.parseObject(json);
              Serial.print("\n");
              root.prettyPrintTo(Serial);
              Serial.println("\n");
              
              
              const char* id = root["ID"];
              Serial.print("ID : ");
              Serial.println(id);
              const char* ii = root["II"];
              Serial.print("II : ");
              Serial.println(ii);
              const char* is = root["IS"];
              Serial.print("IS : ");
              Serial.println(is);
              const char* sk = root["SK"];
              Serial.print("SK : ");
              Serial.println(sk);
              const char* nb = root["ST"]["OB"]["NB"];
              Serial.print("NB : ");
              Serial.println(nb);
              const char* na = root["ST"]["OB"]["NA"];
              Serial.print("NA : ");
              Serial.println(na);
              const char* act = root["ST"]["ACT"];
              //const char* act = root["ACT"];
              Serial.print("ACT : ");
              Serial.println(act);
              const char* res = root["ST"]["RES"];
              //const char* res = root["RES"];
              Serial.print("RES : ");
              Serial.println(res);

              
            }
            else{
            Serial.println("Sem JSON-------------------------------------------------");
            }
            

        //}
            

            coap_handle_req(&scratch_buf, &pkt, &rsppkt);
            coap_dumpPacket(&pkt, debug);

            //memset(packetbuf, 0, UDP_TX_PACKET_MAX_SIZE);
            if (0 != (rc = coap_build(packetbuf, &rsplen, &rsppkt)))
            {
                Serial.print("coap_build failed rc=");
                Serial.println(rc, DEC);
            }
            else
            {
                udp_send(packetbuf, rsplen);
                //Serial.println("Sending response");     
            }
        }
    }
}


