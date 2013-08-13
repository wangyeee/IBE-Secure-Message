//
//  ibeerror.h
//  ssms
//
//  Created by 烨 王 on 12-3-30.
//  Copyright (c) 2012年 __MyCompanyName__. All rights reserved.
//

#ifndef ssms_ibeerror_h
#define ssms_ibeerror_h

/**
 * 操作成功完成
 */
#define ERR_SUCCESS 0

/**
 * 未知操作
 */
#define ERR_UNKNOWN_OP 1

/**
 * 处理请求过程中发生异常
 */
#define ERR_PROC_REQ 2

/**
 * 数据缺失
 */
#define ERR_EOF 3

/**
 * 错误的用户名/密码
 */
#define ERR_WRONG_PWD 4

/**
 * 错误的ID访问密码
 */
#define ERR_WRONG_ID_PWD 5

/**
 * 用户试图获取他人身份描述
 */
#define ERR_ID_THEFT 6

/**
 * ID已经被人使用
 */
#define ERR_ID_USED 7

/**
 * 注册所用的电子邮件地址已被使用
 */
#define ERR_EMAIL_USED 8

/**
 * 数据错误
 */
#define ERR_DATA_ERROR 9

/**
 * 用户等未被激活
 */
#define ERR_NOT_ACTIVE 10

#endif
