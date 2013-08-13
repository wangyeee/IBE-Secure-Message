//
//  ibe.c
//  ssms
//
//  Created by 烨 王 on 12-2-22.
//  Copyright (c) 2012年 __MyCompanyName__. All rights reserved.
//

#include "ibe.h"
#include "string.h"

static void setup_t(element_t alpha, element_t g, element_t g1, element_t h,
             pairing_t pairing) {    
    element_init_Zr(alpha, pairing);
    element_random(alpha);
    element_init_G1(g, pairing);
    element_random(g);
    element_init_G2(h, pairing);
    element_random(h);
    element_init_G1(g1, pairing);
    element_pow_zn(g1, g, alpha);
}

static void keygen_t(element_t hID, element_t user, element_t rID,
              element_t alpha, element_t param_g, element_t param_h,
              pairing_t pairing) {
    element_t e1;
    element_t e2;
    element_t e3;

    element_init_G2(hID, pairing);
    element_init_Zr(e1, pairing);
    element_init_G1(e2, pairing);
    element_init_G1(e3, pairing);
    element_sub(e1, alpha, user);
    element_invert(e1, e1);
    element_pow_zn(e2, param_g, rID);
    element_invert(e2, e2);
    element_mul(e3, param_h, e2);
    element_pow_zn(hID, e3, e1);
    element_random(e1);
    element_random(e2);
    element_random(e3);
}

static void encrypt_t(element_t cipher_u, element_t cipher_v, element_t cipher_w,
               element_t user, element_t message,
               element_t param_g, element_t param_g1, element_t param_h,
               pairing_t pairing) {
    element_t e1;
    element_t e2;
    element_t e3;
    element_t e4;
    element_t e5;
    element_t e6;
    element_t eg;
    element_t random_s;

    element_init_G1(cipher_u, pairing);
    element_init_GT(cipher_v, pairing);
    element_init_GT(cipher_w, pairing);
    element_init_Zr(e5, pairing);
    element_init_G1(e3, pairing);
    element_init_G1(e4, pairing);
    element_init_G2(eg, pairing);
    element_init_GT(e1, pairing);
    element_init_GT(e2, pairing);
    element_init_GT(e6, pairing);
    element_init_Zr(random_s, pairing);
    element_random(random_s);
    element_pow_zn(e3, param_g1, random_s);
    element_mul(e5, random_s, user);
    element_pow_zn(e4, param_g, e5);
    element_invert(e4, e4);
    element_mul(cipher_u, e3, e4);
    element_set(eg, param_g);
    pairing_apply(e1, param_g, eg, pairing);
    element_pow_zn(cipher_v, e1, random_s);
    pairing_apply(e2, param_g, param_h, pairing);
    element_pow_zn(e6, e2, random_s);
    element_invert(e6, e6);
    element_mul(cipher_w, message, e6);
    element_random(e1);
    element_random(e2);
    element_random(e3);
    element_random(e4);
    element_random(e5);
    element_random(e6);
    element_random(eg);
    element_random(random_s);
}

static void decrypt_t(element_t decrypted_message,
               element_t cipher_u, element_t cipher_v, element_t cipher_w,
               element_t hID, element_t rID, pairing_t pairing) {
    element_t e1;
    element_t e2;
    element_t e3;

    element_init_GT(e1, pairing);
    element_init_GT(e2, pairing);
    element_init_GT(e3, pairing);
    pairing_apply(e1, cipher_u, hID, pairing);
    element_pow_zn(e2, cipher_v, rID);
    element_mul(e3, cipher_w, e1);
    element_mul(decrypted_message, e3, e2);
    element_random(e1);
    element_random(e2);
    element_random(e3);
}

size_t setup_str(byte* alpha_out, size_t alpha_size,
                 byte* g_out, size_t g_size,
                 byte* g1_out, size_t g1_size,
                 byte* h_out, size_t h_size,
                 char* pairing_str_in, size_t pairing_str_length) {
    pairing_t p;
    element_t alpha;
    element_t g;
    element_t g1;
    element_t h;
    int size;

    if (strlen(pairing_str_in) > pairing_str_length ||
        pairing_str_length > MAX_PAIRING_STR_LENGTH)
        return -1;
    pairing_init_set_str(p, pairing_str_in);
    setup_t(alpha, g, g1, h, p);
    size = element_length_in_bytes(alpha);
    if (alpha_size < size)
        return size - alpha_size;
    element_to_bytes(alpha_out, alpha);
    element_random(alpha);
    size = element_length_in_bytes(g);
    if (g_size < size)
        return size - g_size;
    element_to_bytes(g_out, g);
    element_random(g);
    size = element_length_in_bytes(g1);
    if (g1_size < size)
        return size - g1_size;
    element_to_bytes(g1_out, g1);
    element_random(g1);
    size = element_length_in_bytes(h);
    if (h_size < size)
        return size - h_size;
    element_to_bytes(h_out, h);
    element_random(h);    
    return 0;
}

size_t keygen_str(byte* hID_out, size_t hID_size,
                  byte* rID_out, size_t rID_size,
                  byte* user_in, size_t user_size,
                  byte* alpha_in, size_t alpha_size,
                  byte* g_in, size_t g_size,
                  byte* h_in, size_t h_size,
                  int random_rID,
                  char* pairing_str_in, size_t pairing_str_length) {
    pairing_t p;
    element_t user;
    element_t rID;
    element_t hID;
    element_t alpha;
    element_t g;
    element_t h;
    int size;

    if (strlen(pairing_str_in) > pairing_str_length ||
        pairing_str_length > MAX_PAIRING_STR_LENGTH)
        return -1;
    if (alpha_size < PBC_ZR_SIZE)
        return PBC_ZR_SIZE - alpha_size;
    if (g_size < PBC_G_SIZE)
        return PBC_G_SIZE - g_size;
    if (h_size < PBC_G_SIZE)
        return PBC_G_SIZE - h_size;
    pairing_init_set_str(p, pairing_str_in);
    element_init_Zr(user, p);
    element_init_Zr(rID, p);
    element_init_G2(hID, p);
    element_init_Zr(alpha, p);
    element_init_G1(g, p);
    element_init_G2(h, p);
    element_from_hash(user, user_in, user_size);
    if (random_rID == 0) {
        element_from_hash(rID, rID_out, rID_size);
    } else {
        element_random(rID);
    }
    element_from_bytes(alpha, alpha_in);
    element_from_bytes(g, g_in);
    element_from_bytes(h, h_in);
    keygen_t(hID, user, rID, alpha, g, h, p);
    element_random(user);
    element_random(alpha);
    element_random(g);
    element_random(h);
    size = element_length_in_bytes(hID);
    if (hID_size < size)
        return hID_size - size;
    element_to_bytes(hID_out, hID);
    element_random(hID);
    if (random_rID) {
        size = element_length_in_bytes(rID);
        if (rID_size < size)
            return rID_size - size;
        element_to_bytes(rID_out, rID);
        element_random(rID);
    }
    return 0;
}

size_t encrypt_str(byte* cipher_buffer_out, size_t cipher_size,
                   byte* plain_in, size_t plain_size,
                   byte* g_in, size_t g_size,
                   byte* g1_in, size_t g1_size,
                   byte* h_in, size_t h_size,
                   byte* alice_in, size_t alice_size,
                   char* pairing_str_in, size_t pairing_str_length) {
    pairing_t p;
    element_t u;
    element_t v;
    element_t w;
    element_t plain;
    element_t g;
    element_t g1;
    element_t h;
    element_t alice;
    int size;
    byte* cipher0 = cipher_buffer_out;

    if (strlen(pairing_str_in) > pairing_str_length ||
        pairing_str_length > MAX_PAIRING_STR_LENGTH)
        return -1;
    if (plain_size > PBC_G_SIZE)
        return plain_size - PBC_G_SIZE; // 一次最多可以加密126字节
    if (g_size < PBC_G_SIZE)
        return PBC_G_SIZE - g_size;
    if (g1_size < PBC_G_SIZE)
        return PBC_G_SIZE - g1_size;
    if (h_size < PBC_G_SIZE)
        return PBC_G_SIZE - h_size;
    pairing_init_set_str(p, pairing_str_in);
    element_init_G1(u, p);
    element_init_GT(v, p);
    element_init_GT(w, p);
    element_init_GT(plain, p);
    element_init_G1(g, p);
    element_init_G1(g1, p);
    element_init_G2(h, p);
    element_init_Zr(alice, p);
    if (plain_size == PBC_G_SIZE) {
        element_from_bytes(plain, plain_in);
    } else {
        byte tmp[PBC_G_SIZE];
        memset(tmp, 0, PBC_G_SIZE);
        memcpy(tmp, plain_in, plain_size);
        element_from_bytes(plain, tmp);
    }
    element_from_bytes(g, g_in);
    element_from_bytes(g1, g1_in);
    element_from_bytes(h, h_in);
    element_from_hash(alice, alice_in, alice_size);
    encrypt_t(u, v, w, alice, plain, g, g1, h, p);
    element_random(alice);
    element_random(plain);
    element_random(g);
    element_random(g1);
    element_random(h);
    size = element_length_in_bytes(u);
    size += element_length_in_bytes(v);
    size += element_length_in_bytes(w);
    if (size > cipher_size)
        return size - cipher_size;
    cipher0 += element_to_bytes(cipher0, u);
    element_random(u);
    cipher0 += element_to_bytes(cipher0, v);
    element_random(v);
    element_to_bytes(cipher0, w);
    element_random(w);
    return 0;
}

size_t decrypt_str(byte* plain_buffer_out, size_t plain_size,
                   byte* cipher_in, size_t cipher_size,
                   byte* rID_in, size_t rID_size,
                   byte* hID_in, size_t hID_size,
                   char* pairing_str_in, size_t pairing_str_length) {
    pairing_t p;
    element_t plain;
    element_t u;
    element_t v;
    element_t w;
    element_t rID;
    element_t hID;
    int size;
    byte* cipher0 = cipher_in;

    if (strlen(pairing_str_in) > pairing_str_length ||
        pairing_str_length > MAX_PAIRING_STR_LENGTH)
        return -1;
    if (cipher_size < PBC_G_SIZE * 3)
        return PBC_G_SIZE * 3 - cipher_size;
    if (rID_size < PBC_ZR_SIZE)
        return PBC_ZR_SIZE - rID_size;
    if (hID_size < PBC_G_SIZE)
        return PBC_G_SIZE - hID_size;
    pairing_init_set_str(p, pairing_str_in);
    element_init_GT(plain, p);
    element_init_G1(u, p);
    element_init_GT(v, p);
    element_init_GT(w, p);
    element_init_Zr(rID, p);
    element_init_G2(hID, p);
    cipher0 += element_from_bytes(u, cipher0);
    cipher0 += element_from_bytes(v, cipher0);
    element_from_bytes(w, cipher0);
    element_from_bytes(rID, rID_in);
    element_from_bytes(hID, hID_in);
    decrypt_t(plain, u, v, w, hID, rID, p);
    element_random(u);
    element_random(v);
    element_random(w);
    element_random(hID); // 擦除内存中敏感数据，不保证擦除前不被窃取
    element_random(rID);
    size = element_length_in_bytes(plain);
    if (size > plain_size)
        return size - plain_size;
    element_to_bytes(plain_buffer_out, plain);
    element_random(plain);
    return 0;
}
