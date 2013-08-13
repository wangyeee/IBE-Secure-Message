#include "hamaster_gradesign_ibe_jni_IBENative.h"
#include "ibe.h"
#include "string.h"
#include "stdlib.h"

/*
 * Class:     hamaster_gradesign_ibe_jni_IBENative
 * Method:    setup_str
 * Signature: ([B[B[B[B[B)I
 */
JNIEXPORT jint JNICALL Java_hamaster_gradesign_ibe_jni_IBENative_setup_1str(JNIEnv* env, jobject obj,
						jbyteArray alpha_out,// 系统主密钥，长度20字节
						jbyteArray g_out,// 参数g，长度128字节
						jbyteArray g1_out,// 参数g1，长度128字节
						jbyteArray h_out,// 参数h，长度128字节
						jbyteArray pairing_str_in) {
  size_t result = 0;
  byte g_c[PBC_G_SIZE];
  byte g1_c[PBC_G_SIZE];
  byte h_c[PBC_G_SIZE];
  byte alpha_c[PBC_ZR_SIZE];

  byte pairing_str_c[MAX_PAIRING_STR_LENGTH];

  memset(pairing_str_c, 0, MAX_PAIRING_STR_LENGTH);
  jint length = (*env)->GetArrayLength(env, pairing_str_in);
  (*env)->GetByteArrayRegion(env, pairing_str_in, 0, length, pairing_str_c); 

  result = setup_str(alpha_c, PBC_ZR_SIZE, g_c, PBC_G_SIZE, g1_c, PBC_G_SIZE, h_c, PBC_G_SIZE, pairing_str_c, length);
  (*env)->SetByteArrayRegion(env, alpha_out, 0, PBC_ZR_SIZE, alpha_c);
  (*env)->SetByteArrayRegion(env, g_out, 0, PBC_G_SIZE, g_c);
  (*env)->SetByteArrayRegion(env, g1_out, 0, PBC_G_SIZE, g1_c);
  (*env)->SetByteArrayRegion(env, h_out, 0, PBC_G_SIZE, h_c);

  memset(g_c, 0, PBC_G_SIZE);
  memset(g1_c, 0, PBC_G_SIZE);
  memset(h_c, 0, PBC_G_SIZE);
  memset(alpha_c, 0, PBC_ZR_SIZE);
  memset(pairing_str_c, 0, MAX_PAIRING_STR_LENGTH);
  return result;
}

/*
 * Class:     hamaster_gradesign_ibe_jni_IBENative
 * Method:    keygen_str
 * Signature: ([B[B[B[B[B[B[B)I
 */
JNIEXPORT jint JNICALL Java_hamaster_gradesign_ibe_jni_IBENative_keygen_1str(JNIEnv* env, jobject obj,
						jbyteArray hID_out,// 私钥hID参数，长度128字节
						jbyteArray rID_out,// 私钥rID参数，长度20字节
						jbyteArray user_in,// 用户身份，如电子邮件地址
						jbyteArray alpha_in,// 系统主密钥，长度20字节
						jbyteArray g_in,// 参数g，长度128字节
						jbyteArray h_in,// 参数h，长度128字节
						jbyteArray pairing_str_in,
						jboolean random_rID) {
  size_t result = 0;
  byte hID_c[PBC_G_SIZE];
  byte* rID_c;//[PBC_ZR_SIZE];

  byte* user_c;
  byte alpha_c[PBC_ZR_SIZE];
  byte g_c[PBC_G_SIZE];
  byte h_c[PBC_G_SIZE];
  byte pairing_str_c[MAX_PAIRING_STR_LENGTH];

  jint length, user_length;

  memset(pairing_str_c, 0, MAX_PAIRING_STR_LENGTH);
  length = (*env)->GetArrayLength(env, pairing_str_in);
  (*env)->GetByteArrayRegion(env, pairing_str_in, 0, length, pairing_str_c);

  user_length = (*env)->GetArrayLength(env, user_in);
  user_c = (byte*) malloc(user_length);

  if (JNI_FALSE == random_rID) {
    jint rID_length = (*env)->GetArrayLength(env, rID_out);
    rID_c = (byte*) malloc(rID_length);
    (*env)->GetByteArrayRegion(env, rID_out, 0, rID_length, rID_c);
  } else {
    rID_c = (byte*) malloc(PBC_ZR_SIZE);
  }

  (*env)->GetByteArrayRegion(env, user_in, 0, user_length, user_c);
  (*env)->GetByteArrayRegion(env, alpha_in, 0, PBC_ZR_SIZE, alpha_c);
  (*env)->GetByteArrayRegion(env, g_in, 0, PBC_G_SIZE, g_c);
  (*env)->GetByteArrayRegion(env, h_in, 0, PBC_G_SIZE, h_c);

  result = keygen_str(hID_c, PBC_G_SIZE, rID_c, PBC_ZR_SIZE, user_c, user_length, alpha_c, PBC_ZR_SIZE, g_c, PBC_G_SIZE, h_c, PBC_G_SIZE, random_rID, pairing_str_c, length);

  (*env)->SetByteArrayRegion(env, hID_out, 0, PBC_G_SIZE, hID_c);
  if (random_rID)
    (*env)->SetByteArrayRegion(env, rID_out, 0, PBC_ZR_SIZE, rID_c);

  memset(user_c, 0, user_length);
  memset(alpha_c, 0, PBC_ZR_SIZE);
  memset(g_c, 0, PBC_G_SIZE);
  memset(h_c, 0, PBC_G_SIZE);
  memset(pairing_str_c, 0, MAX_PAIRING_STR_LENGTH);
  free(user_c);
  free(rID_c);
  return result;
}

/*
 * Class:     hamaster_gradesign_ibe_jni_IBENative
 * Method:    encrypt_str
 * Signature: ([B[B[B[B[B[B[B)I
 */
JNIEXPORT jint JNICALL Java_hamaster_gradesign_ibe_jni_IBENative_encrypt_1str(JNIEnv* env, jobject obj,
						jbyteArray cipher_buffer_out,// 输出密文，长度384字节，按照uvw顺序排列
						jbyteArray plain_in,// 明文，长度128字节
						jbyteArray g_in,// 接收方参数g，长度128字节
						jbyteArray g1_in,// 接收方参数g1，长度128字节
						jbyteArray h_in,// 接收方参数h，长度128字节
						jbyteArray alice_in,// 接收方身份
						jbyteArray pairing_str_in) {
  size_t result = 0;

  byte cipher_buffer_c[3 * PBC_G_SIZE];

  byte* plain_c;

  byte g_c[PBC_G_SIZE];
  byte g1_c[PBC_G_SIZE];
  byte h_c[PBC_G_SIZE];
  byte pairing_str_c[MAX_PAIRING_STR_LENGTH];
  byte* alice_c;
  jint plain_length, alice_length, length;

  memset(pairing_str_c, 0, MAX_PAIRING_STR_LENGTH);
  length = (*env)->GetArrayLength(env, pairing_str_in);
  (*env)->GetByteArrayRegion(env, pairing_str_in, 0, length, pairing_str_c);

  plain_length = (*env)->GetArrayLength(env, plain_in);
  plain_c = (byte*) malloc(plain_length);
  (*env)->GetByteArrayRegion(env, plain_in, 0, plain_length, plain_c);

  alice_length = (*env)->GetArrayLength(env, alice_in);
  alice_c = (byte*) malloc(alice_length);
  (*env)->GetByteArrayRegion(env, alice_in, 0, alice_length, alice_c);
  (*env)->GetByteArrayRegion(env, g_in, 0, PBC_G_SIZE, g_c);
  (*env)->GetByteArrayRegion(env, g1_in, 0, PBC_G_SIZE, g1_c);
  (*env)->GetByteArrayRegion(env, h_in, 0, PBC_G_SIZE, h_c);

  result = encrypt_str(cipher_buffer_c, 3 * PBC_G_SIZE, plain_c, plain_length, g_c, PBC_G_SIZE, g1_c, PBC_G_SIZE, h_c, PBC_G_SIZE, alice_c, alice_length, pairing_str_c, length);

  (*env)->SetByteArrayRegion(env, cipher_buffer_out, 0, 3 * PBC_G_SIZE, cipher_buffer_c);

  memset(cipher_buffer_c, 0, 3 * PBC_G_SIZE);
  memset(plain_c, 0, plain_length);
  free(plain_c);
  memset(g_c, 0, PBC_G_SIZE);
  memset(g1_c, 0, PBC_G_SIZE);
  memset(h_c, 0, PBC_G_SIZE);
  memset(alice_c, 0, alice_length);
  free(alice_c);
  memset(pairing_str_c, 0, MAX_PAIRING_STR_LENGTH);
  return result;
}

/*
 * Class:     hamaster_gradesign_ibe_jni_IBENative
 * Method:    decrypt_str
 * Signature: ([B[B[B[B[B)I
 */
JNIEXPORT jint JNICALL Java_hamaster_gradesign_ibe_jni_IBENative_decrypt_1str(JNIEnv* env, jobject obj,
						jbyteArray plain_buffer_out,// 输出明文，长度128字节
						jbyteArray cipher_in,// 输入密文，长度384字节，按照uvw顺序排列
						jbyteArray rID_in,// 接收方私钥rID，长度20字节
						jbyteArray hID_in,// 接收方私钥hID，长度128字节
						jbyteArray pairing_str_in) {
  size_t result = 0;

  byte plain_buffer_c[PBC_G_SIZE];
  byte cipher_buffer_c[3 * PBC_G_SIZE];
  byte rID_c[PBC_ZR_SIZE];
  byte hID_c[PBC_G_SIZE];
  byte pairing_str_c[MAX_PAIRING_STR_LENGTH];

  jint length;

  memset(pairing_str_c, 0, MAX_PAIRING_STR_LENGTH);
  length = (*env)->GetArrayLength(env, pairing_str_in);
  (*env)->GetByteArrayRegion(env, pairing_str_in, 0, length, pairing_str_c);

  (*env)->GetByteArrayRegion(env, cipher_in, 0, 3 * PBC_G_SIZE, cipher_buffer_c);
  (*env)->GetByteArrayRegion(env, rID_in, 0, PBC_ZR_SIZE, rID_c);
  (*env)->GetByteArrayRegion(env, hID_in, 0, PBC_G_SIZE, hID_c);

  memset(plain_buffer_c, 0, PBC_G_SIZE);
  result = decrypt_str(plain_buffer_c, PBC_G_SIZE, cipher_buffer_c, 3 * PBC_G_SIZE, rID_c, PBC_ZR_SIZE, hID_c, PBC_G_SIZE, pairing_str_c, length);

  (*env)->SetByteArrayRegion(env, plain_buffer_out, 0, PBC_G_SIZE, plain_buffer_c);

  memset(plain_buffer_c, 0, PBC_G_SIZE);
  memset(cipher_buffer_c, 0, 3 * PBC_G_SIZE);
  memset(rID_c, 0, PBC_ZR_SIZE);
  memset(hID_c, 0, PBC_G_SIZE);
  memset(pairing_str_c, 0, MAX_PAIRING_STR_LENGTH);
  return result;
}
