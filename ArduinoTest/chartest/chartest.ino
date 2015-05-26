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

char debug[] = "";

#define PORT 5683
static uint8_t mac[] = {0x90, 0xA2, 0xDA, 0x0E, 0x0C, 0xFF};

EthernetClient client;
EthernetUDP udp;
uint8_t packetbuf[256];
static uint8_t scratch_raw[32];
static coap_rw_buffer_t scratch_buf = {scratch_raw, sizeof(scratch_raw)};

IPAddress ip(192,168,1,241); //<<<<IP hardcoded
char json[] = "{\"sensor\":\"gps\",\"time\":1351824120,\"data\":[48.756080,2.302038]}";

StaticJsonBuffer<200> jsonBuffer;

JsonObject& root = jsonBuffer.parseObject(json);

const char* sensor = root["sensor"];
long time          = root["time"];
double latitude    = root["data"][0];
double longitude   = root["data"][1];



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
    Serial.println(time);
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
    
    if ((sz = udp.parsePacket()) > 0)
    {
        udp.read(packetbuf, sizeof(packetbuf));

        for (i=0;i<sz;i++)
        {
            Serial.print(packetbuf[i], HEX);
            Serial.print(" ");
        }
        Serial.println("");

        if (0 != (rc = coap_parse(&pkt, packetbuf, sz)))
        {
            Serial.print("Bad packet rc=");
            Serial.println(rc, DEC);
        }
        else
        {
            size_t rsplen = sizeof(packetbuf);
            coap_packet_t rsppkt;
            Serial.print("Packet");
            coap_dumpPacket(&pkt, debug);
            Serial.print(debug);
            coap_handle_req(&scratch_buf, &pkt, &rsppkt);

            memset(packetbuf, 0, UDP_TX_PACKET_MAX_SIZE);
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


