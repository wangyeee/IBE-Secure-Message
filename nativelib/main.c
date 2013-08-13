#include <stdio.h>
#include "ibe.h"
#include <string.h>

char* pairing_str = "type a \
			q 8780710799663312522437781984754049815806883199414208211028653399266475630880222957078625179422662221423155858769582317459277713367317481324925129998224791 \
			h 12016012264891146079388821366740534204802954401251311822919615131047207289359704531102844802183906537786776 \
			r 730750818665451621361119245571504901405976559617 \
			exp2 159 \
			exp1 107 \
			sign1 1 \
			sign0 1";

void print_hex(byte*, size_t);
void msg_rand(byte* msg);

int main(int argc, char** argv) {
  byte alpha[PBC_ZR_SIZE];
  byte g[PBC_G_SIZE];
  byte g1[PBC_G_SIZE];
  byte h[PBC_G_SIZE];

  char* email = "wangyeee@gmail.com";
  byte rID[PBC_ZR_SIZE];
  byte hID[PBC_G_SIZE];

  byte msg[PBC_G_SIZE];
  byte uvw[PBC_G_SIZE * 3];

  byte dec[PBC_G_SIZE];
  int i = 0;
 
  setup_str(alpha, 20, g, 128, g1, 128, h, 128, pairing_str, strlen(pairing_str));

  printf("setup:\nalpha:\n");
  print_hex(alpha, 20);
  printf("\ng:\n");
  print_hex(g, 128);
  printf("\ng1:\n");
  print_hex(g1, 128);
  printf("\nh:\n");
  print_hex(h, 128);

  keygen_str(hID, 128, rID, 20, email, strlen(email), alpha, 20, g, 128, h, 128, 1, pairing_str, strlen(pairing_str));

  printf("\n\nkeygen:\nemail:%s\n", email);
  printf("rID:\n");
  print_hex(rID, 20);
  printf("\nhID:\n");
  print_hex(hID, 128);

  msg_rand(msg);

  encrypt_str(uvw, PBC_G_SIZE * 3, msg, 128, g, 128, g1, 128, h, 128, email, strlen(email), pairing_str, strlen(pairing_str));

  printf("\n\nencrypt:\nplain:\n");
  print_hex(msg, 128);
  printf("\n\ncipher:\n");
  print_hex(uvw, 384);

  decrypt_str(dec, 128, uvw, 384, rID, 20, hID, 128, pairing_str, strlen(pairing_str));

  printf("\n\ndecrypt:\ndec:\n");
  print_hex(dec, 128);

  while (i < 128) {
    if (msg[i] != dec[i]) {
      printf("\n\ntest error, data at %d excepted:%02x, actural:%02x.", i, msg[i], dec[i]);
      i = -1;
      break;
    }
    i++;
  }
  if (i != -1)
    printf("\n\ntest passed.\n\n");
  return 0;
}

void msg_rand(byte* msg) {
  pairing_t p;
  element_t gt;

  pairing_init_set_str(p, pairing_str);
  element_init_GT(gt, p);
  element_random(gt);
  element_to_bytes(msg, gt);
}

void print_hex(byte* data, size_t length) {
  size_t i;
  for (i = 0; i < length; i++)
    printf("%02x", data[i]);
}
