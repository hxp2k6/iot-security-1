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

char debug[300];

#define PORT 5683
static uint8_t mac[] = {0x90, 0xA2, 0xDA, 0x0E, 0x0C, 0xFF};

EthernetClient client;
EthernetUDP udp;
uint8_t packetbuf[256];
uint8_t printbuf[256];
static uint8_t scratch_raw[32];
static coap_rw_buffer_t scratch_buf = {scratch_raw, sizeof(scratch_raw)};

IPAddress ip(192,168,1,241); //<<<<IP hardcoded

//char json[] = "{\"sensor\":\"gps\",\"time\":1351824120,\"data\":[48.756080,2.302038]}";


/*const char* sensor = root["sensor"];
long time          = root["time"];
double latitude    = root["data"][0];
double longitude   = root["data"][1];*/



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
    time_t time = now();
    println("Time: " + time);
    
    if ((sz = udp.parsePacket()) > 0)
    {
        udp.read(packetbuf, sizeof(packetbuf));

        for (i=0;i<sz;i++)
        {  
            Serial.print(packetbuf[i], HEX);
            Serial.print(" ");
        }
        
        Serial.println("");
        
        String string;
        char c;
        char json[256];
        boolean pastHeaderFlag = 0;
        for (i=0;i<sz;i++){
            c = packetbuf[i];
            if(c == '{'){
              pastHeaderFlag = 1;
            }
            if(pastHeaderFlag){
              string = string + c;
            }
        }
        Serial.println(string);
        StaticJsonBuffer<256> jsonBuffer;
        strncpy(json, string.c_str(), sizeof(json));
        json[sizeof(json) - 1] = 0;
        JsonObject& root = jsonBuffer.parseObject(json);
        const char* id = root["ID"];
        Serial.println(id);
        const char* ii = root["II"];
        Serial.println(ii);
        const char* is = root["IS"];
        Serial.println(is);
        const char* sk = root["SK"];
        Serial.println(sk);
        //const char* nb = root["ST"]["OB"]["NB"];
        //const char* st = root["ST"];
        //Serial.println(st);
        //st = root["ST"]["OB"];
        //Serial.println(st);
        
        const char* nb = root["ST"]["OB"]["NB"];
        Serial.println(nb);
        const char* na = root["ST"]["OB"]["NA"];
        Serial.println(na);
        /*
        char* na = root["ST"]["OB"]["NA"];*/
        const char* act = root["ACT"];
        Serial.println(act);
        const char* res = root["RES"];
        Serial.println(res);
        
        
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
            Serial.print(" ");
            
            coap_handle_req(&scratch_buf, &pkt, &rsppkt);
            Serial.println("Primeiro Dump\n");
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
                Serial.println("Sending response");
            }
        }
    }
}


