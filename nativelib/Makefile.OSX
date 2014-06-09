# 调试参数
dbg = -g

BITTINESS = -m64
CC = gcc ${BITTINESS}
CXX = c++ ${BITTINESS}

# 共享库路径
LDFLAGS = /usr/local/lib #/libpbc.1.dylib /lib/libcrypto.10.dylib

# 静态库路径
STATICFLAGS = /usr/local/lib/libpbc.a /usr/local/lib/libgmp.a

# JNI包含路径
JNIINCLUDE = /System/Library/Frameworks/JavaVM.framework/Headers

# 编译器
#CC=gcc

# 目标文件名
TARGET=main

# 库文件名
LIBNAME=ibe

# JNI库名称
JNILIBNAME=$(LIBNAME)jni

# 编译测试程序
test: ibe.o
	$(CC) $(dbg) -c main.c
	$(CC) $(dbg) -o $(TARGET) ibe.o main.o -L$(LDFLAGS) -lgmp -lpbc
	./$(TARGET)

# 编译JNI共享库
jnilib: ibe.o hamaster_gradesign_ibe_jni_IBENative.o
	rm -f lib$(JNILIBNAME).d*
	$(CC) -dynamiclib -o lib$(JNILIBNAME).dylib hamaster_gradesign_ibe_jni_IBENative.o ibe.o -L$(LDFLAGS) -lgmp -lpbc -framework JavaVM

hamaster_gradesign_ibe_jni_IBENative.o:
	$(CC) $(dbg) -c hamaster_gradesign_ibe_jni_IBENative.c -I$(JNIINCLUDE)

# 编译共享库
#sharedlibrary: ibe.o
#	rm -f lib$(LIBNAME).d*
#	$(CC) -shared -o lib$(LIBNAME).dylib ibe.o

# 全部编译
all: jnilib test

main.o: main.c
	$(CC) $(dbg) -c main.c

ibe.o: ibe.c ibe.h
	$(CC) $(dbg) -c ibe.c

linecount:
	expr `grep -c "" ibe.c` + `grep -c "" ibe.h` + `grep -c "" main.c` + `grep -c "" Makefile`

clean:
	rm -f *.o
	rm -f lib$(LIBNAME)*
	rm -f *~
	rm -f $(TARGET)
	rm -f $(TARGET)static
	rm -f \#*
