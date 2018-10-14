# enable debug
dbg = -g

CC = gcc

LDFLAGS = /usr/local/lib

JNIINCLUDE = -I/usr/lib/jvm/java-9-openjdk-amd64/include -I/usr/lib/jvm/java-9-openjdk-amd64/include/linux

TARGET=main

# Library name
LIBNAME=ibe

# JNI library name
JNILIBNAME=$(LIBNAME)jni

# compile test target
test: ibe.o
	$(CC) $(dbg) -c main.c
	$(CC) $(dbg) -o $(TARGET) ibe.o main.o -L$(LDFLAGS) -lgmp -lpbc
	./$(TARGET)

# compile JNI shared library
jnilib: ibe.o hamaster_gradesign_ibe_jni_IBENative.o
	$(CC) -shared -fPIC -o lib$(JNILIBNAME).so hamaster_gradesign_ibe_jni_IBENative.o ibe.o -L$(LDFLAGS) -lgmp -lpbc

hamaster_gradesign_ibe_jni_IBENative.o:
	$(CC) $(dbg) -fPIC -c hamaster_gradesign_ibe_jni_IBENative.c $(JNIINCLUDE)

# compile shared library for linking with other C programmes.
sharedlibrary: ibe.o
	$(CC) -shared -o lib$(LIBNAME).so ibe.o

all: jnilib test

main.o: main.c
	$(CC) $(dbg) -c main.c

ibe.o: ibe.c ibe.h
	$(CC) $(dbg) -fPIC -c ibe.c

clean:
	rm -f *.o
	rm -f lib$(LIBNAME)*
	rm -f *~
	rm -f $(TARGET)
	rm -f $(TARGET)static
	rm -f \#*
