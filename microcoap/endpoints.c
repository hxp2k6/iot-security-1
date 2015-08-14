#include <stdbool.h>
#include <string.h>
#include "coap.h"

static char light = '0';

//const uint16_t rsplen = 1500;
//static char rsp[1500] = "";
const uint16_t rsplen = 200;
static char rsp[200] = "";


const uint16_t rsp2len = 1500;
static char rsp2[1500] = "";

void build_rsp(void);

//Meu codigo


#ifdef ARDUINO
#include "Arduino.h"
static int led = 6;
void endpoint_setup(void)
{
  pinMode(led, OUTPUT);
  build_rsp();
}
#else
#include <stdio.h>
void endpoint_setup(void)
{
  build_rsp();
}
#endif

/*static const coap_endpoint_path_t path_well_known_core = {2, {".well-known", "core"}};
static int handle_get_well_known_core(coap_rw_buffer_t *scratch, const coap_packet_t *inpkt, coap_packet_t *outpkt, uint8_t id_hi, uint8_t id_lo)
{
    return coap_make_response(scratch, outpkt, (const uint8_t *)rsp, strlen(rsp), id_hi, id_lo, &inpkt->tok, COAP_RSPCODE_CONTENT, COAP_CONTENTTYPE_APPLICATION_LINKFORMAT);
}*/

/*static const coap_endpoint_path_t path_light = {1, {"light"}};
static int handle_get_light(coap_rw_buffer_t *scratch, const coap_packet_t *inpkt, coap_packet_t *outpkt, uint8_t id_hi, uint8_t id_lo)
{
    create_response_payload(rsp2);
    //return coap_make_response(scratch, outpkt, (const uint8_t *)&light, 1, id_hi, id_lo, &inpkt->tok, COAP_RSPCODE_CONTENT, COAP_CONTENTTYPE_TEXT_PLAIN);
    return coap_make_response(scratch, outpkt, (const uint8_t *)rsp2, strlen(rsp2), id_hi, id_lo, &inpkt->tok, COAP_RSPCODE_CONTENT, COAP_CONTENTTYPE_TEXT_PLAIN);
}*/

/*static int handle_put_light(coap_rw_buffer_t *scratch, const coap_packet_t *inpkt, coap_packet_t *outpkt, uint8_t id_hi, uint8_t id_lo)
{
    if (inpkt->payload.len == 0)
        return coap_make_response(scratch, outpkt, NULL, 0, id_hi, id_lo, &inpkt->tok, COAP_RSPCODE_BAD_REQUEST, COAP_CONTENTTYPE_TEXT_PLAIN);
    if (inpkt->payload.p[0] == '1')
    {
        light = '1';
#ifdef ARDUINO
        digitalWrite(led, HIGH);
#else
        printf("ON\n");
#endif
        return coap_make_response(scratch, outpkt, (const uint8_t *)&light, 1, id_hi, id_lo, &inpkt->tok, COAP_RSPCODE_CHANGED, COAP_CONTENTTYPE_TEXT_PLAIN);
    }
    else
    {
        light = '0';
#ifdef ARDUINO
        digitalWrite(led, LOW);
#else
        printf("OFF\n");
#endif
        return coap_make_response(scratch, outpkt, (const uint8_t *)&light, 1, id_hi, id_lo, &inpkt->tok, COAP_RSPCODE_CHANGED, COAP_CONTENTTYPE_TEXT_PLAIN);
    }
}*/


static const coap_endpoint_path_t path_watch = {1, {"watch"}};
static int handle_get_watch(coap_rw_buffer_t *scratch, const coap_packet_t *inpkt, coap_packet_t *outpkt, uint8_t id_hi, uint8_t id_lo, char *address, char *request)
{
  char temp[100];
  coap_get_path(inpkt->opts, inpkt->numopts, temp);
  
  char newChar[100];
  char resultado[20];
  sprintf(newChar, "%s", "coap://192.168.1.241/");
  strcat(newChar, temp);
  char *tempPointer;
  tempPointer = &newChar;
  //sprintf(resultado, "%s", path);
  strcat(resultado, "resultado");
  if (strcmp(newChar, address) != 0) {
    create_response_payload_pathError(rsp2);
    //create_response_payload_debug(rsp2, address);
  }
  else if (strcmp(request, "GET") != 0){
    create_response_payload_requestError(rsp2);
    //create_response_payload_debug(rsp2, request);
  }
  else{
    create_response_payload(rsp2);
    //create_response_payload_debug(rsp2, resultado);
  }
 
  //create_response_payload_debug(rsp2, resultado);


  //return coap_make_response(scratch, outpkt, (const uint8_t *)&light, 1, id_hi, id_lo, &inpkt->tok, COAP_RSPCODE_CONTENT, COAP_CONTENTTYPE_TEXT_PLAIN);
  return coap_make_response(scratch, outpkt, (const uint8_t *)rsp2, strlen(rsp2), id_hi, id_lo, &inpkt->tok, COAP_RSPCODE_CONTENT, COAP_CONTENTTYPE_TEXT_PLAIN);
}

static int handle_post_watch(coap_rw_buffer_t *scratch, const coap_packet_t *inpkt, coap_packet_t *outpkt, uint8_t id_hi, uint8_t id_lo, char *path, char *request)
{
  if (inpkt->payload.len == 0)
    return coap_make_response(scratch, outpkt, NULL, 0, id_hi, id_lo, &inpkt->tok, COAP_RSPCODE_BAD_REQUEST, COAP_CONTENTTYPE_TEXT_PLAIN);
  else
  {
    create_response_payload_post(rsp2);
    //return coap_make_response(scratch, outpkt, (const uint8_t *)rsp2, strlen(rsp2), id_hi, id_lo, &inpkt->tok, COAP_RSPCODE_CONTINUE, COAP_CONTENTTYPE_TEXT_PLAIN);
    return coap_make_response(scratch, outpkt, (const uint8_t *)rsp2, strlen(rsp2), id_hi, id_lo, &inpkt->tok, COAP_RSPCODE_CONTENT, COAP_CONTENTTYPE_TEXT_PLAIN);
  }
}

const coap_endpoint_t endpoints[] =
{
  //{COAP_METHOD_GET, handle_get_well_known_core, &path_well_known_core, "ct=40"},
  //{COAP_METHOD_GET, handle_get_light, &path_light, "ct=0"},
  {COAP_METHOD_GET, handle_get_watch, &path_watch, "ct=0"},
  {COAP_METHOD_POST, handle_post_watch, &path_watch, "ct=0"},
  //{COAP_METHOD_PUT, handle_put_light, &path_light, NULL},
  {(coap_method_t)0, NULL, NULL, NULL}
};

void build_rsp(void)
{
  uint16_t len = rsplen;
  const coap_endpoint_t *ep = endpoints;
  int i;

  len--; // Null-terminated string

  while (NULL != ep->handler)
  {
    if (NULL == ep->core_attr) {
      ep++;
      continue;
    }

    if (0 < strlen(rsp)) {
      strncat(rsp, ",", len);
      len--;
    }

    strncat(rsp, "<", len);
    len--;

    for (i = 0; i < ep->path->count; i++) {
      strncat(rsp, "/", len);
      len--;

      strncat(rsp, ep->path->elems[i], len);
      len -= strlen(ep->path->elems[i]);
    }

    strncat(rsp, ">;", len);
    len -= 2;

    strncat(rsp, ep->core_attr, len);
    len -= strlen(ep->core_attr);

    ep++;
  }
}

void create_response_payload(const uint8_t *buffer)
{
  char *response = "1337";
  memcpy((void*)buffer, response, strlen(response));
  memset((void*)buffer + strlen(response), '\0', 1);
}

void create_response_payload_debug(const uint8_t *buffer, char temp[]) {
  memcpy((void*)buffer, temp, strlen(temp));
}

void create_response_payload_pathError(const uint8_t *buffer) {
  char *response = "path error";
  memcpy((void*)buffer, response, strlen(response));
  memset((void*)buffer + strlen(response), '\0', 1);
}

void create_response_payload_requestError(const uint8_t *buffer) {
  char *response = "request error";
  memcpy((void*)buffer, response, strlen(response));
  memset((void*)buffer + strlen(response), '\0', 1);
}

void create_response_payload_post(const uint8_t *buffer)
{
  char *response = "autorizado";
  memcpy((void*)buffer, response, strlen(response));
}



