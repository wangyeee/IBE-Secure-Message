//
//  ibe.h
//  ssms
//
//  Created by 烨 王 on 12-2-22.
//  Copyright (c) 2012年 __MyCompanyName__. All rights reserved.
//

#ifndef _IBE_H_
#define _IBE_H_

#include <pbc/pbc.h>
#include <stdlib.h>
#include "ibecommon.h"

/*
 * 生成系统参数
 */
size_t setup_str(byte* alpha_out, size_t alpha_size, // 系统主密钥，长度20字节
                 byte* g_out, size_t g_size,         // 参数g，长度128字节
                 byte* g1_out, size_t g1_size,       // 参数g1，长度128字节
                 byte* h_out, size_t h_size,         // 参数h，长度128字节
                 char* pairing_str_in, size_t pairing_str_length);

/*
 * 为用户生成私钥
 */
size_t keygen_str(byte* hID_out, size_t hID_size,    // 私钥hID参数，长度128字节
                  byte* rID_out, size_t rID_size,    // 私钥rID参数，长度20字节
                  byte* user_in, size_t user_size,   // 用户身份，如电子邮件地址
                  byte* alpha_in, size_t alpha_size, // 系统主密钥，长度20字节
                  byte* g_in, size_t g_size,         // 参数g，长度128字节
                  byte* h_in, size_t h_size,         // 参数h，长度128字节
                  int random_rID,                    // 是否随机生成rID
                  char* pairing_str_in, size_t pairing_str_length);

/*
 * 加密数据
 */
size_t encrypt_str(byte* cipher_buffer_out, size_t cipher_size, // 输出密文，长度384字节，按照uvw顺序排列
                   byte* plain_in, size_t plain_size,           // 明文，长度128字节
                   byte* g_in, size_t g_size,                   // 接收方参数g，长度128字节
                   byte* g1_in, size_t g1_size,                 // 接收方参数g1，长度128字节
                   byte* h_in, size_t h_size,                   // 接收方参数h，长度128字节
                   byte* alice_in, size_t alice_size,           // 接收方身份，如电子邮件地址
                   char* pairing_str_in, size_t pairing_str_length);

/*
 * 解密数据
 */
size_t decrypt_str(byte* plain_buffer_out, size_t plain_size, // 输出明文，长度128字节
                   byte* cipher_in, size_t cipher_size,       // 输入密文，长度384字节，按照uvw顺序排列
                   byte* rID_in, size_t rID_size,             // 接收方私钥rID，长度20字节
                   byte* hID_in, size_t hID_size,             // 接收方私钥hID，长度128字节
                   char* pairing_str_in, size_t pairing_str_length);

#endif /* _IBE_H_ */
